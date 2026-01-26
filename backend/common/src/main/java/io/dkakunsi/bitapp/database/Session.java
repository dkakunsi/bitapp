package io.dkakunsi.bitapp.database;

public interface Session {
  void commit();

  void rollback();

  void close();
}
