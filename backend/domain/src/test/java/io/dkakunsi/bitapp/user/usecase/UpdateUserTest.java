package io.dkakunsi.bitapp.user.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import io.dkakunsi.bitapp.common.AppError.Code;
import io.dkakunsi.bitapp.common.Context;
import io.dkakunsi.bitapp.domain.entity.Id;
import io.dkakunsi.bitapp.user.dto.UpdateUserInput;
import io.dkakunsi.bitapp.user.entity.User;
import io.dkakunsi.bitapp.user.entity.User.Language;
import io.dkakunsi.bitapp.user.repository.UserRepository;

public final class UpdateUserTest {

  private UpdateUser underTest;

  private UserRepository userRepository;

  @BeforeEach
  void setUp() {
    userRepository = mock(UserRepository.class);
    underTest = new UpdateUser(userRepository);
  }

  @Test
  public void givenValidUpdateLanguageRequestWhenUserExistsThenShouldUpdateLanguageAndSuccess() {
    // Given
    var email = "user@email.com";
    var updateInput = UpdateUserInput.builder()
        .email(email)
        .language("ID")
        .build();

    var context = Context.builder().requester(email).build();

    var existingUser = User.builder()
        .id(Id.of(email))
        .name("User Name")
        .phone("081234567890")
        .photoUrl("http://photo.url/user")
        .language(Language.EN)
        .build();

    when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));
    when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    var result = underTest.process(context, updateInput);

    // Then
    assertTrue(result.isSuccess());
    verify(userRepository).findByEmail(email);

    var updatedResult = result.data().get();
    assertEquals(email, updatedResult.email());
    assertEquals(Language.ID.name(), updatedResult.language());

    var userCaptor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(userCaptor.capture());
    var savedUser = userCaptor.getValue();
    assertEquals(email, savedUser.id().value());
    assertEquals(Language.ID, savedUser.language());
    assertEquals("User Name", savedUser.name());
    assertEquals("081234567890", savedUser.phone());
    assertEquals("http://photo.url/user", savedUser.photoUrl());
  }

  @Test
  public void givenUpdateLanguageRequestWhenRequesterDoesNotMatchEmailThenShouldReturnBadRequest() {
    // Given
    var email = "user@email.com";
    var differentEmail = "other@email.com";
    var updateInput = UpdateUserInput.builder()
        .email(email)
        .language("ID")
        .build();

    var context = Context.builder().requester(differentEmail).build();

    // When
    var result = underTest.process(context, updateInput);

    // Then
    assertFalse(result.isSuccess());
    assertEquals(Code.BAD_REQUEST, result.error().get().code());
    assertEquals("User can only update their own data", result.error().get().message());
  }

  @Test
  public void givenValidUpdateLanguageRequestWhenUserDoesNotExistThenShouldReturnEmpty() {
    // Given
    var email = "nonexistent@email.com";
    var updateInput = UpdateUserInput.builder()
        .email(email)
        .language("ID")
        .build();

    var context = Context.builder().requester(email).build();

    when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

    // When
    var result = underTest.process(context, updateInput);

    // Then
    assertTrue(result.isFailed());
    assertEquals(Code.NOT_FOUND, result.error().get().code());
    assertEquals("User not found", result.error().get().message());
  }

  @Test
  public void givenValidUpdateLanguageRequestWhenRepositoryThrowsErrorThenShouldFail() {
    // Given
    var email = "user@email.com";
    var updateInput = UpdateUserInput.builder()
        .email(email)
        .language("ID")
        .build();

    var context = Context.builder().requester(email).build();

    when(userRepository.findByEmail(email)).thenThrow(new RuntimeException("Database error"));

    // When
    var result = underTest.process(context, updateInput);

    // Then
    assertFalse(result.isSuccess());
    assertEquals(Code.SERVER_ERROR, result.error().get().code());
    assertEquals("Database error", result.error().get().message());
  }

  @Test
  public void givenInvalidUpdateLanguageRequestWhenIllegalArgumentExceptionThenShouldReturnBadRequest() {
    // Given
    var email = "user@email.com";
    var updateInput = UpdateUserInput.builder()
        .email(email)
        .language("EN")
        .build();

    var context = Context.builder().requester(email).build();

    when(userRepository.findByEmail(email)).thenThrow(new IllegalArgumentException("Invalid email format"));

    // When
    var result = underTest.process(context, updateInput);

    // Then
    assertFalse(result.isSuccess());
    assertEquals(Code.BAD_REQUEST, result.error().get().code());
    assertEquals("Invalid email format", result.error().get().message());
  }

  @Test
  public void givenUpdateLanguageFromEnToIdWhenProcessedThenShouldUpdateCorrectly() {
    // Given
    var email = "user@email.com";
    var updateInput = UpdateUserInput.builder()
        .email(email)
        .language("ID")
        .build();

    var context = Context.builder().requester(email).build();

    var existingUser = User.builder()
        .id(Id.of(email))
        .name("User Name")
        .language(Language.EN)
        .build();

    when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));
    when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    var result = underTest.process(context, updateInput);

    // Then
    assertTrue(result.isSuccess());
    assertEquals(Language.ID.name(), result.data().get().language());
  }

  @Test
  public void givenUpdateLanguageFromIdToEnWhenProcessedThenShouldUpdateCorrectly() {
    // Given
    var email = "user@email.com";
    var updateInput = UpdateUserInput.builder()
        .email(email)
        .language("EN")
        .build();

    var context = Context.builder().requester(email).build();

    var existingUser = User.builder()
        .id(Id.of(email))
        .name("User Name")
        .language(Language.ID)
        .build();

    when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));
    when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    var result = underTest.process(context, updateInput);

    // Then
    assertTrue(result.isSuccess());
    assertEquals(Language.EN.name(), result.data().get().language());
  }

  @Test
  public void givenUpdateLanguageWithInvalidLanguageCodeWhenProcessedThenShouldFail() {
    // Given
    var email = "user@email.com";
    var updateInput = UpdateUserInput.builder()
        .email(email)
        .language("INVALID")
        .build();

    var context = Context.builder().requester(email).build();

    var existingUser = User.builder()
        .id(Id.of(email))
        .name("User Name")
        .language(Language.EN)
        .build();

    when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));

    // When
    var result = underTest.process(context, updateInput);

    // Then
    assertFalse(result.isSuccess());
    assertEquals(Code.BAD_REQUEST, result.error().get().code());
  }

  @Test
  public void givenUpdateLanguageToSameLanguageWhenProcessedThenShouldSucceed() {
    // Given
    var email = "user@email.com";
    var updateInput = UpdateUserInput.builder()
        .email(email)
        .language("EN")
        .build();

    var context = Context.builder().requester(email).build();

    var existingUser = User.builder()
        .id(Id.of(email))
        .name("User Name")
        .language(Language.EN)
        .build();

    when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));
    when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    var result = underTest.process(context, updateInput);

    // Then
    assertTrue(result.isSuccess());
    assertEquals(Language.EN.name(), result.data().get().language());

    var userCaptor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(userCaptor.capture());
    var savedUser = userCaptor.getValue();
    assertEquals(Language.EN, savedUser.language());
  }

  @Test
  public void givenUpdateLanguageRequestWhenUserHasAllFieldsPopulatedThenShouldPreserveOtherFields() {
    // Given
    var email = "user@email.com";
    var updateInput = UpdateUserInput.builder()
        .email(email)
        .language("ID")
        .build();

    var context = Context.builder().requester(email).build();

    var existingUser = User.builder()
        .id(Id.of(email))
        .name("Complete User")
        .phone("1234567890")
        .photoUrl("http://example.com/photo.jpg")
        .language(Language.EN)
        .build();

    when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));
    when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    var result = underTest.process(context, updateInput);

    // Then
    assertTrue(result.isSuccess());

    var userCaptor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(userCaptor.capture());
    var savedUser = userCaptor.getValue();

    // Verify language was updated
    assertEquals(Language.ID, savedUser.language());

    // Verify other fields were preserved
    assertEquals("Complete User", savedUser.name());
    assertEquals("1234567890", savedUser.phone());
    assertEquals("http://example.com/photo.jpg", savedUser.photoUrl());
  }
}
