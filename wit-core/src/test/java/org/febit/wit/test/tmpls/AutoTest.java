// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.wit.test.tmpls;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Map;
import java.util.TreeMap;
import org.febit.wit.Context;
import org.febit.wit.EngineManager;
import org.febit.wit.Template;
import org.febit.wit.exceptions.ParseException;
import org.febit.wit.exceptions.ResourceNotFoundException;
import org.febit.wit.exceptions.ScriptRuntimeException;
import org.febit.wit.io.Out;
import org.febit.wit.io.impl.DiscardOut;
import org.febit.wit.io.impl.OutputStreamOut;
import org.febit.wit.tools.testunit.AssertGlobalRegister;
import org.febit.wit.util.ClassUtil;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author zqq90
 */
public class AutoTest {

    private final static int BUFFER_SIZE = 1024;
    private final static String AUTO_TEST_PATH = "org/febit/wit/test/tmpls/auto/";

    private Map<String, String> collectAutoTestTemplates() throws IOException {
        final Map<String, String> templates = new TreeMap<>();

        ClassLoader classLoader = ClassUtil.getDefaultClassLoader();
        try {
            URL url = classLoader.getResource(AUTO_TEST_PATH);
            File file = new File(url.getFile());
            String[] files = file.list();
            for (int i = 0, len = files.length; i < len; i++) {
                String path = files[i];
                if (path.endsWith(".wit")) {
                    String outPath = path.concat(".out");
                    templates.put("/auto/".concat(path), new File(file, outPath).exists() ? outPath : null);
                }
            }
        } catch (Exception ex) {
            throw new IOException("Failed to load templates", ex);
        }
        return templates;
    }

    @Test
    public void test() throws ResourceNotFoundException, IOException, ParseException, ScriptRuntimeException {

        Map<String, String> templates = collectAutoTestTemplates();
        ClassLoader classLoader = ClassUtil.getDefaultClassLoader();

        final ByteArrayOutputStream bytesBuffer = new ByteArrayOutputStream();
        final byte[] buffer = new byte[BUFFER_SIZE];

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (Map.Entry<String, String> entry : templates.entrySet()) {
            String templatePath = entry.getKey();
            String outPath = entry.getValue();
            if (outPath != null) {
                out.reset();
                mergeTemplate(templatePath, out);

                //read outNotNull 
                InputStream in = classLoader.getResourceAsStream(AUTO_TEST_PATH.concat(outPath));
                if (in != null) {
                    int read;
                    bytesBuffer.reset();
                    while ((read = in.read(buffer, 0, BUFFER_SIZE)) >= 0) {
                        bytesBuffer.write(buffer, 0, read);
                    }
                    assertArrayEquals(bytesBuffer.toByteArray(), out.toByteArray());
                    System.out.println("\tresult match to: " + outPath);
                    bytesBuffer.reset();
                }
                out.reset();
            } else {
                mergeTemplate(templatePath);
            }
        }
    }

    public void mergeTemplate(String templatePath) throws ResourceNotFoundException {
        mergeTemplate(templatePath, DiscardOut.INSTANCE);
    }

    public void mergeTemplate(String templatePath, OutputStream out) throws ResourceNotFoundException {
        mergeTemplate(templatePath, new OutputStreamOut(out, EngineManager.getEngine()));
    }

    public void mergeTemplate(String templatePath, Out out) throws ResourceNotFoundException {
        System.out.println("AUTO RUN: " + templatePath);
        Template template = EngineManager.getEngine().getTemplate(templatePath);
        try {
            Context context = template.merge(out);
            System.out.println("\tassert count: " + context.getLocal(AssertGlobalRegister.ASSERT_COUNT_KEY));
        } catch (ScriptRuntimeException e) {
            throw e;
        }
    }
}
