// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.wit.core;

final class Symbol {

    final int id;
    final int line;
    final int column;
    final Object value;

    /**
     * The parse state.
     */
    int state;
    boolean isOnEdgeOfNewLine = false;

    Symbol(int id, Object value, Symbol sym) {
        this(id, sym.line, sym.column, value);
    }

    Symbol(int id, int line, int column, Object value) {
        this.id = id;
        this.line = line;
        this.column = column;
        this.value = value;
    }
}
