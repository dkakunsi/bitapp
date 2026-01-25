package io.dkakunsi.bitapp.database;

public interface SessionManager<T> {
  Session<T> createSession();
}
