package io.dkakunsi.bitapp.domain.usecase;

import java.util.Optional;

import io.dkakunsi.bitapp.common.AppError;
import io.dkakunsi.bitapp.common.AppError.Code;

public final record Result<DATA>(
    Optional<DATA> data,
    Optional<AppError> error,
    Optional<String> message) {

  public boolean isSuccess() {
    return error.isEmpty();
  }

  public boolean isFailed() {
    return !isSuccess();
  }

  public boolean isEmpty() {
    return data.isEmpty();
  }

  public static <DATA> Result<DATA> success() {
    return new Result<>(Optional.empty(), Optional.empty(), Optional.empty());
  }

  public static <DATA> Result<DATA> success(DATA data) {
    return new Result<>(Optional.of(data), Optional.empty(), Optional.empty());
  }

  public static <DATA> Result<DATA> success(String message) {
    return new Result<>(Optional.empty(), Optional.empty(), Optional.of(message));
  }

  public static <DATA> Result<DATA> failure(Code serverError, String message) {
    final var error = new AppError(serverError, message);
    return new Result<>(Optional.empty(), Optional.of(error), Optional.empty());
  }

}
