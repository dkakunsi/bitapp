package io.dkakunsi.lab.test;

import java.util.Map;

import org.testcontainers.containers.PostgreSQLContainer;

public abstract class Postgres {
  static final String POSTGRES_HOST = "postgres.host";
  static final String POSTGRES_PORT = "postgres.port";
  static final String POSTGRES_DBNAME = "postgres.dbname";
  static final String POSTGRES_USERNAME = "postgres.username";
  static final String POSTGRES_PASSWORD = "postgres.password";

  private static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>(
      "postgres:16-alpine");

  public static boolean isRunning() {
    return postgresContainer.isRunning();
  }

  public static boolean isNotRunning() {
    return !isRunning();
  }

  public static void startDb() throws Exception {
    if (!isRunning()) {
      postgresContainer.start();
    }
  }

  public static void stopDb() throws Exception {
    if (isRunning()) {
      postgresContainer.stop();
    }
  }

  public static Map<String, String> getDbConfig() throws Exception {
    while (!postgresContainer.isRunning()) {
      System.out.println("Waiting for Postgres to start...");
      Thread.sleep(1000);
    }
    return Map.of(
        POSTGRES_HOST, postgresContainer.getHost(),
        POSTGRES_PORT, postgresContainer.getFirstMappedPort().toString(),
        POSTGRES_DBNAME, postgresContainer.getDatabaseName(),
        POSTGRES_USERNAME, postgresContainer.getUsername(),
        POSTGRES_PASSWORD, postgresContainer.getPassword());
  }
}
