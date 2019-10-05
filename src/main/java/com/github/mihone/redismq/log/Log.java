package com.github.mihone.redismq.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Log {
    private Logger logger;


    public void info(String s, Object... objects) {
        if (logger.isInfoEnabled()) {
            logger.info(s, objects);
        }
    }

    public void info(String s) {
        if (logger.isInfoEnabled()) {
            logger.info(s);
        }
    }

    public void error(String s, Object... objects) {
        if (logger.isInfoEnabled()) {
            logger.info(s, objects);
        }
    }

    public void error(String s) {
        if (logger.isErrorEnabled()) {
            logger.error(s);
        }
    }

    public void debug(String s, Object... objects) {
        if (logger.isDebugEnabled()) {
            logger.debug(s, objects);
        }
    }

    public void debug(String s) {
        if (logger.isDebugEnabled()) {
            logger.debug(s);
        }
    }

    public void warn(String s, Object... objects) {
        if (logger.isWarnEnabled()) {
            logger.warn(s, objects);
        }
    }

    public void warn(String s) {
        if (logger.isWarnEnabled()) {
            logger.warn(s);
        }
    }

    public void trace(String s, Object... objects) {
        if (logger.isTraceEnabled()) {
            logger.trace(s, objects);
        }
    }

    public void trace(String s) {
        if (logger.isTraceEnabled()) {
            logger.trace(s);
        }
    }


    public static Log getLogger(Class<?> clazz) {
        Log log = new Log();
        log.logger = LoggerFactory.getLogger(clazz);
        return log;
    }

    private Log() {
    }
}
