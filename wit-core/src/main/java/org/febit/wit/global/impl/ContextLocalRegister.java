// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.wit.global.impl;

import org.febit.wit.exceptions.ScriptRuntimeException;
import org.febit.wit.global.GlobalManager;
import org.febit.wit.global.GlobalRegister;
import org.febit.wit.lang.MethodDeclare;

/**
 *
 * @since 1.4.0
 * @author zqq90
 */
public class ContextLocalRegister implements GlobalRegister {

    protected String name = "LOCAL";

    @Override
    public void regist(GlobalManager manager) {
        manager.setConst(this.name, (MethodDeclare) (context, args) -> {
            final int i = args.length - 1;
            if (i == 0) {
                return context.getLocal(args[0]);
            }
            if (i < 0) {
                throw new ScriptRuntimeException("This function need at least 1 arg: ");
            }
            context.setLocal(args[0], args[1]);
            return args[1];
        });
    }
}
