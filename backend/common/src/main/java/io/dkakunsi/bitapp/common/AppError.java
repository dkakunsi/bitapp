package io.dkakunsi.bitapp.common;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

public final record AppError(@NotNull Code code, @NotBlank String message) {
  @Getter
  public static enum Code {
    SERVER_ERROR(500), BAD_REQUEST(400), NOT_FOUND(404);

    private int httpCode;

    private Code(int httpCode) {
      this.httpCode = httpCode;
    }
  }
}
