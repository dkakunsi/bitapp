package io.dkakunsi.bitapp;

import java.util.Optional;

import lombok.Getter;

public final record Result<DATA>(
    Optional<DATA> data,
    Optional<ErrorCode> errorCode,
    Optional<String> errorMessage) {

  public static final String DEFAULT_ERROR_MESSAGE = "Unknown error";

  public boolean isSuccess() {
    return errorCode.isEmpty();
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

  public static <DATA> Result<DATA> failure(Result<?> other) {
    final var errorCode = other.errorCode().orElse(ErrorCode.INTERNAL_ERROR);
    final var errorMessage = other.errorMessage().orElse(DEFAULT_ERROR_MESSAGE);
    return failure(errorCode, errorMessage);
  }

  public static <DATA> Result<DATA> failure(Exception exception) {
    final var message = exception.getMessage() != null
        ? exception.getMessage()
        : DEFAULT_ERROR_MESSAGE;
    return failure(ErrorCode.INTERNAL_ERROR, message);
  }

  public static <DATA> Result<DATA> badRequest(String message) {
    return failure(ErrorCode.BAD_REQUEST, message);
  }

  public static <DATA> Result<DATA> forbidden(String message) {
    return failure(ErrorCode.FORBIDDEN, message);
  }

  public static <DATA> Result<DATA> notFound(String message) {
    return failure(ErrorCode.NOT_FOUND, message);
  }

  public static <DATA> Result<DATA> internalError(String message) {
    return failure(ErrorCode.INTERNAL_ERROR, message);
  }

  private static <DATA> Result<DATA> failure(ErrorCode errorCode, String errorMessage) {
    return new Result<>(Optional.empty(), Optional.of(errorCode), Optional.of(errorMessage));
  }

  @Getter
  public static enum ErrorCode {
    BAD_REQUEST(400),
    FORBIDDEN(403),
    NOT_FOUND(404),
    INTERNAL_ERROR(500);

    private int httpCode;

    private ErrorCode(int httpCode) {
      this.httpCode = httpCode;
    }
  }
}
