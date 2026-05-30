package io.dkakunsi.bitapp.common;

import java.util.NoSuchElementException;

import org.apache.commons.lang3.StringUtils;
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

}
