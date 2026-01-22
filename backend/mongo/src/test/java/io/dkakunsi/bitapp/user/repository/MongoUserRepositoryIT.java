package io.dkakunsi.bitapp.user.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.morphia.Datastore;
import io.dkakunsi.bitapp.common.EntityStatus;
import io.dkakunsi.bitapp.common.EnvironmentConfiguration;
import io.dkakunsi.bitapp.common.Id;
import io.dkakunsi.bitapp.mongo.MongoConfiguration;
import io.dkakunsi.bitapp.test.MongoServer;
import io.dkakunsi.bitapp.user.entity.User;
import io.dkakunsi.bitapp.user.entity.User.Language;

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
        .status(EntityStatus.ACTIVE)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .createdBy(email)
        .updatedBy(email)
        .build();

    // When
    var savedUser = repository.save(user);

    // Then
    assertNotNull(savedUser);
    assertEquals(email, savedUser.id().value());
    assertEquals(name, savedUser.name());
    assertEquals(phone, savedUser.phone());
    assertEquals(photoUrl, savedUser.photoUrl());
    assertEquals(Language.EN, savedUser.language());
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
        .status(EntityStatus.ACTIVE)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .createdBy(email)
        .updatedBy(email)
        .build();
    repository.save(originalUser);

    var updatedUser = User.builder()
        .id(Id.of(email))
        .name("Updated Name")
        .phone("089876543210")
        .photoUrl("http://photo.url/updated")
        .language(Language.ID)
        .status(EntityStatus.ACTIVE)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .createdBy(email)
        .updatedBy(email)
        .build();

    // When
    repository.save(updatedUser);
    var foundUser = repository.findByEmail(email);

    // Then
    assertTrue(foundUser.isPresent());
    assertEquals("Updated Name", foundUser.get().name());
    assertEquals("089876543210", foundUser.get().phone());
    assertEquals("http://photo.url/updated", foundUser.get().photoUrl());
    assertEquals(Language.ID, foundUser.get().language());
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
        .status(EntityStatus.ACTIVE)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .createdBy(email)
        .updatedBy(email)
        .build();
    repository.save(user);

    // When
    var foundUser = repository.findByEmail(email);

    // Then
    assertTrue(foundUser.isPresent());
    assertEquals(email, foundUser.get().id().value());
    assertEquals(name, foundUser.get().name());
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
        .status(EntityStatus.ACTIVE)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .createdBy(email)
        .updatedBy(email)
        .build();

    // When
    repository.save(user);
    var foundUser = repository.findByEmail(email);

    // Then
    assertTrue(foundUser.isPresent());
    assertEquals(email, foundUser.get().id().value());
    assertEquals("Test User", foundUser.get().name());
    assertEquals(null, foundUser.get().phone());
    assertEquals(null, foundUser.get().photoUrl());
    assertEquals(Language.EN, foundUser.get().language());
  }

  @Test
  public void givenMultipleUsersWhenSaveThenShouldPersistAll() {
    // Given
    var email1 = "user1@email.com";
    var user1 = User.builder()
        .id(Id.of(email1))
        .name("User One")
        .phone("081111111111")
        .photoUrl("http://photo.url/user1")
        .language(Language.EN)
        .status(EntityStatus.ACTIVE)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .createdBy(email1)
        .updatedBy(email1)
        .build();

    var email2 = "user2@email.com";
    var user2 = User.builder()
        .id(Id.of(email2))
        .name("User Two")
        .phone("082222222222")
        .photoUrl("http://photo.url/user2")
        .language(Language.ID)
        .status(EntityStatus.ACTIVE)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .createdBy(email2)
        .updatedBy(email2)
        .build();

    // When
    repository.save(user1);
    repository.save(user2);

    // Then
    var foundUser1 = repository.findByEmail("user1@email.com");
    var foundUser2 = repository.findByEmail("user2@email.com");

    assertTrue(foundUser1.isPresent());
    assertTrue(foundUser2.isPresent());
    assertEquals("User One", foundUser1.get().name());
    assertEquals("User Two", foundUser2.get().name());
  }

  @Test
  public void givenUserWithDifferentLanguagesWhenSaveThenShouldPersistLanguage() {
    // Given
    var emailEn = "user-en@email.com";
    var userEn = User.builder()
        .id(Id.of(emailEn))
        .name("English User")
        .phone("081111111111")
        .photoUrl("http://photo.url/en")
        .language(Language.EN)
        .status(EntityStatus.ACTIVE)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .createdBy(emailEn)
        .updatedBy(emailEn)
        .build();

    var emailId = "user-id@email.com";
    var userId = User.builder()
        .id(Id.of(emailId))
        .name("Indonesian User")
        .phone("082222222222")
        .photoUrl("http://photo.url/id")
        .language(Language.ID)
        .status(EntityStatus.ACTIVE)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .createdBy(emailId)
        .updatedBy(emailId)
        .build();

    // When
    repository.save(userEn);
    repository.save(userId);

    // Then
    var foundUserEn = repository.findByEmail(emailEn);
    var foundUserId = repository.findByEmail(emailId);

    assertTrue(foundUserEn.isPresent());
    assertTrue(foundUserId.isPresent());
    assertEquals(Language.EN, foundUserEn.get().language());
    assertEquals(Language.ID, foundUserId.get().language());
  }

  @Test
  public void givenUserWhenSaveMultipleTimesThenShouldReflectLatestChanges() {
    // Given
    var email = "update-multiple@email.com";
    var originalUser = User.builder()
        .id(Id.of(email))
        .name("Original Name")
        .phone("081111111111")
        .photoUrl("http://photo.url/original")
        .language(Language.EN)
        .status(EntityStatus.ACTIVE)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .createdBy(email)
        .updatedBy(email)
        .build();
    repository.save(originalUser);

    // When - First update
    var firstUpdate = User.builder()
        .id(Id.of(email))
        .name("First Update")
        .phone("082222222222")
        .photoUrl("http://photo.url/first")
        .language(Language.ID)
        .status(EntityStatus.ACTIVE)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .createdBy(email)
        .updatedBy("updater1")
        .build();
    repository.save(firstUpdate);

    // When - Second update
    var secondUpdate = User.builder()
        .id(Id.of(email))
        .name("Second Update")
        .phone("083333333333")
        .photoUrl("http://photo.url/second")
        .language(Language.EN)
        .status(EntityStatus.ACTIVE)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .createdBy(email)
        .updatedBy("updater2")
        .build();
    repository.save(secondUpdate);

    // Then
    var foundUser = repository.findByEmail(email);
    assertTrue(foundUser.isPresent());
    assertEquals("Second Update", foundUser.get().name());
    assertEquals("083333333333", foundUser.get().phone());
    assertEquals("http://photo.url/second", foundUser.get().photoUrl());
    assertEquals(Language.EN, foundUser.get().language());
    assertEquals("updater2", foundUser.get().updatedBy());
  }

  @Test
  public void givenUserWithLongPhoneNumberWhenSaveThenShouldPersist() {
    // Given
    var email = "long-phone@email.com";
    var longPhoneNumber = "+62-812-3456-7890-1234";
    var user = User.builder()
        .id(Id.of(email))
        .name("User with Long Phone")
        .phone(longPhoneNumber)
        .photoUrl("http://photo.url/user")
        .language(Language.EN)
        .status(EntityStatus.ACTIVE)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .createdBy(email)
        .updatedBy(email)
        .build();

    // When
    repository.save(user);
    var foundUser = repository.findByEmail(email);

    // Then
    assertTrue(foundUser.isPresent());
    assertEquals(longPhoneNumber, foundUser.get().phone());
  }

  @Test
  public void givenUserWithSpecialCharactersInNameWhenSaveThenShouldPersist() {
    // Given
    var email = "special@email.com";
    var specialName = "O'Brien-Smith & Associates, Inc.";
    var user = User.builder()
        .id(Id.of(email))
        .name(specialName)
        .phone("081234567890")
        .photoUrl("http://photo.url/special")
        .language(Language.EN)
        .status(EntityStatus.ACTIVE)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .createdBy(email)
        .updatedBy(email)
        .build();

    // When
    repository.save(user);
    var foundUser = repository.findByEmail(email);

    // Then
    assertTrue(foundUser.isPresent());
    assertEquals(specialName, foundUser.get().name());
  }
}
