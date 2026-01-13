package io.dkakunsi.bitapp.common;

import org.apache.commons.lang3.StringUtils;

public interface Logger {

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

  default String getRequestId() {
    try {
      var requestId = Context.get().requestId();
      return StringUtils.isBlank(requestId) ? "NOT-SPECIFIED" : requestId;
    } catch (Exception ex) {
      return "NOT-SPECIFIED";
    }
  }
}
