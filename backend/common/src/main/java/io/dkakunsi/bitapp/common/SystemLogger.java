package io.dkakunsi.bitapp.common;

import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

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
      logger.debug(getMessage(message));
    }
  }

  public void debug(String format, Object... args) {
    if (logger.isDebugEnabled()) {
      logger.debug(getMessage(format, args));
    }
  }

  public void error(String message) {
    if (logger.isErrorEnabled()) {
      logger.error(getMessage(message));
    }
  }

  public void error(String message, Throwable ex) {
    if (logger.isErrorEnabled()) {
      logger.error(getMessage(message), ex);
    }
  }

  public void error(String format, Throwable ex, Object... args) {
    if (logger.isErrorEnabled()) {
      logger.error(getMessage(format, args), ex);
    }
  }

  public void error(String format, String arg) {
    if (logger.isErrorEnabled()) {
      logger.error(getMessage(format, arg));
    }
  }

  public void error(String format, Object... args) {
    if (logger.isErrorEnabled()) {
      logger.error(getMessage(format, args));
    }
  }

  public void info(String message) {
    if (logger.isInfoEnabled()) {
      logger.info(getMessage(message));
    }
  }

  public void info(String format, Object... args) {
    if (logger.isInfoEnabled()) {
      logger.info(getMessage(format, args));
    }
  }

  public void trace(String message) {
    if (logger.isTraceEnabled()) {
      logger.trace(getMessage(message));
    }
  }

  public void trace(String format, Object... args) {
    if (logger.isTraceEnabled()) {
      logger.trace(getMessage(format, args));
    }
  }

  public void warn(String message) {
    if (logger.isWarnEnabled()) {
      logger.warn(getMessage(message));
    }
  }

  public void warn(String format, Object... args) {
    if (logger.isWarnEnabled()) {
      logger.warn(getMessage(format, args));
    }
  }

  private String getMessage(String format, Object... args) {
    var formatter = MessageFormatter.arrayFormat(format, args);
    return getMessage(formatter.getMessage());
  }

  private String getMessage(String message) {
    var formatter = MessageFormatter.format("Request_ID: '{}'. {}", requestId(), message);
    return formatter.getMessage();
  }
}
