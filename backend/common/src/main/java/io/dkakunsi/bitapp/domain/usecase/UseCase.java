package io.dkakunsi.bitapp.domain.usecase;

import java.util.NoSuchElementException;

import io.dkakunsi.bitapp.common.AppError.Code;
import io.dkakunsi.bitapp.common.Context;
import io.dkakunsi.bitapp.common.Logger;
import io.dkakunsi.bitapp.common.SystemLogger;
import io.dkakunsi.bitapp.common.Validatable;

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
      return Result.failure(Code.BAD_REQUEST, e.getMessage());
    } catch (Exception e) {
      LOGGER.error("Request failed with message: {}", e.getMessage());
      return Result.failure(Code.SERVER_ERROR, e.getMessage());
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
