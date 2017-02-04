// Copyright (c) 2013-2016, febit.org. All Rights Reserved.
package org.febit.wit.loaders.impl;

import org.febit.wit.loaders.Loader;
import org.febit.wit.loaders.Resource;
import org.febit.wit.loaders.impl.resources.StringResource;

/**
 *
 * @author zqq90
 */
public class StringLoader implements Loader {

    protected boolean enableCache;

    @Override
    public Resource get(String name) {
        return new StringResource(name);
    }

    @Override
    public String concat(String parent, String name) {
        //ignore parent
        return name;
    }

    @Override
    public String normalize(String name) {
        return name;
    }

    @Override
    public boolean isEnableCache(String name) {
        return enableCache;
    }
}