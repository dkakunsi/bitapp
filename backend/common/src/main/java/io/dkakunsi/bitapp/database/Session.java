package io.dkakunsi.bitapp.database;

public interface Session<T> {
  void commit();

  void rollback();

  void close();

  T getSessionObject();
}
