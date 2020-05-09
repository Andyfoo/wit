// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.wit.loaders.impl;

import lombok.val;
import org.febit.wit.Engine;
import org.febit.wit.Init;
import org.febit.wit.exceptions.IllegalConfigException;
import org.febit.wit.loaders.Loader;
import org.febit.wit.loaders.Resource;
import org.febit.wit.util.StringUtil;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

/**
 * @author zqq90
 * @since 1.4.0
 */
@SuppressWarnings({
        "WeakerAccess"
})
public class RouteLoader implements Loader {

    protected String loaders;
    protected Loader defaultLoader;

    protected LoaderEntry[] entries;
    protected String[] rules;

    @Init
    public void init(final Engine engine) {
        //init Route rules
        val raws = StringUtil.toArray(this.loaders);
        val size = raws.length;
        val prefixes = new String[size];
        val loaderMap = new HashMap<String, LoaderEntry>();
        for (int i = 0; i < size; i++) {
            val raw = raws[i];
            val index = raw.indexOf(' ');
            if (index < 0) {
                throw new IllegalConfigException("Illegal RouteLoader rule: ".concat(raw));
            }
            val rule = raw.substring(0, index);
            prefixes[i] = rule;
            loaderMap.put(rule, new LoaderEntry(rule,
                    (Loader) engine.get(raw.substring(index + 1).trim())));
        }
        Arrays.sort(prefixes, Comparator.reverseOrder());
        val loaderEntries = new LoaderEntry[size];
        for (int i = 0; i < size; i++) {
            loaderEntries[i] = loaderMap.get(prefixes[i]);
        }
        this.rules = prefixes;
        this.entries = loaderEntries;
        //default Loader
    }

    protected LoaderEntry getLoaderEntry(String resourceName) {
        val prefixes = this.rules;
        for (int i = 0, len = prefixes.length; i < len; i++) {
            if (resourceName.startsWith(prefixes[i])) {
                return this.entries[i];
            }
        }
        return null;
    }

    @Override
    public Resource get(String name) {
        val entry = getLoaderEntry(name);
        if (entry != null) {
            return entry.get(name);
        }
        return this.defaultLoader.get(name);
    }

    @Override
    public String concat(String parent, String name) {
        val entry = getLoaderEntry(name);
        if (entry != null) {
            return entry.normalize(name);
        }
        val parentEntry = getLoaderEntry(parent);
        if (parentEntry != null) {
            return parentEntry.concat(parent, name);
        }
        return this.defaultLoader.concat(parent, name);
    }

    @Override
    public String normalize(String name) {
        val entry = getLoaderEntry(name);
        if (entry != null) {
            return entry.normalize(name);
        }
        return this.defaultLoader.normalize(name);
    }

    @Override
    public boolean isEnableCache(String name) {
        val entry = getLoaderEntry(name);
        if (entry != null) {
            return entry.isEnableCache(name);
        }
        return this.defaultLoader.isEnableCache(name);
    }

    protected static class LoaderEntry {

        private final String prefix;
        private final Loader loader;
        private final int prefixLength;

        public LoaderEntry(String prefix, Loader loader) {
            this.prefix = prefix;
            this.loader = loader;
            this.prefixLength = prefix.length();
        }

        public Resource get(String name) {
            return this.loader.get(name.substring(this.prefixLength));
        }

        public String concat(String parent, String name) {
            return fix(this.loader.concat(parent.substring(this.prefixLength), name));
        }

        public String normalize(String name) {
            return fix(this.loader.normalize(name.substring(this.prefixLength)));
        }

        public String fix(final String name) {
            if (name != null) {
                return this.prefix.concat(name);
            }
            return null;
        }

        private boolean isEnableCache(String name) {
            return this.loader.isEnableCache(name.substring(this.prefixLength));
        }
    }
}
