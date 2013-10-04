package com.mikea.util;

import java.util.logging.Logger;

/**
 * @author mike.aizatsky@gmail.com
 */
public class Loggers {
    public static Logger getContextLogger() {
        return Logger.getLogger(getCallerClassName());
    }

    private static String getCallerClassName() {
        return findCaller(Loggers.class).getClassName();
    }

    private static StackTraceElement findCaller(Class<?> excludedClass) {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        for (StackTraceElement stackTraceElement : stackTraceElements) {
            if (stackTraceElement.getClassName().startsWith("java.lang")) {
                continue;
            }
            if (stackTraceElement.getClassName().equals(excludedClass.getName())) {
                continue;
            }
            return stackTraceElement;
        }
        throw new IllegalStateException();
    }


}
