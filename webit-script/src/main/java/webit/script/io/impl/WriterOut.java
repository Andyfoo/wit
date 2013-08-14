package webit.script.io.impl;

import java.io.IOException;
import java.io.Writer;
import webit.script.exceptions.ScriptRuntimeException;
import webit.script.io.Out;

/**
 *
 * @author Zqq
 */
public final class WriterOut implements Out {

    private final Writer writer;
    private final String encoding;

    public WriterOut(Writer writer, String encoding) {
        this.writer = writer;
        this.encoding = encoding;
    }

    public void write(byte[] bytes) {
        try {
            writer.write(new String(bytes, encoding));
        } catch (IOException ex) {
            throw new ScriptRuntimeException(ex);
        }
    }

    public void write(String string) {
        try {
            writer.write(string);
        } catch (IOException ex) {
            throw new ScriptRuntimeException(ex);
        }
    }

    public String getEncoding() {
        return encoding;
    }
}
