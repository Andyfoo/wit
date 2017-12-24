// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.wit.test.tmpls;

import java.util.HashSet;
import java.util.Set;
import org.febit.wit.EngineManager;
import org.febit.wit.Template;
import org.febit.wit.Vars;
import org.febit.wit.exceptions.ResourceNotFoundException;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author zqq90
 */
public class DebugTest {

    private final Set<Object> labelCache = new HashSet<>();
    private int pointCount = 0;

    @Test
    public void test() throws ResourceNotFoundException {
        Template template = EngineManager.getTemplate("/debug.wit");

        template.merge();

        labelCache.clear();
        pointCount = 0;
        template.debug(Vars.EMPTY, (label, context, statement, result) -> {
            labelCache.add(label);
            pointCount++;
        });

        assertEquals(18, pointCount);
        assertTrue(labelCache.contains(null));
        assertTrue(labelCache.contains("p1"));
        assertTrue(labelCache.contains("p2"));
        assertTrue(labelCache.contains("p3"));
        assertTrue(labelCache.contains("p4"));

    }

}
