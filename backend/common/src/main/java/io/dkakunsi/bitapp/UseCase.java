package io.dkakunsi.bitapp;

import java.util.NoSuchElementException;

import io.dkakunsi.bitapp.Logger.SystemLogger;


public interface UseCase<IN, OUT> {

  Logger LOGGER = SystemLogger.getLogger(UseCase.class);

  default Result<OUT> process(IN input) {
    try {
      if (input instanceof Validatable validatable) {
        validatable.validate();
      }
      return execute(input);
    } catch (IllegalArgumentException e) {
      LOGGER.info("Request failed with message: {}", e.getMessage());
      return Result.badRequest(e.getMessage());
    } catch (Exception e) {
      LOGGER.error("Request failed with message: {}", e.getMessage());
      return Result.failure(e);
    }
  }

  Result<OUT> execute(IN input);

  default Context getContext() {
    return Context.current();
  }

  default String getRequester() {
    try {
      return getContext().requester();
    } catch (NoSuchElementException e) {
      throw new RuntimeException("No requester found in context");
    }
  }
}
