package io.dkakunsi.bitapp.common;

import java.util.List;

public interface Validator {
  <T> List<Violation> validate(T input);

  public static record Violation(
      String field,
      String message) {

    @Override
    public final String toString() {
      return String.format("%s: %s", field, message);
    }
  }
}
