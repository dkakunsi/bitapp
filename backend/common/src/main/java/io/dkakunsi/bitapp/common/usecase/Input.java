package io.dkakunsi.bitapp.common.usecase;

import io.dkakunsi.bitapp.common.Context;
import jakarta.validation.constraints.NotNull;

public final record Input<DATA>(@NotNull DATA data, @NotNull Context context) {
  public String requester() {
    return context.requester();
  }
}
