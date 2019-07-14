// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.wit.core.text.impl;

import lombok.val;
import org.febit.wit.Template;
import org.febit.wit.core.ast.Statement;
import org.febit.wit.core.text.TextStatementFactory;
import org.febit.wit.exceptions.ScriptRuntimeException;
import org.febit.wit.io.charset.CoderFactory;
import org.febit.wit.io.charset.Encoder;
import org.febit.wit.util.InternedEncoding;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author zqq90
 */
public class ByteArrayTextStatementFactory implements TextStatementFactory {

    protected InternedEncoding encoding;
    protected CoderFactory coderFactory;
    protected final ThreadLocal<Encoder> encoders = new ThreadLocal<>();
    protected final ThreadLocal<ByteArrayOutputStream> outputs = new ThreadLocal<>();

    @Override
    public void startTemplateParser(Template template) {
        encoders.set(coderFactory.newEncoder(encoding));
        outputs.set(new ByteArrayOutputStream(512));
    }

    @Override
    public void finishTemplateParser(Template template) {
        encoders.remove();
        outputs.remove();
    }

    protected byte[] getBytes(char[] text) {
        try {
            val out = outputs.get();
            encoders.get().write(text, 0, text.length, out);
            final byte[] bytes = out.toByteArray();
            out.reset();
            return bytes;
        } catch (IOException ex) {
            throw new ScriptRuntimeException(ex);
        }
    }

    @Override
    public Statement getTextStatement(Template template, char[] text, int line, int column) {
        return new ByteArrayTextStatement(getBytes(text), line, column);
    }
}
