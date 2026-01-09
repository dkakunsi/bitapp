package io.dkakunsi.lab.javalin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.dkakunsi.bitapp.common.AppError;
import io.dkakunsi.bitapp.common.AppError.Code;
import io.dkakunsi.bitapp.common.usecase.Input;
import io.dkakunsi.bitapp.common.usecase.Result;
import io.dkakunsi.bitapp.common.usecase.UseCase;
import io.dkakunsi.bitapp.user.dto.UserRegistrationInput;
import io.dkakunsi.bitapp.user.dto.UserRegistrationOutput;
import io.dkakunsi.bitapp.user.model.User.Language;
import kong.unirest.Unirest;

class UserRegistrationJavalinEndpointTest {

  private static final String BASE_URL = "http://localhost:20001";

  private static UseCase<UserRegistrationInput, UserRegistrationOutput> usecase;

  private static JavalinServer server;

  @SuppressWarnings("unchecked")
  @BeforeAll
  static void setup() throws Exception {
    usecase = (UseCase<UserRegistrationInput, UserRegistrationOutput>) mock(UseCase.class);
    var endpoint = new UserRegistrationJavalinEndpoint(usecase, null);
    server = JavalinServer.of(20001);
    server.addEndpoint(endpoint);
    server.start();
  }

  @AfterAll
  static void destroy() {
    server.stop();
  }

  @SuppressWarnings("unchecked")
  @Test
  void givenValidRegistrationRequest_WhenRequested_ThenShouldOkAndReturnUser() {
    // Given
    var body = """
        {"email":"user@email.com","name":"User Name","phone":"081234567890","photoUrl":"http://photo.url/user"}
        """;
    var result = mock(Result.class);
    when(result.isSuccess()).thenReturn(true);
    when(result.isEmpty()).thenReturn(false);
    when(result.isFailed()).thenReturn(false);
    when(result.data()).thenReturn(Optional.of(UserRegistrationOutput.builder()
        .email("user@email.com")
        .name("User Name")
        .phone("081234567890")
        .photoUrl("http://photo.url/user")
        .language(Language.EN)
        .build()));
    when(usecase.process(any(Input.class))).thenReturn(result);

    // When
    var response = Unirest.post(BASE_URL + "/users").body(body).asString();

    // Then
    assertEquals(201, response.getStatus());

    var responseBody = response.getBody();
    assertNotNull(responseBody);
    assertTrue(responseBody.contains("\"email\":\"user@email.com\""));
    assertTrue(responseBody.contains("\"name\":\"User Name\""));
    assertTrue(responseBody.contains("\"phone\":\"081234567890\""));
    assertTrue(responseBody.contains("\"photoUrl\":\"http://photo.url/user\""));
  }

  @SuppressWarnings("unchecked")
  @Test
  void givenValidRegistrationRequestAndEmptyOutput_WhenRequested_ThenShouldOkWithEmptyResponse() {
    // Given
    var body = """
        {"email":"user@email.com","name":"User Name","phone":"081234567890","photoUrl":"http://photo.url/user"}
        """;
    var output = mock(Result.class);
    when(output.isSuccess()).thenReturn(true);
    when(output.isEmpty()).thenReturn(true);
    when(output.isFailed()).thenReturn(false);
    when(usecase.process(any(Input.class))).thenReturn(output);

    // When
    var response = Unirest.post(BASE_URL + "/users").body(body).asString();

    // Then
    assertEquals(201, response.getStatus());

    var responseBody = response.getBody();
    assertEquals("", responseBody);
  }

  @SuppressWarnings("unchecked")
  @Test
  void givenValidRegistrationRequestAndProcessReturnsError_WhenRequested_ThenShouldReturnProperMessage() {
    // Given
    var body = """
        {"email":"user@email.com","name":"User Name","phone":"081234567890","photoUrl":"http://photo.url/user"}
        """;
    var output = mock(Result.class);
    when(output.isSuccess()).thenReturn(false);
    when(output.isFailed()).thenReturn(true);
    when(output.error()).thenReturn(Optional.of(new AppError(Code.SERVER_ERROR, "Database error")));
    when(usecase.process(any(Input.class))).thenReturn(output);

    // When
    var response = Unirest.post(BASE_URL + "/users").body(body).asString();

    // Then
    assertEquals(500, response.getStatus());

    var responseBody = response.getBody();
    assertEquals("Database error", responseBody);
  }

  @SuppressWarnings("unchecked")
  @Test
  void givenValidRegistrationRequestAndProcessReturnsBadRequest_WhenRequested_ThenShouldReturn400() {
    // Given
    var body = """
        {"email":"invalid-email","name":"User Name"}
        """;
    var output = mock(Result.class);
    when(output.isSuccess()).thenReturn(false);
    when(output.isFailed()).thenReturn(true);
    when(output.error()).thenReturn(Optional.of(new AppError(Code.BAD_REQUEST, "Invalid email format")));
    when(usecase.process(any(Input.class))).thenReturn(output);

    // When
    var response = Unirest.post(BASE_URL + "/users").body(body).asString();

    // Then
    assertEquals(400, response.getStatus());

    var responseBody = response.getBody();
    assertEquals("Invalid email format", responseBody);
  }

  @Test
  void givenInvalidTargetUrl_WhenRequested_ThenShouldReturnNotFound() {
    // Given
    var body = """
        {"email":"user@email.com","name":"User Name"}
        """;

    // When
    var response = Unirest.put(BASE_URL + "/users/invalid").body(body).asString();

    // Then
    assertEquals(404, response.getStatus());
  }

  @SuppressWarnings("unchecked")
  @Test
  void givenMinimalValidRequest_WhenRequested_ThenShouldOkAndReturnUser() {
    // Given
    var body = """
        {"email":"user@email.com","name":"User Name"}
        """;
    var result = mock(Result.class);
    when(result.isSuccess()).thenReturn(true);
    when(result.isEmpty()).thenReturn(false);
    when(result.isFailed()).thenReturn(false);
    when(result.data()).thenReturn(Optional.of(UserRegistrationOutput.builder()
        .email("user@email.com")
        .name("User Name")
        .phone(null)
        .photoUrl(null)
        .language(Language.EN)
        .build()));
    when(usecase.process(any(Input.class))).thenReturn(result);

    // When
    var response = Unirest.post(BASE_URL + "/users").body(body).asString();

    // Then
    assertEquals(201, response.getStatus());

    var responseBody = response.getBody();
    assertNotNull(responseBody);
    assertTrue(responseBody.contains("\"email\":\"user@email.com\""));
    assertTrue(responseBody.contains("\"name\":\"User Name\""));
  }
}
