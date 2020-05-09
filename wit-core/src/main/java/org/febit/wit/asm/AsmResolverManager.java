// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.wit.asm;

import org.febit.wit.exceptions.UncheckedException;
import org.febit.wit.resolvers.GetResolver;
import org.febit.wit.resolvers.ResolverManager;
import org.febit.wit.resolvers.SetResolver;
import org.febit.wit.util.ClassMap;
import org.febit.wit.util.ClassUtil;
import org.febit.wit.util.StringUtil;
import org.febit.wit.util.bean.FieldInfo;
import org.febit.wit.util.bean.FieldInfoResolver;
import org.febit.wit_shaded.asm.ClassWriter;
import org.febit.wit_shaded.asm.Constants;
import org.febit.wit_shaded.asm.Label;
import org.febit.wit_shaded.asm.MethodWriter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @author zqq90
 */
public class AsmResolverManager extends ResolverManager {

    private static final String[] ASM_RESOLVER = {"org/febit/wit/asm/AsmResolver"};
    private static final ClassMap<AsmResolver> CACHE = new ClassMap<>();

    @Override
    protected SetResolver resolveSetResolver(Class<?> type) {
        SetResolver resolver = getAsmResolver(type);
        if (resolver != null) {
            return resolver;
        }
        return super.resolveSetResolver(type);
    }

    @Override
    protected GetResolver resolveGetResolver(Class<?> type) {
        GetResolver resolver = getAsmResolver(type);
        if (resolver != null) {
            return resolver;
        }
        return super.resolveGetResolver(type);
    }

    private AsmResolver getAsmResolver(Class<?> type) {
        AsmResolver resolver = CACHE.get(type);
        if (resolver == null) {
            synchronized (CACHE) {
                resolver = CACHE.get(type);
                if (resolver == null) {
                    try {
                        resolver = (AsmResolver) createResolverClass(type)
                                .getConstructor().newInstance();
                        resolver = CACHE.putIfAbsent(type, resolver);
                    } catch (Exception | LinkageError e) {
                        logger.error("Failed to create resolver for:".concat(type.getName()), e);
                    }
                }
            }
        }
        return resolver;
    }

    static Class<?> createResolverClass(Class<?> beanClass) {
        //XXX: rewrite
        if (!ClassUtil.isPublic(beanClass)) {
            throw new UncheckedException(StringUtil.format("Class<?> [{}] is not public", beanClass));
        }
        final String className = "org.febit.wit.asm.Resolver" + AsmUtil.NEXT_SN.getAndIncrement();

        final ClassWriter classWriter = new ClassWriter(Constants.V1_5, Constants.ACC_PUBLIC + Constants.ACC_FINAL,
                AsmUtil.getInternalName(className), "java/lang/Object", ASM_RESOLVER);
        AsmUtil.visitConstructor(classWriter);

        final FieldInfo[] all = FieldInfoResolver.resolve(beanClass)
                .sorted()
                .toArray(FieldInfo[]::new);
        Arrays.sort(all);
        final int size = all.length;
        int[] hashes;
        int[] indexer;
        if (size > 0) {
            hashes = new int[size];
            indexer = new int[size];
            int hashCount = 0;
            int hash;
            hashes[hashCount++] = hash = all[0].hashOfName;
            int i = 1;
            while (i < size) {
                FieldInfo fieldInfo = all[i];
                if (hash != fieldInfo.hashOfName) {
                    indexer[hashCount - 1] = i;
                    hashes[hashCount++] = hash = fieldInfo.hashOfName;
                }
                i++;
            }
            indexer[hashCount - 1] = size;
            hashes = Arrays.copyOf(hashes, hashCount);
            indexer = Arrays.copyOf(indexer, hashCount);
        } else {
            hashes = null;
            indexer = null;
        }

        visitXetMethod(true, classWriter, beanClass, all, hashes, indexer);
        visitXetMethod(false, classWriter, beanClass, all, hashes, indexer);

        //getMatchClass
        final MethodWriter m = classWriter.visitMethod(Constants.ACC_PUBLIC, "getMatchClass",
                "()Ljava/lang/Class;", null);
        m.visitInsn(Constants.ACONST_NULL);
        m.visitInsn(Constants.ARETURN);
        m.visitMaxs();

        return AsmUtil.loadClass(className, classWriter);
    }

    private static void visitXetMethod(final boolean isGetter, final ClassWriter classWriter, final Class<?> beanClass,
                                       final FieldInfo[] all, final int[] hashes, final int[] indexer) {
        final String beanName = AsmUtil.getBoxedInternalName(beanClass);
        final MethodWriter m;
        if (isGetter) {
            m = classWriter.visitMethod(Constants.ACC_PUBLIC, "get",
                    "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", null);
        } else {
            m = classWriter.visitMethod(Constants.ACC_PUBLIC, "set",
                    "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V", null);
        }
        final int fieldInfosLength = all.length;
        if (fieldInfosLength != 0) {
            final Label finalEndLabel = new Label();
            if (fieldInfosLength < 4) {
                visitXetFields(isGetter, m, all, 0, fieldInfosLength, beanName, finalEndLabel);
            } else {
                m.visitVarInsn(Constants.ALOAD, 2);
                m.invokeVirtual("java/lang/Object", "hashCode", "()I");

                final int size = hashes.length;
                Label[] labels = new Label[size];
                for (int i = 0; i < size; i++) {
                    labels[i] = new Label();
                }

                m.visitLookupSwitchInsn(finalEndLabel, hashes, labels);
                int start = 0;
                for (int i = 0; i < size; i++) {
                    int end = indexer[i];
                    m.visitLabel(labels[i]);
                    visitXetFields(isGetter, m, all, start, end, beanName, finalEndLabel);
                    start = end;
                }
            }
            m.visitLabel(finalEndLabel);
        }
        //Exception
        m.visitTypeInsn(Constants.NEW, "org/febit/wit/exceptions/ScriptRuntimeException");
        m.visitInsn(Constants.DUP);
        m.visitLdcInsn("Invalid property " + beanClass.getName() + '#');
        m.visitVarInsn(Constants.ALOAD, 2);
        m.invokeStatic(AsmUtil.TYPE_STRING_NAME, "valueOf", "(Ljava/lang/Object;)Ljava/lang/String;");
        m.invokeVirtual(AsmUtil.TYPE_STRING_NAME, "concat", "(Ljava/lang/String;)Ljava/lang/String;");
        m.visitMethodInsn(Constants.INVOKESPECIAL, "org/febit/wit/exceptions/ScriptRuntimeException",
                AsmUtil.METHOD_CTOR, "(Ljava/lang/String;)V");
        m.visitInsn(Constants.ATHROW);
        m.visitMaxs();
    }

    private static void visitXetFields(final boolean isGetter, final MethodWriter m,
                                       final FieldInfo[] fieldInfos, final int start, final int end,
                                       final String beanName, final Label failedMatchLabel) {
        final Label[] gotoTable = new Label[end - start];
        //if ==
        for (int i = start; i < end; i++) {
            Label label = new Label();
            gotoTable[i - start] = label;
            m.visitLdcInsn(fieldInfos[i].name);
            m.visitVarInsn(Constants.ALOAD, 2);
            // if == goto
            m.visitJumpInsn(Constants.IF_ACMPEQ, label);
        }
        //if equals
        for (int i = start; i < end; i++) {
            m.visitLdcInsn(fieldInfos[i].name);
            m.visitVarInsn(Constants.ALOAD, 2);
            m.invokeVirtual(AsmUtil.TYPE_STRING_NAME, "equals", "(Ljava/lang/Object;)Z");
            // if true goto
            m.visitJumpInsn(Constants.IFNE, gotoTable[i - start]);
        }
        //failed, to end
        m.visitJumpInsn(Constants.GOTO, failedMatchLabel);
        //actions
        for (int i = start; i < end; i++) {
            m.visitLabel(gotoTable[i - start]);
            FieldInfo info = fieldInfos[i];
            if (isGetter) {
                appendGetFieldCode(m, info, beanName);
            } else {
                appendSetFieldCode(m, info, beanName);
            }
        }
    }

    private static void appendGetFieldCode(final MethodWriter m, final FieldInfo fieldInfo, final String beanName) {
        final Method getter = fieldInfo.getGetterMethod();
        final Field field = fieldInfo.getField();
        if (getter != null || field != null) {
            Class<?> resultType = getter != null ? getter.getReturnType() : field.getType();
            m.visitVarInsn(Constants.ALOAD, 1);
            m.checkCast(beanName);
            if (getter != null) {
                //return book.getName()
                m.invokeVirtual(beanName, getter.getName(), AsmUtil.getDescriptor(getter));
            } else {
                //return book.name
                m.visitFieldInsn(Constants.GETFIELD, beanName, fieldInfo.name, AsmUtil.getDescriptor(resultType));
            }
            AsmUtil.visitBoxIfNeed(m, resultType);
            m.visitInsn(Constants.ARETURN);
        } else {
            //Unreadable Exception
            AsmUtil.visitScriptRuntimeException(m, StringUtil.format("Unreadable property {}#{}",
                    fieldInfo.owner.getName(), fieldInfo.name));
        }
    }

    private static void appendSetFieldCode(final MethodWriter m, final FieldInfo fieldInfo, final String beanName) {
        final Method setter = fieldInfo.getSetterMethod();
        if (setter != null || fieldInfo.isFieldSettable()) {
            Class<?> fieldClass = setter != null ? setter.getParameterTypes()[0] : fieldInfo.getField().getType();
            m.visitVarInsn(Constants.ALOAD, 1);
            m.checkCast(beanName);
            m.visitVarInsn(Constants.ALOAD, 3);
            m.checkCast(AsmUtil.getBoxedInternalName(fieldClass));
            AsmUtil.visitUnboxIfNeed(m, fieldClass);
            if (setter != null) {
                //book.setName((String)name)
                m.invokeVirtual(beanName, setter.getName(), AsmUtil.getDescriptor(setter));
            } else {
                //book.name = (String) name
                m.visitFieldInsn(Constants.PUTFIELD, beanName, fieldInfo.name, AsmUtil.getDescriptor(fieldClass));
            }

            m.visitInsn(Constants.RETURN);
        } else {
            //UnwriteableException
            AsmUtil.visitScriptRuntimeException(m, StringUtil.format("Unwriteable property {}#{}",
                    fieldInfo.owner.getName(), fieldInfo.name));
        }
    }
}
