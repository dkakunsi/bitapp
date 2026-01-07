package io.dkakunsi.bitapp.user.process;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.dkakunsi.bitapp.common.Context;
import io.dkakunsi.bitapp.common.Id;
import io.dkakunsi.bitapp.common.Input;
import io.dkakunsi.bitapp.user.dto.UserRetrievalInput;
import io.dkakunsi.bitapp.user.model.User;
import io.dkakunsi.bitapp.user.model.User.Language;
import io.dkakunsi.bitapp.user.repository.UserRepository;

public class UserRetrievalProcessTest {

  private static final String REQUESTER = "Requester";

  private UserRetrievalProcess underTest;

  private UserRepository userRepository;

  @BeforeEach
  void setUp() {
    userRepository = mock(UserRepository.class);
    underTest = new UserRetrievalProcess(userRepository);
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
    var inputData = UserRetrievalInput.builder()
        .email(email)
        .build();
    var context = Context.builder().requester(REQUESTER).build();
    var input = new Input<>(inputData, context);
    var result = underTest.process(input);

    assertTrue(result.isSuccess());
    assertTrue(result.data().isPresent());
    var user = result.data().get();
    assertEquals(email, user.getId().value());
    assertEquals("Existing User", user.getName());
    assertEquals("081234567890", user.getPhone());
    assertEquals("http://photo.url/existing_user", user.getPhotoUrl());
    assertEquals(User.Language.EN, user.getLanguage());
  }

  @Test
  void returnEmptyData_whenUserNotExists() {
    // Given
    var email = "nonexistent@example.com";
    when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

    // When
    var inputData = UserRetrievalInput.builder()
        .email(email)
        .build();
    var context = Context.builder().requester(REQUESTER).build();
    var input = new Input<>(inputData, context);
    var result = underTest.process(input);

    assertTrue(result.isSuccess());
    assertTrue(result.data().isEmpty());
  }

  @Test
  void returnServerError_whenUserPortThrowsException() {
    // Given
    var email = "error@example.com";
    when(userRepository.findByEmail(email)).thenThrow(new RuntimeException("Database error"));

    // When
    var inputData = UserRetrievalInput.builder()
        .email(email)
        .build();
    var context = Context.builder().requester(REQUESTER).build();
    var input = new Input<>(inputData, context);
    var result = underTest.process(input);

    assertTrue(result.isFailed());
    var error = result.error().get();
    assertEquals("Database error", error.message());
  }
}