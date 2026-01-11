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

  @Override
  public final boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (other == null || getClass() != other.getClass()) {
      return false;
    }
    Id id = (Id) other;
    return value.equals(id.value);
  }

  public final boolean equals(String value) {
    return this.value.equals(value);
  }
}
