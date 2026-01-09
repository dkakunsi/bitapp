package io.dkakunsi.bitapp.user.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.dkakunsi.bitapp.common.Id;
import io.dkakunsi.bitapp.user.dto.RegisterUserInput;
import io.dkakunsi.bitapp.user.model.User.Language;

public final class UserTest {

  @Test
  public void givenUserBuilderWhenBuildThenShouldCreateUserWithAllFields() {
    // Given
    var id = Id.of("user@email.com");
    var name = "User Name";
    var phone = "081234567890";
    var photoUrl = "http://photo.url/user";
    var language = Language.EN;

    // When
    var user = User.builder()
        .id(id)
        .name(name)
        .phone(phone)
        .photoUrl(photoUrl)
        .language(language)
        .build();

    // Then
    assertNotNull(user);
    assertEquals(id, user.getId());
    assertEquals(name, user.getName());
    assertEquals(phone, user.getPhone());
    assertEquals(photoUrl, user.getPhotoUrl());
    assertEquals(language, user.getLanguage());
  }

  @Test
  public void givenUserRegistrationInputWhenFromThenShouldCreateUserWithDefaultLanguage() {
    // Given
    var email = "user@email.com";
    var name = "User Name";
    var phone = "081234567890";
    var photoUrl = "http://photo.url/user";
    var input = RegisterUserInput.builder()
        .email(email)
        .name(name)
        .phone(phone)
        .photoUrl(photoUrl)
        .build();

    // When
    var user = input.toUser();

    // Then
    assertNotNull(user);
    assertEquals(Id.of(email), user.getId());
    assertEquals(name, user.getName());
    assertEquals(phone, user.getPhone());
    assertEquals(photoUrl, user.getPhotoUrl());
    assertEquals(Language.EN, user.getLanguage());
  }

  @Test
  public void givenUserRegistrationInputWithNullPhoneThenShouldCreateUserWithNullPhone() {
    // Given
    var email = "user@email.com";
    var name = "User Name";
    var input = RegisterUserInput.builder()
        .email(email)
        .name(name)
        .phone(null)
        .photoUrl(null)
        .build();

    // When
    var user = input.toUser();

    // Then
    assertNotNull(user);
    assertEquals(Id.of(email), user.getId());
    assertEquals(name, user.getName());
    assertEquals(null, user.getPhone());
    assertEquals(null, user.getPhotoUrl());
    assertEquals(Language.EN, user.getLanguage());
  }

  @Test
  public void givenDifferentNameWhenNeedUpdateThenShouldReturnTrue() {
    // Given
    var user = User.builder()
        .id(Id.of("user@email.com"))
        .name("Original Name")
        .phone("081234567890")
        .photoUrl("http://photo.url/user")
        .language(Language.EN)
        .build();

    var input = RegisterUserInput.builder()
        .email("user@email.com")
        .name("Updated Name")
        .phone("081234567890")
        .photoUrl("http://photo.url/user")
        .build();

    // When
    var needUpdate = user.needUpdate(input);

    // Then
    assertTrue(needUpdate);
  }

  @Test
  public void givenDifferentPhoneWhenNeedUpdateThenShouldReturnTrue() {
    // Given
    var user = User.builder()
        .id(Id.of("user@email.com"))
        .name("User Name")
        .phone("081234567890")
        .photoUrl("http://photo.url/user")
        .language(Language.EN)
        .build();

    var input = RegisterUserInput.builder()
        .email("user@email.com")
        .name("User Name")
        .phone("089876543210")
        .photoUrl("http://photo.url/user")
        .build();

    // When
    var needUpdate = user.needUpdate(input);

    // Then
    assertTrue(needUpdate);
  }

  @Test
  public void givenDifferentPhotoUrlWhenNeedUpdateThenShouldReturnTrue() {
    // Given
    var user = User.builder()
        .id(Id.of("user@email.com"))
        .name("User Name")
        .phone("081234567890")
        .photoUrl("http://photo.url/user")
        .language(Language.EN)
        .build();

    var input = RegisterUserInput.builder()
        .email("user@email.com")
        .name("User Name")
        .phone("081234567890")
        .photoUrl("http://new.photo.url/user")
        .build();

    // When
    var needUpdate = user.needUpdate(input);

    // Then
    assertTrue(needUpdate);
  }

  @Test
  public void givenSameValuesWhenNeedUpdateThenShouldReturnFalse() {
    // Given
    var user = User.builder()
        .id(Id.of("user@email.com"))
        .name("User Name")
        .phone("081234567890")
        .photoUrl("http://photo.url/user")
        .language(Language.EN)
        .build();

    var input = RegisterUserInput.builder()
        .email("user@email.com")
        .name("User Name")
        .phone("081234567890")
        .photoUrl("http://photo.url/user")
        .build();

    // When
    var needUpdate = user.needUpdate(input);

    // Then
    assertFalse(needUpdate);
  }

  @Test
  public void givenNullPhoneInBothWhenNeedUpdateThenShouldReturnFalse() {
    // Given
    var user = User.builder()
        .id(Id.of("user@email.com"))
        .name("User Name")
        .phone(null)
        .photoUrl("http://photo.url/user")
        .language(Language.EN)
        .build();

    var input = RegisterUserInput.builder()
        .email("user@email.com")
        .name("User Name")
        .phone(null)
        .photoUrl("http://photo.url/user")
        .build();

    // When
    var needUpdate = user.needUpdate(input);

    // Then
    assertFalse(needUpdate);
  }

  @Test
  public void givenNullPhoneToNonNullPhoneWhenNeedUpdateThenShouldReturnTrue() {
    // Given
    var user = User.builder()
        .id(Id.of("user@email.com"))
        .name("User Name")
        .phone(null)
        .photoUrl("http://photo.url/user")
        .language(Language.EN)
        .build();

    var input = RegisterUserInput.builder()
        .email("user@email.com")
        .name("User Name")
        .phone("081234567890")
        .photoUrl("http://photo.url/user")
        .build();

    // When
    var needUpdate = user.needUpdate(input);

    // Then
    assertTrue(needUpdate);
  }

  @Test
  public void givenUserRegistrationInputWhenUpdateThenShouldCreateUpdatedUserPreservingIdAndLanguage() {
    // Given
    var originalId = Id.of("user@email.com");
    var originalLanguage = Language.ID;
    var user = User.builder()
        .id(originalId)
        .name("Original Name")
        .phone("081234567890")
        .photoUrl("http://photo.url/user")
        .language(originalLanguage)
        .build();

    var updatedName = "Updated Name";
    var updatedPhone = "089876543210";
    var updatedPhotoUrl = "http://new.photo.url/user";
    var input = RegisterUserInput.builder()
        .email("different@email.com") // Email should not affect the update
        .name(updatedName)
        .phone(updatedPhone)
        .photoUrl(updatedPhotoUrl)
        .build();

    // When
    var updatedUser = user.update(input);

    // Then
    assertNotNull(updatedUser);
    assertEquals(originalId, updatedUser.getId()); // ID should be preserved
    assertEquals(originalLanguage, updatedUser.getLanguage()); // Language should be preserved
    assertEquals(updatedName, updatedUser.getName());
    assertEquals(updatedPhone, updatedUser.getPhone());
    assertEquals(updatedPhotoUrl, updatedUser.getPhotoUrl());
  }

  @Test
  public void givenUserRegistrationInputWithNullValuesWhenUpdateThenShouldCreateUserWithNullValues() {
    // Given
    var originalId = Id.of("user@email.com");
    var originalLanguage = Language.EN;
    var user = User.builder()
        .id(originalId)
        .name("Original Name")
        .phone("081234567890")
        .photoUrl("http://photo.url/user")
        .language(originalLanguage)
        .build();

    var input = RegisterUserInput.builder()
        .email("user@email.com")
        .name("Updated Name")
        .phone(null)
        .photoUrl(null)
        .build();

    // When
    var updatedUser = user.update(input);

    // Then
    assertNotNull(updatedUser);
    assertEquals(originalId, updatedUser.getId());
    assertEquals(originalLanguage, updatedUser.getLanguage());
    assertEquals("Updated Name", updatedUser.getName());
    assertEquals(null, updatedUser.getPhone());
    assertEquals(null, updatedUser.getPhotoUrl());
  }

  @Test
  public void givenTwoUsersWithSameValuesWhenEqualsThenShouldReturnTrue() {
    // Given
    var id = Id.of("user@email.com");
    var name = "User Name";
    var phone = "081234567890";
    var photoUrl = "http://photo.url/user";
    var language = Language.EN;

    var user1 = User.builder()
        .id(id)
        .name(name)
        .phone(phone)
        .photoUrl(photoUrl)
        .language(language)
        .build();

    var user2 = User.builder()
        .id(id)
        .name(name)
        .phone(phone)
        .photoUrl(photoUrl)
        .language(language)
        .build();

    // When & Then
    assertEquals(user1, user2);
    assertEquals(user1.hashCode(), user2.hashCode());
  }

  @Test
  public void givenTwoUsersWithDifferentIdsWhenEqualsThenShouldReturnFalse() {
    // Given
    var user1 = User.builder()
        .id(Id.of("user1@email.com"))
        .name("User Name")
        .phone("081234567890")
        .photoUrl("http://photo.url/user")
        .language(Language.EN)
        .build();

    var user2 = User.builder()
        .id(Id.of("user2@email.com"))
        .name("User Name")
        .phone("081234567890")
        .photoUrl("http://photo.url/user")
        .language(Language.EN)
        .build();

    // When & Then
    assertFalse(user1.equals(user2));
  }

  @Test
  public void givenUserWhenToStringThenShouldContainAllFields() {
    // Given
    var user = User.builder()
        .id(Id.of("user@email.com"))
        .name("User Name")
        .phone("081234567890")
        .photoUrl("http://photo.url/user")
        .language(Language.EN)
        .build();

    // When
    var toString = user.toString();

    // Then
    assertNotNull(toString);
    assertTrue(toString.contains("user@email.com"));
    assertTrue(toString.contains("User Name"));
    assertTrue(toString.contains("081234567890"));
    assertTrue(toString.contains("http://photo.url/user"));
    assertTrue(toString.contains("EN"));
  }
}
