package io.dkakunsi.bitapp.common;

import java.util.UUID;

public final record Id(String value) {
  public static Id of(String value) {
    return new Id(value);
  }

  public static Id generate() {
    final var uuid = UUID.randomUUID().toString();
    return Id.of(uuid);
  }
}
