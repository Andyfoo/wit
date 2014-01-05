// Copyright (c) 2013-2014, Webit Team. All Rights Reserved.
package webit.script;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Map;
import java.util.TreeMap;
import static org.junit.Assert.assertArrayEquals;
import org.junit.Test;
import webit.script.exceptions.ParseException;
import webit.script.exceptions.ResourceNotFoundException;
import webit.script.exceptions.ScriptRuntimeException;
import webit.script.global.AssertGlobalRegister;
import webit.script.test.util.DiscardOutputStream;
import webit.script.util.ClassLoaderUtil;
import webit.script.util.FastByteArrayOutputStream;
import webit.script.util.FastByteBuffer;
import webit.script.util.StringUtil;

/**
 *
 * @author Zqq
 */
public class AutoTest {

    private final static int BUFFER_SIZE = 1024;
    private final static String AUTO_TEST_PATH = "webit/script/test/tmpls/auto/";

    private Map<String, String> collectAutoTestTemplates() {
        final Map<String, String> templates = new TreeMap<String, String>();

        ClassLoader classLoader = ClassLoaderUtil.getDefaultClassLoader();
        try {
            URL url = classLoader.getResource(AUTO_TEST_PATH);
            File file = new File(url.getFile());
            String[] files = file.list();
            for (int i = 0, len = files.length; i < len; i++) {
                String path = files[i];
                if (StringUtil.endsWithIgnoreCase(path, ".wit")) {
                    String outPath = path.concat(".out");
                    templates.put("/auto/".concat(path), new File(file, outPath).exists() ? outPath : null);
                }
            }
        } catch (Exception ex) {
            //ignore
        }
        return templates;
    }

    @Test
    public void test() throws ResourceNotFoundException, IOException {

        Map<String, String> templates = collectAutoTestTemplates();
        ClassLoader classLoader = ClassLoaderUtil.getDefaultClassLoader();

        final FastByteBuffer bytesBuffer = new FastByteBuffer();
        final byte[] buffer = new byte[BUFFER_SIZE];

        try {

            FastByteArrayOutputStream out = new FastByteArrayOutputStream();
            for (Map.Entry<String, String> entry : templates.entrySet()) {
                String templatePath = entry.getKey();
                String outPath = entry.getValue();
                if (outPath != null) {
                    out.reset();
                    mergeTemplate(templatePath, out);

                    //read out 
                    InputStream in;
                    if ((in = classLoader.getResourceAsStream(AUTO_TEST_PATH.concat(outPath))) != null) {
                        int read;
                        bytesBuffer.clear();
                        while ((read = in.read(buffer, 0, BUFFER_SIZE)) >= 0) {
                            bytesBuffer.append(buffer, 0, read);
                        }
                        assertArrayEquals(bytesBuffer.toArray(), out.toByteArray());
                        System.out.println("\tresult match to: " + outPath);

                        bytesBuffer.clear();
                    }
                    out.reset();
                } else {
                    mergeTemplate(templatePath, new DiscardOutputStream());
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
            throw e;
        } catch (ScriptRuntimeException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public void mergeTemplate(String templatePath, OutputStream out) throws ResourceNotFoundException {

        System.out.println("AUTO RUN: " + templatePath);
        Template template = EngineManager.getEngine().getTemplate(templatePath);
        AssertGlobalRegister.instance.resetAssertCount();
        template.merge(out);

        System.out.println("\tassert count: " + AssertGlobalRegister.instance.getAssertCount());
    }
}
