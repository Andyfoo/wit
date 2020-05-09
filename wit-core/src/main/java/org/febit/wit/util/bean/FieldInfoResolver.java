// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.wit.util.bean;

import org.febit.wit.util.ClassUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author zqq90
 */
public class FieldInfoResolver {

    private final Class<?> beanType;
    private final Map<String, FieldInfo> fieldInfos;

    private FieldInfoResolver(Class<?> beanClass) {
        this.beanType = beanClass;
        this.fieldInfos = new HashMap<>();
    }

    public static Stream<FieldInfo> resolve(Class<?> beanClass) {
        return new FieldInfoResolver(beanClass).resolve();
    }

    @SuppressWarnings({
            "squid:S3776" // Cognitive Complexity of methods should not be too high
    })
    private Stream<FieldInfo> resolve() {
        for (Field field : beanType.getFields()) {
            if (!ClassUtil.isStatic(field)) {
                registerField(field);
            }
        }
        for (Method method : beanType.getMethods()) {
            if (ClassUtil.isStatic(method)
                    || method.getDeclaringClass() == Object.class) {
                continue;
            }
            int argsCount = method.getParameterCount();
            String methodName = method.getName();
            int methodNameLength = methodName.length();
            if (argsCount == 0
                    && method.getReturnType() != void.class) {
                if (methodNameLength > 3
                        && methodName.startsWith("get")) {
                    registerGetterMethod(cutFieldName(methodName, 3), method);
                } else if (methodNameLength > 2
                        && methodName.startsWith("is")) {
                    registerGetterMethod(cutFieldName(methodName, 2), method);
                }
            } else if (argsCount == 1
                    && methodNameLength > 3
                    && method.getReturnType() == void.class
                    && methodName.startsWith("set")) {
                registerSetterMethod(cutFieldName(methodName, 3), method);
            }
        }
        return fieldInfos.values().stream();
    }

    private FieldInfo getOrCreateFieldInfo(String name) {
        return fieldInfos.computeIfAbsent(name, key -> new FieldInfo(beanType, key));
    }

    private void registerField(Field field) {
        getOrCreateFieldInfo(field.getName()).field = field;
    }

    private void registerGetterMethod(String name, Method method) {
        getOrCreateFieldInfo(name).getterMethod = method;
    }

    private void registerSetterMethod(String name, Method method) {
        getOrCreateFieldInfo(name).setterMethod = method;
    }

    static String cutFieldName(final String string, final int from) {
        final int nextIndex = from + 1;
        final int len = string.length();
        if (len > nextIndex) {
            char c = string.charAt(nextIndex);
            if (c >= 'A' && c <= 'Z') {
                return string.substring(from);
            }
        }
        char[] buffer = new char[len - from];
        string.getChars(from, len, buffer, 0);
        char c = buffer[0];
        if (c >= 'A' && c <= 'Z') {
            buffer[0] = (char) (c + 0x20);
        }
        return new String(buffer);
    }
}
