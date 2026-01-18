package io.dkakunsi.bitapp.common.usecase;

import io.dkakunsi.bitapp.common.AppError.Code;
import io.dkakunsi.bitapp.common.Context;
import io.dkakunsi.bitapp.common.Validatable;

public interface UseCase<IN, OUT> {
  default Result<OUT> process(Context context, IN input) {
    try {
      if (input instanceof Validatable validatable) {
        validatable.validate();
      }
      return execute(context, input);
    } catch (IllegalArgumentException e) {
      return Result.failure(Code.BAD_REQUEST, e.getMessage());
    } catch (Exception e) {
      return Result.failure(Code.SERVER_ERROR, e.getMessage());
    }
  }

  Result<OUT> execute(Context context, IN input);
}
