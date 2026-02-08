package io.dkakunsi.bitapp.user.endpoint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.dkakunsi.bitapp.common.AppError.Code;
import io.dkakunsi.bitapp.domain.usecase.Result;
import io.dkakunsi.bitapp.javalin.JavalinServer;
import io.dkakunsi.bitapp.user.dto.UserResult;
import io.dkakunsi.bitapp.user.entity.User.Language;
import io.dkakunsi.bitapp.user.usecase.GetUser;
import kong.unirest.Unirest;

class GetUserEndpointTest {

  private static final int PORT = 20003;

  private static String baseUrl;

  private static GetUser usecase;

  private static JavalinServer server;

  @BeforeAll
  static void setup() throws Exception {
    baseUrl = "http://localhost:" + PORT;
    usecase = mock(GetUser.class);
    var endpoint = new GetUserEndpoint(usecase);
    server = JavalinServer.of(PORT);
    server.addEndpoint(endpoint);
    server.start();
  }

  @AfterAll
  static void destroy() {
    server.stop();
  }

  @Test
  void givenValidEmail_WhenRequested_ThenShouldReturnUser() {
    // Given
    var email = "user@email.com";
    var getResult = UserResult.builder()
        .email(email)
        .name("User Name")
        .phone("081234567890")
        .photoUrl("http://photo.url/user")
        .language(Language.EN.name())
        .build();
    var result = Result.success(getResult);
    when(usecase.process(any(String.class))).thenReturn(result);

    // When
    var response = Unirest.get(baseUrl + "/users/" + email).asString();

    // Then
    assertEquals(200, response.getStatus());

    var responseBody = response.getBody();
    assertNotNull(responseBody);
    var resultBody = new JSONObject(responseBody);
    assertEquals(email, resultBody.getString("email"));
    assertEquals("User Name", resultBody.getString("name"));
    assertEquals("081234567890", resultBody.getString("phone"));
    assertEquals("http://photo.url/user", resultBody.getString("photoUrl"));
    assertEquals("EN", resultBody.getString("language"));
  }

  @Test
  void givenNonExistentEmail_WhenRequested_ThenShouldReturn404() {
    // Given
    var email = "nonexistent@email.com";
    var result = Result.<UserResult>failure(Code.NOT_FOUND, "User not found");
    when(usecase.process(any(String.class))).thenReturn(result);

    // When
    var response = Unirest.get(baseUrl + "/users/" + email).asString();

    // Then
    assertEquals(404, response.getStatus());
    assertEquals("User not found", response.getBody());
  }

  @Test
  void givenServerError_WhenRequested_ThenShouldReturn500() {
    // Given
    var email = "user@email.com";
    var result = Result.<UserResult>failure(Code.SERVER_ERROR, "Database connection failed");
    when(usecase.process(any(String.class))).thenReturn(result);

    // When
    var response = Unirest.get(baseUrl + "/users/" + email).asString();

    // Then
    assertEquals(500, response.getStatus());
    assertEquals("Database connection failed", response.getBody());
  }
}
