package io.dkakunsi.bitapp;

public interface Session extends AutoCloseable {
  void commit();

  void rollback();

  void close();
}
