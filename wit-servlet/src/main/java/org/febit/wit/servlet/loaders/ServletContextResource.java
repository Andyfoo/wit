// Copyright (c) 2013-2016, febit.org. All Rights Reserved.
package org.febit.wit.servlet.loaders;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import javax.servlet.ServletContext;
import org.febit.wit.exceptions.ResourceNotFoundException;
import org.febit.wit.loaders.Resource;

/**
 *
 * @author zqq90
 */
public class ServletContextResource implements Resource {

    private final String path;
    private final String encoding;
    private final ServletContext servletContext;

    public ServletContextResource(String path, String encoding, ServletContext servletContext) {
        this.path = path;
        this.encoding = encoding;
        this.servletContext = servletContext;
    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public Reader openReader() throws IOException {
        final InputStream in = servletContext.getResourceAsStream(path);
        if (in != null) {
            return new InputStreamReader(in, encoding);
        }
        throw new ResourceNotFoundException("Resource Not Found: ".concat(path));
    }

    /**
     * @since 1.4.1
     */
    @Override
    public boolean exists() {
        try {
            if (servletContext.getResource(path) != null) {
                return true;
            }
        } catch (Exception ignore) {
        }
        return false;
    }
}