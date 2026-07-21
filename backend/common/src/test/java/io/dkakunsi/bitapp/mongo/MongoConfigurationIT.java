package io.dkakunsi.bitapp.mongo;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.dkakunsi.bitapp.Configuration;
import io.dkakunsi.bitapp.Configuration.EnvironmentConfiguration;
import io.dkakunsi.bitapp.test.MongoServer;

public class MongoConfigurationIT {

  private static Configuration configuration;

  @BeforeAll
  public static void startMongo() throws Exception {
    MongoServer.startDb();
    var mongodbConfig = MongoServer.getDbConfig();
    configuration = EnvironmentConfiguration.of(mongodbConfig::get);
  }

  @AfterAll
  public static void stopMongo() throws Exception {
    MongoServer.stopDb();
  }

  @Test
  public void givenEnvironmentVariablesWhenConfigurationCreatedThenShouldReturnValidDatabase() {
    // Given & When
    var underTest = new MongoConfiguration(configuration);
    var database = underTest.getDatabase();

    // Then
    assertNotNull(database);
    assertNotNull(database.getName());

    // Cleanup
    underTest.close();
  }

  @Test
  public void givenConfigurationWhenGetMongoClientThenShouldReturnValidClient() {
    // Given & When
    var underTest = new MongoConfiguration(configuration);
    var client = underTest.getMongoClient();

    // Then
    assertNotNull(client);

    // Test connection by getting database names
    var databaseNames = client.listDatabaseNames();
    assertNotNull(databaseNames);

    // Cleanup
    underTest.close();
  }
}