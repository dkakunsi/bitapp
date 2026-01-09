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
import io.dkakunsi.bitapp.common.Id;
import io.dkakunsi.bitapp.common.usecase.Input;
import io.dkakunsi.bitapp.common.usecase.Result;
import io.dkakunsi.bitapp.common.usecase.UseCase;
import io.dkakunsi.bitapp.user.dto.UserRetrievalInput;
import io.dkakunsi.bitapp.user.model.User;
import io.dkakunsi.bitapp.user.model.User.Language;
import kong.unirest.Unirest;

class UserRetrievalJavalinEndpointTest {

  private static final String BASE_URL = "http://localhost:20002";

  private static UseCase<UserRetrievalInput, User> usecase;

  private static JavalinServer server;

  @SuppressWarnings("unchecked")
  @BeforeAll
  static void setup() throws Exception {
    usecase = (UseCase<UserRetrievalInput, User>) mock(UseCase.class);
    var endpoint = new UserRetrievalJavalinEndpoint(usecase, null);
    server = JavalinServer.of(20002);
    server.addEndpoint(endpoint);
    server.start();
  }

  @AfterAll
  static void destroy() {
    server.stop();
  }

  @SuppressWarnings("unchecked")
  @Test
  void givenValidEmail_WhenRequested_ThenShouldReturnUser() {
    // Given
    var email = "user@email.com";
    var result = mock(Result.class);
    when(result.isSuccess()).thenReturn(true);
    when(result.isEmpty()).thenReturn(false);
    when(result.isFailed()).thenReturn(false);
    when(result.data()).thenReturn(Optional.of(User.builder()
        .id(Id.of(email))
        .name("User Name")
        .phone("081234567890")
        .photoUrl("http://photo.url/user")
        .language(Language.EN)
        .build()));
    when(usecase.process(any(Input.class))).thenReturn(result);

    // When
    var response = Unirest.get(BASE_URL + "/users/" + email).asString();

    // Then
    assertEquals(200, response.getStatus());

    var responseBody = response.getBody();
    assertNotNull(responseBody);
    assertTrue(responseBody.contains("\"name\":\"User Name\""));
    assertTrue(responseBody.contains("\"phone\":\"081234567890\""));
    assertTrue(responseBody.contains("\"photoUrl\":\"http://photo.url/user\""));
  }

  @SuppressWarnings("unchecked")
  @Test
  void givenNonExistentEmail_WhenRequested_ThenShouldReturn404() {
    // Given
    var email = "nonexistent@email.com";
    var result = mock(Result.class);
    when(result.isSuccess()).thenReturn(true);
    when(result.isEmpty()).thenReturn(true);
    when(result.isFailed()).thenReturn(false);
    when(usecase.process(any(Input.class))).thenReturn(result);

    // When
    var response = Unirest.get(BASE_URL + "/users/" + email).asString();

    // Then
    assertEquals(404, response.getStatus());
    assertEquals("User not found", response.getBody());
  }

  @SuppressWarnings("unchecked")
  @Test
  void givenServerError_WhenRequested_ThenShouldReturn500() {
    // Given
    var email = "user@email.com";
    var error = new AppError(Code.SERVER_ERROR, "Database connection failed");
    var result = mock(Result.class);
    when(result.isSuccess()).thenReturn(false);
    when(result.isEmpty()).thenReturn(false);
    when(result.isFailed()).thenReturn(true);
    when(result.error()).thenReturn(Optional.of(error));
    when(usecase.process(any(Input.class))).thenReturn(result);

    // When
    var response = Unirest.get(BASE_URL + "/users/" + email).asString();

    // Then
    assertEquals(500, response.getStatus());
    assertEquals("Database connection failed", response.getBody());
  }
}
