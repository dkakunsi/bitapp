package io.dkakunsi.bitapp.mongo;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import dev.morphia.Datastore;
import dev.morphia.Morphia;
import io.dkakunsi.bitapp.common.Configuration;

public final class MongoConfiguration {

  private static final String ENV_MONGO_CONNECTION_STRING = "MONGO_CONNECTION_STRING";
  private static final String ENV_MONGO_HOST = "MONGO_HOST";
  private static final String ENV_MONGO_PORT = "MONGO_PORT";
  private static final String ENV_MONGO_DATABASE = "MONGO_DATABASE";
  private static final String ENV_MONGO_AUTH_SOURCE = "MONGO_AUTH_SOURCE";
  private static final String ENV_MONGO_SECURE = "MONGO_SECURE";
  private static final String ENV_MONGO_USERNAME = "MONGO_USERNAME";
  private static final String ENV_MONGO_PASSWORD = "MONGO_PASSWORD";

  private final Configuration configuration;
  private Optional<MongoClient> mongoClient;

  public MongoConfiguration(Configuration configuration) {
    this.configuration = configuration;
    mongoClient = Optional.empty();
  }

  private MongoClient initMongoClient() {
    if (mongoClient.isPresent()) {
      return mongoClient.get();
    }

    var connectionString = getConnectionString();
    var settingsBuilder = MongoClientSettings.builder()
        .applyConnectionString(new ConnectionString(connectionString));
    if (isSecure()) {
      var credential = getCredential(connectionString);
      settingsBuilder.credential(credential);
    }
    var settings = settingsBuilder.build();
    mongoClient = Optional.of(MongoClients.create(settings));

    return mongoClient.get();
  }

  private String getConnectionString() {
    String connectionString = configuration.get(ENV_MONGO_CONNECTION_STRING).orElse(null);
    if (StringUtils.isNotBlank(connectionString)) {
      return connectionString;
    }

    String host = configuration.get(ENV_MONGO_HOST).orElseThrow();
    String port = configuration.get(ENV_MONGO_PORT).orElseThrow();

    return String.format("mongodb://%s:%s", host, port);
  }

  private boolean isSecure() {
    String secure = configuration.get(ENV_MONGO_SECURE).orElse("false");
    return secure.equalsIgnoreCase("true");
  }

  private MongoCredential getCredential(String connectionString) {
    var username = configuration.get(ENV_MONGO_USERNAME).orElseThrow();
    var password = configuration.get(ENV_MONGO_PASSWORD).orElseThrow();
    var databaseName = getAuthenticationDatabase(connectionString);
    var credential = com.mongodb.MongoCredential.createCredential(
        username,
        databaseName,
        password.toCharArray());
    return credential;
  }

  String getAuthenticationDatabase(String connectionString) {
    var authSource = configuration.get(ENV_MONGO_AUTH_SOURCE).orElse(null);
    if (StringUtils.isNotBlank(authSource)) {
      return authSource;
    }

    if (connectionString.startsWith("mongodb+srv://")) {
      return "admin";
    }

    return configuration.get(ENV_MONGO_DATABASE).orElseThrow();
  }

  public MongoDatabase getDatabase() {
    var databaseName = configuration.get(ENV_MONGO_DATABASE).orElseThrow();
    return initMongoClient().getDatabase(databaseName);
  }

  public MongoClient getMongoClient() {
    return initMongoClient();
  }

  public Datastore getDatastore() {
    var databaseName = configuration.get(ENV_MONGO_DATABASE).orElseThrow();
    var mongoClient = getMongoClient();
    var datastore = Morphia.createDatastore(mongoClient, databaseName);
    return datastore;
  }

  public void close() {
    if (mongoClient.isPresent()) {
      mongoClient.get().close();
      mongoClient = Optional.empty();
    }
  }

  public MongoSessionManager getSessionManager() {
    return new MongoSessionManager(getDatastore());
  }
}
