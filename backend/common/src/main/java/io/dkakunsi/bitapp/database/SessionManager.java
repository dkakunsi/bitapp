package io.dkakunsi.bitapp.database;

public interface SessionManager {
  ScopedValue<Session> SESSION = ScopedValue.newInstance();

  Session createSession();
}
