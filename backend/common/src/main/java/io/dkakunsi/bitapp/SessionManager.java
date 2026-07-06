package io.dkakunsi.bitapp;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Supplier;

public interface SessionManager {
  ScopedValue<Session> SESSION = ScopedValue.newInstance();

  static Optional<Session> getCurrentSession() {
    try {
      return Optional.of(SessionManager.SESSION.get());
    } catch (NoSuchElementException _) {
      return Optional.empty();
    }
  }

  <T> Result<T> executeInSession(Supplier<Result<T>> function);
}
