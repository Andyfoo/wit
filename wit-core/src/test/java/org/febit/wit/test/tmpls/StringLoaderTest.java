/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.febit.wit.test.tmpls;

import java.io.StringWriter;
import org.junit.Assert;
import org.junit.Test;
import org.febit.wit.Engine;
import org.febit.wit.exceptions.ResourceNotFoundException;

/**
 *
 * @author zqq90
 */
public class StringLoaderTest {

    @Test
    public void test() throws ResourceNotFoundException {

        final StringWriter writer = new StringWriter();

        Engine.create("")
                .getTemplate("string:<% echo \"Hello Wit！\"; %>")
                .merge(writer);
        Assert.assertEquals("Hello Wit！", writer.toString());
    }
}