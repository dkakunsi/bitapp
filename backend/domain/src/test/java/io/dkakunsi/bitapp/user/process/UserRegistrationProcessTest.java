package io.dkakunsi.bitapp.user.process;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import io.dkakunsi.bitapp.common.Context;
import io.dkakunsi.bitapp.common.Id;
import io.dkakunsi.bitapp.common.Input;
import io.dkakunsi.bitapp.common.AppError.Code;
import io.dkakunsi.bitapp.user.dto.UserRegistrationInput;
import io.dkakunsi.bitapp.user.model.User;
import io.dkakunsi.bitapp.user.model.User.Language;
import io.dkakunsi.bitapp.user.repository.UserRepository;

public final class UserRegistrationProcessTest {

  private UserRegistrationProcess underTest;

  private UserRepository userRepository;

  private static final String REQUESTER = "Requester";

  @BeforeEach
  void setUp() {
    userRepository = mock(UserRepository.class);
    underTest = new UserRegistrationProcess(userRepository);
  }

  @Test
  public void givenValidRegisterUserRequestWhenUserIsNotExistsThenShouldCreateUserAndSuccess() {
    // Given
    var email = "user@email.com";
    var username = "User Name";
    var phone = "081234567890";
    var photoUrl = "http://photo.url/user";
    var registerInput = UserRegistrationInput.builder()
        .email(email)
        .name(username)
        .phone(phone)
        .photoUrl(photoUrl)
        .build();

    var context = Context.builder().requester(REQUESTER).build();
    var processInput = new Input<>(registerInput, context);

    when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
    when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    var result = underTest.process(processInput);

    // Then
    assertTrue(result.isSuccess());

    var createdUser = result.data().get();
    assertEquals(username, createdUser.getName());
    assertEquals(phone, createdUser.getPhone());
    assertEquals(photoUrl, createdUser.getPhotoUrl());
    assertEquals(Language.EN, createdUser.getLanguage());
    assertNotNull(createdUser.getId());

    verify(userRepository).findByEmail(email);

    var userCaptor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(userCaptor.capture());
    var savedUser = userCaptor.getValue();
    assertEquals(username, savedUser.getName());
    assertEquals(phone, savedUser.getPhone());
    assertEquals(photoUrl, savedUser.getPhotoUrl());
    assertEquals(Language.EN, savedUser.getLanguage());
    assertNotNull(savedUser.getId());
  }

  @Test
  public void givenValidRegisterUserRequestWhenUserExistsThenShouldUpdateUserAndSuccess() {
    // Given
    var existingUsername = "User Name";
    var email = "user@email.com";
    var phone = "081234567890";
    var photoUrl = "http://photo.url/user";
    var updatingUserName = "Update User Name";
    var registerInput = UserRegistrationInput.builder()
        .email(email)
        .name(updatingUserName)
        .phone(phone)
        .photoUrl(photoUrl)
        .build();

    var context = Context.builder().requester(REQUESTER).build();
    var processInput = new Input<>(registerInput, context);

    var existingUser = User.builder()
        .id(Id.of(email))
        .name(existingUsername)
        .language(Language.EN)
        .phone(phone)
        .photoUrl(photoUrl)
        .build();

    when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));
    when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    var result = underTest.process(processInput);

    // Then
    assertTrue(result.isSuccess());
    verify(userRepository).findByEmail(email);

    var createdUser = result.data().get();
    assertEquals(email, createdUser.getId().value());
    assertEquals(updatingUserName, createdUser.getName());
    assertEquals(phone, createdUser.getPhone());
    assertEquals(photoUrl, createdUser.getPhotoUrl());
    assertEquals(Language.EN, createdUser.getLanguage());

    var userCaptor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(userCaptor.capture());
    var savedUser = userCaptor.getValue();
    assertEquals(email, savedUser.getId().value());
    assertEquals(updatingUserName, savedUser.getName());
    assertEquals(phone, savedUser.getPhone());
    assertEquals(photoUrl, savedUser.getPhotoUrl());
    assertEquals(Language.EN, savedUser.getLanguage());
  }

  @Test
  public void givenValidRegisterUserRequestWhenUserExistsWithNoChangesThenShouldNotSaveAndSuccess() {
    // Given
    var username = "User Name";
    var email = "user@email.com";
    var phone = "081234567890";
    var photoUrl = "http://photo.url/user";
    var registerInput = UserRegistrationInput.builder()
        .email(email)
        .name(username)
        .phone(phone)
        .photoUrl(photoUrl)
        .build();

    var context = Context.builder().requester(REQUESTER).build();
    var processInput = new Input<>(registerInput, context);

    var existingUser = User.builder()
        .id(Id.of(email))
        .name(username)
        .language(Language.EN)
        .phone(phone)
        .photoUrl(photoUrl)
        .build();

    when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));
    when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    var result = underTest.process(processInput);

    // Then
    assertTrue(result.isSuccess());
    verify(userRepository).findByEmail(email);

    var createdUser = result.data().get();
    assertEquals(email, createdUser.getId().value());
    assertEquals(username, createdUser.getName());
    assertEquals(phone, createdUser.getPhone());
    assertEquals(photoUrl, createdUser.getPhotoUrl());
    assertEquals(Language.EN, createdUser.getLanguage());

    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  public void givenValidRegisterUserRequestWhenPortThrowsErrorThenShouldFail() {
    // Given
    when(userRepository.save(any())).thenThrow(new RuntimeException("An error occured"));

    var registerInput = UserRegistrationInput.builder().build();
    var context = Context.builder().requester(REQUESTER).build();
    var processInput = new Input<>(registerInput, context);

    // When
    var result = underTest.process(processInput);

    // Then
    assertFalse(result.isSuccess());
    assertEquals(Code.SERVER_ERROR, result.error().get().code());
    assertEquals("An error occured", result.error().get().message());
  }
}
