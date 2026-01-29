package io.dkakunsi.bitapp.database;

import java.util.function.Supplier;

import io.dkakunsi.bitapp.domain.usecase.Result;

public interface SessionManager {
  ScopedValue<Session> SESSION = ScopedValue.newInstance();

  <T> Result<T> executeInSession(Supplier<Result<T>> function);
}
