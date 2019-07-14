// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.wit.loggers.impl;

import org.febit.wit.Init;
import org.febit.wit.loggers.AbstractLogger;

import java.io.PrintStream;
import java.util.Locale;

/**
 * @author zqq90
 */
@SuppressWarnings({
        "WeakerAccess"
})
public class SimpleLogger extends AbstractLogger {

    @SuppressWarnings({
            "squid:S106" // Standard outputs should not be used directly to log anything
    })
    private static final PrintStream OUT = System.out;

    //settings
    protected String level = "info";

    private String prefix;
    private int levelNum;

    @Init
    public void init() {
        prefix = '[' + name + "] ";
        switch (level.trim().toLowerCase(Locale.US)) {
            case "error":
                levelNum = LEVEL_ERROR;
                break;
            case "warn":
                levelNum = LEVEL_WARN;
                break;
            case "info":
                levelNum = LEVEL_INFO;
                break;
            case "debug":
                levelNum = LEVEL_DEBUG;
                break;
            case "off":
            default:
                levelNum = Integer.MAX_VALUE;
        }
    }

    @Override
    protected boolean isEnabled(int level) {
        return level >= this.levelNum;
    }

    @Override
    protected void log(int level, String msg) {
        printLog(level, msg, null);
    }

    @Override
    protected void log(int level, String msg, Throwable throwable) {
        printLog(level, msg, throwable);
    }

    private void printLog(int level, String msg, Throwable throwable) {
        if (!isEnabled(level)) {
            return;
        }
        OUT.println(prefix.concat(msg != null ? msg : ""));
        if (throwable != null) {
            throwable.printStackTrace(OUT);
        }
    }
}
