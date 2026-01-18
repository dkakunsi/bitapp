package io.dkakunsi.bitapp.user.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.dkakunsi.bitapp.common.AppError.Code;
import io.dkakunsi.bitapp.common.Context;
import io.dkakunsi.bitapp.common.Id;
import io.dkakunsi.bitapp.user.dto.GetUserInput;
import io.dkakunsi.bitapp.user.entity.User;
import io.dkakunsi.bitapp.user.entity.User.Language;
import io.dkakunsi.bitapp.user.repository.UserRepository;

public final class GetUserTest {

  private static final String REQUESTER = "Requester";

  private GetUser underTest;

  private UserRepository userRepository;

  @BeforeEach
  void setUp() {
    userRepository = mock(UserRepository.class);
    underTest = new GetUser(userRepository);
  }

  @Test
  void returnUserData_whenUserExists() {
    // Given
    var email = "user@example.com";
    var existingUser = User.builder()
        .id(Id.of(email))
        .name("Existing User")
        .phone("081234567890")
        .photoUrl("http://photo.url/existing_user")
        .language(Language.EN)
        .build();
    when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));

    // When
    var inputData = GetUserInput.builder()
        .email(email)
        .build();
    var context = Context.builder().requester(REQUESTER).build();
    var result = underTest.process(context, inputData);

    // Then
    assertTrue(result.isSuccess());
    assertTrue(result.data().isPresent());
    var user = result.data().get();
    assertEquals(email, user.email());
    assertEquals("Existing User", user.name());
    assertEquals("081234567890", user.phone());
    assertEquals("http://photo.url/existing_user", user.photoUrl());
    assertEquals(User.Language.EN.name(), user.language());
    verify(userRepository).findByEmail(email);
  }

  @Test
  void returnEmptyData_whenUserNotExists() {
    // Given
    var email = "nonexistent@example.com";
    when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

    // When
    var inputData = GetUserInput.builder()
        .email(email)
        .build();
    var context = Context.builder().requester(REQUESTER).build();
    var result = underTest.process(context, inputData);

    // Then
    assertTrue(result.isFailed());
    assertEquals(Code.NOT_FOUND, result.error().get().code());
    assertEquals("User not found", result.error().get().message());

    verify(userRepository).findByEmail(email);
  }

  @Test
  void returnServerError_whenUserPortThrowsException() {
    // Given
    var email = "error@example.com";
    when(userRepository.findByEmail(email)).thenThrow(new RuntimeException("Database error"));

    // When
    var inputData = GetUserInput.builder()
        .email(email)
        .build();
    var context = Context.builder().requester(REQUESTER).build();
    var result = underTest.process(context, inputData);

    // Then
    assertFalse(result.isSuccess());
    assertTrue(result.isFailed());
    assertTrue(result.error().isPresent());
    var error = result.error().get();
    assertEquals(Code.SERVER_ERROR, error.code());
    assertEquals("Database error", error.message());

    verify(userRepository).findByEmail(email);
  }
}