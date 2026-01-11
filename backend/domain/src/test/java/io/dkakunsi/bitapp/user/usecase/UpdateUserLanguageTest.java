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
import io.dkakunsi.bitapp.common.Id;
import io.dkakunsi.bitapp.common.usecase.Input;
import io.dkakunsi.bitapp.user.dto.UpdateUserLanguageInput;
import io.dkakunsi.bitapp.user.model.User;
import io.dkakunsi.bitapp.user.model.User.Language;
import io.dkakunsi.bitapp.user.repository.UserRepository;

public final class UpdateUserLanguageTest {

  private UpdateUserLanguage underTest;

  private UserRepository userRepository;

  private static final String REQUESTER = "Requester";

  @BeforeEach
  void setUp() {
    userRepository = mock(UserRepository.class);
    underTest = new UpdateUserLanguage(userRepository);
  }

  @Test
  public void givenValidUpdateLanguageRequestWhenUserExistsThenShouldUpdateLanguageAndSuccess() {
    // Given
    var email = "user@email.com";
    var updateInput = UpdateUserLanguageInput.builder()
        .email(email)
        .language(Language.ID)
        .build();

    var context = Context.builder().requester(REQUESTER).build();
    var processInput = new Input<>(updateInput, context);

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
    var result = underTest.process(processInput);

    // Then
    assertTrue(result.isSuccess());
    verify(userRepository).findByEmail(email);

    var updatedResult = result.data().get();
    assertEquals(email, updatedResult.email());
    assertEquals(Language.ID, updatedResult.language());

    var userCaptor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(userCaptor.capture());
    var savedUser = userCaptor.getValue();
    assertEquals(email, savedUser.getId().value());
    assertEquals(Language.ID, savedUser.getLanguage());
    assertEquals("User Name", savedUser.getName());
    assertEquals("081234567890", savedUser.getPhone());
    assertEquals("http://photo.url/user", savedUser.getPhotoUrl());
  }

  @Test
  public void givenValidUpdateLanguageRequestWhenUserDoesNotExistThenShouldFail() {
    // Given
    var email = "nonexistent@email.com";
    var updateInput = UpdateUserLanguageInput.builder()
        .email(email)
        .language(Language.ID)
        .build();

    var context = Context.builder().requester(REQUESTER).build();
    var processInput = new Input<>(updateInput, context);

    when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

    // When
    var result = underTest.process(processInput);

    // Then
    assertFalse(result.isSuccess());
    assertEquals(Code.BAD_REQUEST, result.error().get().code());
    assertEquals("User not found", result.error().get().message());
  }

  @Test
  public void givenValidUpdateLanguageRequestWhenRepositoryThrowsErrorThenShouldFail() {
    // Given
    var email = "user@email.com";
    var updateInput = UpdateUserLanguageInput.builder()
        .email(email)
        .language(Language.ID)
        .build();

    var context = Context.builder().requester(REQUESTER).build();
    var processInput = new Input<>(updateInput, context);

    when(userRepository.findByEmail(email)).thenThrow(new RuntimeException("Database error"));

    // When
    var result = underTest.process(processInput);

    // Then
    assertFalse(result.isSuccess());
    assertEquals(Code.SERVER_ERROR, result.error().get().code());
    assertEquals("Database error", result.error().get().message());
  }

  @Test
  public void givenInvalidUpdateLanguageRequestWhenIllegalArgumentExceptionThenShouldReturnBadRequest() {
    // Given
    var email = "user@email.com";
    var updateInput = UpdateUserLanguageInput.builder()
        .email(email)
        .language(Language.EN)
        .build();

    var context = Context.builder().requester(REQUESTER).build();
    var processInput = new Input<>(updateInput, context);

    when(userRepository.findByEmail(email)).thenThrow(new IllegalArgumentException("Invalid email format"));

    // When
    var result = underTest.process(processInput);

    // Then
    assertFalse(result.isSuccess());
    assertEquals(Code.BAD_REQUEST, result.error().get().code());
    assertEquals("Invalid email format", result.error().get().message());
  }

  @Test
  public void givenUpdateLanguageFromEnToIdWhenProcessedThenShouldUpdateCorrectly() {
    // Given
    var email = "user@email.com";
    var updateInput = UpdateUserLanguageInput.builder()
        .email(email)
        .language(Language.ID)
        .build();

    var context = Context.builder().requester(REQUESTER).build();
    var processInput = new Input<>(updateInput, context);

    var existingUser = User.builder()
        .id(Id.of(email))
        .name("User Name")
        .language(Language.EN)
        .build();

    when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));
    when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    var result = underTest.process(processInput);

    // Then
    assertTrue(result.isSuccess());
    assertEquals(Language.ID, result.data().get().language());
  }

  @Test
  public void givenUpdateLanguageFromIdToEnWhenProcessedThenShouldUpdateCorrectly() {
    // Given
    var email = "user@email.com";
    var updateInput = UpdateUserLanguageInput.builder()
        .email(email)
        .language(Language.EN)
        .build();

    var context = Context.builder().requester(REQUESTER).build();
    var processInput = new Input<>(updateInput, context);

    var existingUser = User.builder()
        .id(Id.of(email))
        .name("User Name")
        .language(Language.ID)
        .build();

    when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));
    when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    var result = underTest.process(processInput);

    // Then
    assertTrue(result.isSuccess());
    assertEquals(Language.EN, result.data().get().language());
  }
}
