package io.dkakunsi.bitapp.mongo;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;

import org.junit.jupiter.api.Test;

import io.dkakunsi.bitapp.Configuration.EnvironmentConfiguration;

public class MongoConfigurationTest {

  @Test
  public void givenConfigurationWhenCloseThenShouldCloseClient() {
    // Given
    var testConfig = EnvironmentConfiguration.of(key -> Map.of(
        "MONGO_CONNECTION_STRING", "mongodb://localhost:27017",
        "MONGO_DATABASE", "dev").get(key));
    var underTest = new MongoConfiguration(testConfig);

    // When
    assertDoesNotThrow(underTest::close);

    // Then
    assertNotNull(underTest);
  }

  @Test
  public void givenSrvConnectionWhenAuthSourceNotSetThenShouldUseAdminSource() {
    // Given
    var testConfig = EnvironmentConfiguration.of(key -> Map.of(
        "MONGO_CONNECTION_STRING", "mongodb+srv://cluster0.example.mongodb.net/",
        "MONGO_DATABASE", "dev").get(key));
    var underTest = new MongoConfiguration(testConfig);

    // When
    var authSource = underTest.getAuthenticationDatabase("mongodb+srv://cluster0.example.mongodb.net/");

    // Then
    assertEquals("admin", authSource);
  }

  @Test
  public void givenAuthSourceWhenSetThenShouldUseConfiguredSource() {
    // Given
    var testConfig = EnvironmentConfiguration.of(key -> Map.of(
        "MONGO_CONNECTION_STRING", "mongodb+srv://cluster0.example.mongodb.net/",
        "MONGO_DATABASE", "dev",
        "MONGO_AUTH_SOURCE", "customAuth").get(key));
    var underTest = new MongoConfiguration(testConfig);

    // When
    var authSource = underTest.getAuthenticationDatabase("mongodb+srv://cluster0.example.mongodb.net/");

    // Then
    assertEquals("customAuth", authSource);
  }
}
