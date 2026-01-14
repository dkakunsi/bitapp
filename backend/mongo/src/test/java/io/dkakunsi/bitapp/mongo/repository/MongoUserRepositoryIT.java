package io.dkakunsi.bitapp.mongo.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.morphia.Datastore;
import io.dkakunsi.bitapp.common.EnvironmentConfiguration;
import io.dkakunsi.bitapp.common.Id;
import io.dkakunsi.bitapp.mongo.MongoConfiguration;
import io.dkakunsi.bitapp.test.MongoServer;
import io.dkakunsi.bitapp.user.model.User;
import io.dkakunsi.bitapp.user.model.User.Language;

public class MongoUserRepositoryIT {

  private static MongoConfiguration mongoConfiguration;
  private static Datastore datastore;
  private MongoUserRepository repository;

  @BeforeAll
  public static void startMongo() throws Exception {
    MongoServer.startDb();
    var mongodbConfig = MongoServer.getDbConfig();
    var configuration = EnvironmentConfiguration.of(mongodbConfig::get);

    mongoConfiguration = new MongoConfiguration(configuration);
    datastore = mongoConfiguration.getDatastore();
  }

  @AfterAll
  public static void stopMongo() throws Exception {
    if (mongoConfiguration != null) {
      mongoConfiguration.close();
    }
    MongoServer.stopDb();
  }

  @BeforeEach
  public void setUp() {
    datastore.getDatabase().getCollection("users").drop();
    repository = new MongoUserRepository(datastore);
  }

  @Test
  public void givenNewUserWhenSaveThenShouldPersistUser() {
    // Given
    var email = "user@email.com";
    var name = "Test User";
    var phone = "081234567890";
    var photoUrl = "http://photo.url/user";
    var user = User.builder()
        .id(Id.of(email))
        .name(name)
        .phone(phone)
        .photoUrl(photoUrl)
        .language(Language.EN)
        .build();

    // When
    var savedUser = repository.save(user);

    // Then
    assertNotNull(savedUser);
    assertEquals(email, savedUser.getId().value());
    assertEquals(name, savedUser.getName());
    assertEquals(phone, savedUser.getPhone());
    assertEquals(photoUrl, savedUser.getPhotoUrl());
    assertEquals(Language.EN, savedUser.getLanguage());
  }

  @Test
  public void givenExistingUserWhenSaveThenShouldUpdateUser() {
    // Given
    var email = "user@email.com";
    var originalUser = User.builder()
        .id(Id.of(email))
        .name("Original Name")
        .phone("081234567890")
        .photoUrl("http://photo.url/original")
        .language(Language.EN)
        .build();
    repository.save(originalUser);

    var updatedUser = User.builder()
        .id(Id.of(email))
        .name("Updated Name")
        .phone("089876543210")
        .photoUrl("http://photo.url/updated")
        .language(Language.ID)
        .build();

    // When
    repository.save(updatedUser);
    var foundUser = repository.findByEmail(email);

    // Then
    assertTrue(foundUser.isPresent());
    assertEquals("Updated Name", foundUser.get().getName());
    assertEquals("089876543210", foundUser.get().getPhone());
    assertEquals("http://photo.url/updated", foundUser.get().getPhotoUrl());
    assertEquals(Language.ID, foundUser.get().getLanguage());
  }

  @Test
  public void givenExistingUserWhenFindByEmailThenShouldReturnUser() {
    // Given
    var email = "user@email.com";
    var name = "Test User";
    var user = User.builder()
        .id(Id.of(email))
        .name(name)
        .phone("081234567890")
        .photoUrl("http://photo.url/user")
        .language(Language.EN)
        .build();
    repository.save(user);

    // When
    var foundUser = repository.findByEmail(email);

    // Then
    assertTrue(foundUser.isPresent());
    assertEquals(email, foundUser.get().getId().value());
    assertEquals(name, foundUser.get().getName());
  }

  @Test
  public void givenNonExistentUserWhenFindByEmailThenShouldReturnEmpty() {
    // Given
    var email = "nonexistent@email.com";

    // When
    var foundUser = repository.findByEmail(email);

    // Then
    assertTrue(foundUser.isEmpty());
  }

  @Test
  public void givenUserWithNullFieldsWhenSaveThenShouldPersistWithNulls() {
    // Given
    var email = "user@email.com";
    var user = User.builder()
        .id(Id.of(email))
        .name("Test User")
        .phone(null)
        .photoUrl(null)
        .language(Language.EN)
        .build();

    // When
    repository.save(user);
    var foundUser = repository.findByEmail(email);

    // Then
    assertTrue(foundUser.isPresent());
    assertEquals(email, foundUser.get().getId().value());
    assertEquals("Test User", foundUser.get().getName());
    assertEquals(null, foundUser.get().getPhone());
    assertEquals(null, foundUser.get().getPhotoUrl());
    assertEquals(Language.EN, foundUser.get().getLanguage());
  }

  @Test
  public void givenMultipleUsersWhenSaveThenShouldPersistAll() {
    // Given
    var user1 = User.builder()
        .id(Id.of("user1@email.com"))
        .name("User One")
        .phone("081111111111")
        .photoUrl("http://photo.url/user1")
        .language(Language.EN)
        .build();

    var user2 = User.builder()
        .id(Id.of("user2@email.com"))
        .name("User Two")
        .phone("082222222222")
        .photoUrl("http://photo.url/user2")
        .language(Language.ID)
        .build();

    // When
    repository.save(user1);
    repository.save(user2);

    // Then
    var foundUser1 = repository.findByEmail("user1@email.com");
    var foundUser2 = repository.findByEmail("user2@email.com");

    assertTrue(foundUser1.isPresent());
    assertTrue(foundUser2.isPresent());
    assertEquals("User One", foundUser1.get().getName());
    assertEquals("User Two", foundUser2.get().getName());
  }
}