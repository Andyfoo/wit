// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.wit.security;

import org.febit.wit.security.impl.DefaultNativeSecurityManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author zqq90
 */
class DefaultNativeSecurityManagerTest {

    @Test
    void test() {

        String list = "-a"
                + ",a"
                + ",+b"
                + "\r\n-b"
                + ", c"
                + "\r-c.d "
                + ", c.d.e.f"
                + ",-c.d.e.f.g";

        DefaultNativeSecurityManager manager = new DefaultNativeSecurityManager();

        manager.setList(list);
        manager.init();

        assertFalse(manager.access("a"));
        assertFalse(manager.access("a.aa"));
        assertFalse(manager.access("a.aa.aaa"));

        assertFalse(manager.access("b"));
        assertFalse(manager.access("b.b"));
        assertFalse(manager.access("b.c"));

        assertTrue(manager.access("c"));
        assertTrue(manager.access("c.cc"));
        assertTrue(manager.access("c.cc.ccc"));
        assertFalse(manager.access("c.d"));
        assertFalse(manager.access("c.d.e"));
        assertTrue(manager.access("c.d.e.f"));
        assertFalse(manager.access("c.d.e.f.g"));
        assertFalse(manager.access("c.d.e.f.g.h"));

    }
}
