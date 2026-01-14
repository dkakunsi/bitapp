package io.dkakunsi.bitapp.test;

import java.util.Map;

import org.testcontainers.containers.MongoDBContainer;

public abstract class MongoServer {
  static final String MONGO_CONNECTION_STRING = "MONGO_CONNECTION_STRING";
  static final String MONGO_DATABASE = "MONGO_DATABASE";
  static final String MONGO_SECURE = "MONGO_SECURE";

  private static final String TEST_DATABASE = "testdb";
  private static MongoDBContainer mongoContainer = new MongoDBContainer("mongo:8.0");

  public static boolean isRunning() {
    return mongoContainer.isRunning();
  }

  public static boolean isNotRunning() {
    return !isRunning();
  }

  public static void startDb() throws Exception {
    if (!isRunning()) {
      mongoContainer.start();
    }
  }

  public static void stopDb() throws Exception {
    if (isRunning()) {
      mongoContainer.stop();
    }
  }

  public static Map<String, String> getDbConfig() throws Exception {
    while (!mongoContainer.isRunning()) {
      System.out.println("Waiting for MongoDB to start...");
      Thread.sleep(1000);
    }
    return Map.of(
        MONGO_CONNECTION_STRING, mongoContainer.getConnectionString(),
        MONGO_DATABASE, TEST_DATABASE,
        MONGO_SECURE, "false");
  }

  public static String getConnectionString() throws Exception {
    while (!mongoContainer.isRunning()) {
      System.out.println("Waiting for MongoDB to start...");
      Thread.sleep(1000);
    }
    return mongoContainer.getConnectionString();
  }
}
