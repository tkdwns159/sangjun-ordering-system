package com.sangjun.common.domain;

import java.util.logging.Level;
import java.util.logging.Logger;

public class JavaUtilLogger {
    private final Logger logger;

    public JavaUtilLogger(String className) {
        this.logger = Logger.getLogger(className);
    }

    public void info(String message, Object... args) {
        logger.log(Level.INFO, message, args);
    }
}
