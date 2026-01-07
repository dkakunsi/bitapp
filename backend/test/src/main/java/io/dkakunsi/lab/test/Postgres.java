package io.dkakunsi.lab.test;

import java.util.Map;

import org.testcontainers.containers.PostgreSQLContainer;

public abstract class Postgres {
  static final String POSTGRES_HOST = "postgres.host";
  static final String POSTGRES_PORT = "postgres.port";
  static final String POSTGRES_DBNAME = "postgres.dbname";
  static final String POSTGRES_USERNAME = "postgres.username";
  static final String POSTGRES_PASSWORD = "postgres.password";

  private static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
      "postgres:16-alpine");

  public static boolean isRunning() {
    return postgres.isRunning();
  }

  public static boolean isNotRunning() {
    return !isRunning();
  }

  public static void startDb() throws Exception {
    if (!isRunning()) {
      postgres.start();
    }
  }

  public static void stopDb() throws Exception {
    if (isRunning()) {
      postgres.stop();
    }
  }

  public static Map<String, String> getDbConfig() throws Exception {
    while (!postgres.isRunning()) {
      System.out.println("Waiting for Postgres to start...");
      Thread.sleep(1000);
    }
    return Map.of(
        POSTGRES_HOST, postgres.getHost(),
        POSTGRES_PORT, postgres.getFirstMappedPort().toString(),
        POSTGRES_DBNAME, postgres.getDatabaseName(),
        POSTGRES_USERNAME, postgres.getUsername(),
        POSTGRES_PASSWORD, postgres.getPassword());
  }
}
