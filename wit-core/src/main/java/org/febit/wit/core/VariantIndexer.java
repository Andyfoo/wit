// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.wit.core;

import org.febit.wit.util.ArrayUtil;

/**
 *
 * @author zqq90
 */
public final class VariantIndexer {

    public static final VariantIndexer EMPTY = new VariantIndexer(0, null, ArrayUtil.emptyStrings(), null);

    public final int id;
    public final VariantIndexer parent;
    public final String[] names;
    public final int[] indexs;

    VariantIndexer(int id, VariantIndexer parent, String[] names, int[] indexs) {
        this.id = id;
        this.parent = parent;
        this.names = names;
        this.indexs = indexs;
    }

    public int getCurrentIndex(final String name) {
        final String[] myNames = this.names;
        int i = myNames.length;
        while (i != 0) {
            --i;
            if (myNames[i].equals(name)) {
                return indexs[i];
            }
        }
        return -1;
    }

    public int getIndex(final int level, final String name) {
        if (level == id) {
            return getCurrentIndex(name);
        }
        return -1;
    }

    public int getIndex(final String name) {
        final String[] myNames = this.names;
        int i = myNames.length;
        while (i != 0) {
            --i;
            if (myNames[i].equals(name)) {
                return indexs[i];
            }
        }
        if (this.parent != null) {
            return parent.getIndex(name);
        }
        return -1;
    }

    public String getName(int index) {
        return this.names[index];
    }
}
