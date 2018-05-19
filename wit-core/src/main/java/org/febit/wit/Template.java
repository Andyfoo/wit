// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.wit;

import java.io.OutputStream;
import java.io.Writer;
import java.util.Map;
import org.febit.wit.core.Parser;
import org.febit.wit.core.ast.TemplateAST;
import org.febit.wit.debug.BreakPointListener;
import org.febit.wit.exceptions.ParseException;
import org.febit.wit.exceptions.ScriptRuntimeException;
import org.febit.wit.exceptions.TemplateException;
import org.febit.wit.io.Out;
import org.febit.wit.io.impl.DiscardOut;
import org.febit.wit.io.impl.OutputStreamOut;
import org.febit.wit.io.impl.WriterOut;
import org.febit.wit.loaders.Resource;
import org.febit.wit.util.InternedEncoding;

/**
 *
 * @author zqq90
 */
public class Template {

    private final Engine engine;
    private final String name;
    private final Resource resource;

    private volatile TemplateAST ast;

    Template(Engine engine, String name, Resource resource) {
        this.engine = engine;
        this.name = name;
        this.resource = resource;
    }

    /**
     * Reload this template.
     *
     * @since 1.4.0
     * @throws ParseException
     */
    public void reload() {
        prepareAst(true);
    }

    private TemplateAST prepareAst() {
        final TemplateAST myAst = this.ast;
        if (myAst != null
                && !this.resource.isModified()) {
            return myAst;
        }
        return prepareAst(false);
    }

    private synchronized TemplateAST prepareAst(boolean forceRebuild) {
        TemplateAST myAst = this.ast;
        if (forceRebuild || myAst == null || this.resource.isModified()) {
            myAst = Parser.parse(this);
            this.ast = myAst;
        }
        return myAst;
    }

    /**
     * Merge this template.
     *
     * @param outputStream
     * @return Context
     * @throws ScriptRuntimeException
     * @throws ParseException
     */
    public Context merge(final OutputStream outputStream) {
        return merge(Vars.EMPTY, new OutputStreamOut(outputStream, engine));
    }

    /**
     * Merge this template.
     *
     * @param out
     * @param encoding
     * @return Context
     * @throws ScriptRuntimeException
     * @throws ParseException
     */
    public Context merge(final OutputStream out, final String encoding) {
        return merge(Vars.EMPTY, new OutputStreamOut(out, InternedEncoding.intern(encoding), engine));
    }

    /**
     * Merge this template.
     *
     * @param writer
     * @return Context
     * @throws ScriptRuntimeException
     * @throws ParseException
     */
    public Context merge(final Writer writer) {
        return merge(Vars.EMPTY, new WriterOut(writer, engine));
    }

    /**
     * Merge this template.
     *
     * @param vars
     * @param outputStream
     * @return Context
     * @throws ScriptRuntimeException
     * @throws ParseException
     */
    public Context merge(final Map<String, Object> vars, final OutputStream outputStream) {
        return merge(Vars.of(vars), new OutputStreamOut(outputStream, engine));
    }

    /**
     * Merge this template.
     *
     * @param vars
     * @param out
     * @param encoding
     * @return Context
     * @throws ScriptRuntimeException
     * @throws ParseException
     */
    public Context merge(final Map<String, Object> vars, final OutputStream out, final String encoding) {
        return merge(Vars.of(vars), new OutputStreamOut(out, InternedEncoding.intern(encoding), engine));
    }

    /**
     * Merge this template.
     *
     * @param vars
     * @param writer
     * @return Context
     * @throws ScriptRuntimeException
     * @throws ParseException
     */
    public Context merge(final Map<String, Object> vars, final Writer writer) {
        return merge(Vars.of(vars), new WriterOut(writer, engine));
    }

    /**
     * Merge this template.
     *
     * @param vars
     * @param out
     * @return Context
     * @throws ScriptRuntimeException
     * @throws ParseException
     */
    public Context merge(final Vars vars, final OutputStream out) {
        return merge(vars, new OutputStreamOut(out, engine));
    }

    /**
     * Merge this template.
     *
     * @param vars
     * @param out
     * @param encoding
     * @return Context
     * @throws ScriptRuntimeException
     * @throws ParseException
     */
    public Context merge(final Vars vars, final OutputStream out, final String encoding) {
        return merge(vars, new OutputStreamOut(out, InternedEncoding.intern(encoding), engine));
    }

    /**
     * Merge this template.
     *
     * @param vars
     * @param writer
     * @return Context
     * @throws ScriptRuntimeException
     * @throws ParseException
     */
    public Context merge(final Vars vars, final Writer writer) {
        return merge(vars, new WriterOut(writer, engine));
    }

    /**
     * Merge this template.
     *
     * @since 1.4.0
     * @param out
     * @return Context
     * @throws ScriptRuntimeException
     * @throws ParseException
     */
    public Context merge(final Out out) {
        return merge(Vars.EMPTY, out);
    }

    /**
     * Merge this template, and discard outputs.
     *
     * @since 2.4.0
     * @return Context
     * @throws ScriptRuntimeException
     * @throws ParseException
     */
    public Context merge() {
        return merge(Vars.EMPTY);
    }

    /**
     * Merge this template, and discard outputs.
     *
     * @since 2.4.0
     * @param vars
     * @return Context
     * @throws ScriptRuntimeException
     * @throws ParseException
     */
    public Context merge(final Map<String, Object> vars) {
        return merge(Vars.of(vars));
    }

    /**
     * Merge this template, and discard outputs.
     *
     * @since 2.4.0
     * @param vars
     * @return Context
     * @throws ScriptRuntimeException
     * @throws ParseException
     */
    public Context merge(final Vars vars) {
        return merge(vars, DiscardOut.INSTANCE);
    }

    /**
     * Merge this template.
     *
     * @param vars
     * @param out
     * @return Context
     * @throws ScriptRuntimeException
     * @throws ParseException
     */
    public Context merge(final Vars vars, final Out out) {
        try {
            return prepareAst()
                    .execute(this, out, vars);
        } catch (Exception e) {
            throw completeException(e);
        }
    }

    public Context mergeToContext(final InternalContext context, final Vars vars) {
        try {
            return prepareAst()
                    .execute(this, context, vars);
        } catch (Exception e) {
            throw completeException(e);
        }
    }

    /**
     * Debug this template.
     *
     * @param vars
     * @param out
     * @param listener
     * @return Context
     * @throws ScriptRuntimeException
     * @throws ParseException
     */
    public Context debug(final Vars vars, final Out out, final BreakPointListener listener) {
        try {
            return Parser.parse(this, listener)
                    .execute(this, out, vars);
        } catch (Exception e) {
            throw completeException(e);
        }
    }

    /**
     * Debug this template, and discard outputs.
     *
     * @since 2.4.0
     * @param vars
     * @param listener
     * @return Context
     * @throws ScriptRuntimeException
     * @throws ParseException
     */
    public Context debug(final Vars vars, final BreakPointListener listener) {
        return debug(vars, DiscardOut.INSTANCE, listener);
    }

    /**
     * Debug this template, and discard outputs.
     *
     * @since 2.5.0
     * @param out
     * @param listener
     * @return Context
     * @throws ScriptRuntimeException
     * @throws ParseException
     */
    public Context debug(final Out out, final BreakPointListener listener) {
        return debug(Vars.EMPTY, out, listener);
    }

    /**
     * Debug this template, and discard outputs.
     *
     * @since 2.5.0
     * @param listener
     * @return Context
     * @throws ScriptRuntimeException
     * @throws ParseException
     */
    public Context debug(final BreakPointListener listener) {
        return debug(Vars.EMPTY, DiscardOut.INSTANCE, listener);
    }

    public void reset() {
        this.ast = null;
    }

    /**
     * Get the time that the template AST was last modified.
     *
     * @return the last modified time, measured in milliseconds
     */
    public long getLastModified() {
        final TemplateAST myAst = this.ast;
        return myAst != null ? myAst.getCreatedAt() : -1L;
    }

    /**
     * Get engine.
     *
     * @since 2.5.0
     * @return template engine
     */
    public Engine getEngine() {
        return engine;
    }

    /**
     * Get template name.
     *
     * @since 2.5.0
     * @return template name
     */
    public String getName() {
        return name;
    }

    /**
     * Get resource for this template.
     *
     * @since 2.5.0
     * @return resource
     */
    public Resource getResource() {
        return resource;
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || !(obj instanceof Template)) {
            return false;
        }
        Template other = (Template) obj;
        return this.engine == other.engine
                && this.name.equals(other.name);
    }

    private TemplateException completeException(final Exception exception) {
        return ((exception instanceof TemplateException)
                ? ((TemplateException) exception)
                : new ScriptRuntimeException(exception)).setTemplate(this);
    }
}
