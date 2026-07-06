package io.dkakunsi.bitapp;

import org.slf4j.LoggerFactory;

public final class SystemLogger implements Logger {

  private final org.slf4j.Logger logger;

  private SystemLogger(Class<?> clazz) {
    logger = LoggerFactory.getLogger(clazz);
  }

  public static SystemLogger getLogger(Class<?> clazz) {
    return new SystemLogger(clazz);
  }

  public void debug(String message) {
    if (logger.isDebugEnabled()) {
      logger.debug(addRequestIdIfAvailable(message));
    }
  }

  public void debug(String format, Object... args) {
    if (logger.isDebugEnabled()) {
      logger.debug(buildMessage(format, args));
    }
  }

  public void error(String message) {
    if (logger.isErrorEnabled()) {
      logger.error(addRequestIdIfAvailable(message));
    }
  }

  public void error(String message, Throwable ex) {
    if (logger.isErrorEnabled()) {
      logger.error(addRequestIdIfAvailable(message), ex);
    }
  }

  public void error(String format, Throwable ex, Object... args) {
    if (logger.isErrorEnabled()) {
      logger.error(buildMessage(format, args), ex);
    }
  }

  public void error(String format, String arg) {
    if (logger.isErrorEnabled()) {
      logger.error(buildMessage(format, arg));
    }
  }

  public void error(String format, Object... args) {
    if (logger.isErrorEnabled()) {
      logger.error(buildMessage(format, args));
    }
  }

  public void info(String message) {
    if (logger.isInfoEnabled()) {
      logger.info(addRequestIdIfAvailable(message));
    }
  }

  public void info(String format, Object... args) {
    if (logger.isInfoEnabled()) {
      logger.info(buildMessage(format, args));
    }
  }

  public void trace(String message) {
    if (logger.isTraceEnabled()) {
      logger.trace(addRequestIdIfAvailable(message));
    }
  }

  public void trace(String format, Object... args) {
    if (logger.isTraceEnabled()) {
      logger.trace(buildMessage(format, args));
    }
  }

  public void warn(String message) {
    if (logger.isWarnEnabled()) {
      logger.warn(addRequestIdIfAvailable(message));
    }
  }

  public void warn(String format, Object... args) {
    if (logger.isWarnEnabled()) {
      logger.warn(buildMessage(format, args));
    }
  }
}
