package io.dkakunsi.bitapp.user.endpoint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import io.dkakunsi.bitapp.user.dto.RegisterUserInput;
import io.dkakunsi.bitapp.user.dto.UserResult;
import io.dkakunsi.bitapp.user.entity.User.Language;
import io.dkakunsi.bitapp.user.usecase.RegisterUser;
import kong.unirest.Unirest;

class RegisterUserEndpointTest {

  private static final int PORT = 20004;

  private static String baseUrl;

  private static RegisterUser usecase;

  private static JavalinServer server;

  @BeforeAll
  static void setup() throws Exception {
    baseUrl = "http://localhost:" + PORT;
    usecase = mock(RegisterUser.class);
    var endpoint = new RegisterUserEndpoint(usecase);
    server = JavalinServer.of(PORT);
    server.addEndpoint(endpoint);
    server.start();
  }

  @AfterAll
  static void destroy() {
    server.stop();
  }

  @Test
  void givenValidRegistrationRequest_WhenRequested_ThenShouldOkAndReturnUser() {
    // Given
    var registerResult = UserResult.builder()
        .email("user@email.com")
        .name("User Name")
        .phone("081234567890")
        .photoUrl("http://photo.url/user")
        .language(Language.EN.name())
        .build();
    var result = Result.success(registerResult);
    when(usecase.process(any(RegisterUserInput.class))).thenReturn(result);

    var requestBody = """
        {
          "email":"user@email.com",
          "name":"User Name",
          "phone":"081234567890",
          "photoUrl":"http://photo.url/user"
        }
        """;

    // When
    var response = Unirest.post(baseUrl + "/users").body(requestBody).asString();

    // Then
    assertEquals(200, response.getStatus());

    var responseBody = response.getBody();
    assertNotNull(responseBody);
    var resultBody = new JSONObject(responseBody);
    assertEquals("user@email.com", resultBody.getString("email"));
    assertEquals("User Name", resultBody.getString("name"));
    assertEquals("081234567890", resultBody.getString("phone"));
    assertEquals("http://photo.url/user", resultBody.getString("photoUrl"));
    assertEquals("EN", resultBody.getString("language"));
  }

  @Test
  void givenValidRegistrationRequestAndEmptyResult_WhenRequested_ThenShouldOkWithEmptyResponse() {
    // Given
    var result = Result.<UserResult>success();
    when(usecase.process(any(RegisterUserInput.class))).thenReturn(result);

    var requestBody = """
        {
          "email":"user@email.com",
          "name":"User Name",
          "phone":"081234567890",
          "photoUrl":"http://photo.url/user"
        }
        """;

    // When
    var response = Unirest.post(baseUrl + "/users").body(requestBody).asString();

    // Then
    assertEquals(200, response.getStatus());

    var responseBody = response.getBody();
    assertEquals("", responseBody);
  }

  @Test
  void givenValidRegistrationRequestAndProcessReturnsError_WhenRequested_ThenShouldReturnProperMessage() {
    // Given
    var result = Result.<UserResult>failure(Code.SERVER_ERROR, "Database error");
    when(usecase.process(any(RegisterUserInput.class))).thenReturn(result);

    var requestBody = """
        {
          "email":"user@email.com",
          "name":"User Name",
          "phone":"081234567890",
          "photoUrl":"http://photo.url/user"
        }
        """;

    // When
    var response = Unirest.post(baseUrl + "/users").body(requestBody).asString();

    // Then
    assertEquals(500, response.getStatus());

    var responseBody = response.getBody();
    assertEquals("Database error", responseBody);
  }

  @Test
  void givenValidRegistrationRequestAndProcessReturnsBadRequest_WhenRequested_ThenShouldReturn400() {
    // Given
    var result = Result.<UserResult>failure(Code.BAD_REQUEST, "email: must be a well-formed email address");
    when(usecase.process(any(RegisterUserInput.class))).thenReturn(result);

    var requestBody = """
        {
          "email":"invalid-email",
          "name":"User Name"
        }
        """;

    // When
    var response = Unirest.post(baseUrl + "/users").body(requestBody).asString();

    // Then
    assertEquals(400, response.getStatus());

    var responseBody = response.getBody();
    assertEquals("email: must be a well-formed email address", responseBody);
  }

  @Test
  void givenInvalidTargetUrl_WhenRequested_ThenShouldReturnNotFound() {
    // Given
    var requestBody = """
        {
          "email":"user@email.com",
          "name":"User Name"
        }
        """;

    // When
    var response = Unirest.put(baseUrl + "/users/invalid").body(requestBody).asString();

    // Then
    assertEquals(404, response.getStatus());
  }

  @Test
  void givenMinimalValidRequest_WhenRequested_ThenShouldOkAndReturnUser() {
    // Given
    var registerUserResult = UserResult.builder()
        .email("user@email.com")
        .name("User Name")
        .phone(null)
        .photoUrl(null)
        .language(Language.EN.name())
        .build();
    var result = Result.success(registerUserResult);
    when(usecase.process(any(RegisterUserInput.class))).thenReturn(result);

    var requestBody = """
        {
          "email":"user@email.com",
          "name":"User Name"
        }
        """;

    // When
    var response = Unirest.post(baseUrl + "/users").body(requestBody).asString();

    // Then
    assertEquals(200, response.getStatus());

    var responseBody = response.getBody();
    assertNotNull(responseBody);
    var resultBody = new JSONObject(responseBody);
    assertEquals("user@email.com", resultBody.getString("email"));
    assertEquals("User Name", resultBody.getString("name"));
    assertTrue(resultBody.isNull("phone"));
    assertTrue(resultBody.isNull("photoUrl"));
    assertEquals("EN", resultBody.getString("language"));
  }
}
