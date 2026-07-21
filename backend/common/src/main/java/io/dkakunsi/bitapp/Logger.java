package io.dkakunsi.bitapp;

import java.util.NoSuchElementException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

public interface Logger {

  String NOT_SPECIFIED = "NOT-SPECIFIED";

  String REQUEST_ID = "Request_Id";

  void debug(String message);

  void debug(String format, Object... args);

  void error(String message);

  void error(String message, Throwable ex);

  void error(String format, Throwable ex, Object... args);

  void error(String format, String arg);

  void error(String format, Object... args);

  void info(String message);

  void info(String format, Object... args);

  void trace(String message);

  void trace(String format, Object... args);

  void warn(String message);

  void warn(String format, Object... args);

  default String requestId() {
    try {
      var requestId = Context.current().requestId();
      return StringUtils.isBlank(requestId) ? NOT_SPECIFIED : requestId;
    } catch (NoSuchElementException _) {
      return NOT_SPECIFIED;
    }
  }

  default String buildMessage(String format, Object... args) {
    var formatter = MessageFormatter.arrayFormat(format, args);
    return addRequestIdIfAvailable(formatter.getMessage());
  }

  default String addRequestIdIfAvailable(String message) {
    var requestId = requestId();
    if (NOT_SPECIFIED.equals(requestId)) {
      return message;
    }
    var args = new Object[] { REQUEST_ID, requestId, message };
    var formatter = MessageFormatter.arrayFormat("{}: '{}'. {}", args);
    return formatter.getMessage();
  }

  public static final class SystemLogger implements Logger {

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

}
