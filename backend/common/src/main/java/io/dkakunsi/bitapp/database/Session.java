package io.dkakunsi.bitapp.database;

public interface Session extends AutoCloseable {
  void commit();

  void rollback();

  void close();
}
