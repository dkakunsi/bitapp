package io.dkakunsi.bitapp;

import lombok.Getter;

public final record AppError(Code code, String message) {
  @Getter
  public static enum Code {
    SERVER_ERROR(500),
    BAD_REQUEST(400),
    UNAUTHORIZED(401),
    FORBIDDEN(403),
    NOT_FOUND(404),
    INTERNAL_ERROR(500);

    private int httpCode;

    private Code(int httpCode) {
      this.httpCode = httpCode;
    }
  }
}
