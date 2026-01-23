package io.dkakunsi.bitapp.user.endpoint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import io.dkakunsi.bitapp.common.AppError.Code;
import io.dkakunsi.bitapp.common.Context;
import io.dkakunsi.bitapp.common.usecase.Result;
import io.dkakunsi.bitapp.javalin.JavalinServer;
import io.dkakunsi.bitapp.user.dto.UpdateUserLanguageInput;
import io.dkakunsi.bitapp.user.dto.UserResult;
import io.dkakunsi.bitapp.user.entity.User.Language;
import io.dkakunsi.bitapp.user.usecase.UpdateUserLanguage;
import kong.unirest.Unirest;

class UpdateUserLanguageEndpointTest {

  private static final int PORT = 20006;

  private static String baseUrl;

  private static UpdateUserLanguage usecase;

  private static JavalinServer server;

  @BeforeAll
  static void setup() throws Exception {
    baseUrl = "http://localhost:" + PORT;
    usecase = mock(UpdateUserLanguage.class);
    var endpoint = new UpdateUserLanguageEndpoint(usecase);
    server = JavalinServer.of(PORT);
    server.addEndpoint(endpoint);
    server.start();
  }

  @AfterAll
  static void destroy() {
    server.stop();
  }

  /**
   * <b>Given</b> a valid update language request to change language to ID<br>
   * <b>When</b> the PATCH /users/{email}/language endpoint is called<br>
   * <b>Then</b> the language should be updated and return status 200 with the
   * updated result
   */
  @Test
  void givenValidUpdateLanguageToIdRequest_WhenRequested_ThenShouldOkAndReturnUpdatedUser() {
    // Given
    var email = "user@email.com";
    var updateResult = UserResult.builder()
        .email(email)
        .language(Language.ID.name())
        .build();
    var result = Result.success(updateResult);
    when(usecase.process(any(Context.class), any(UpdateUserLanguageInput.class))).thenReturn(result);

    // When
    var response = Unirest.patch(baseUrl + "/users/" + email + "/language/ID").asString();

    // Then
    assertEquals(200, response.getStatus());

    var responseBody = response.getBody();
    assertNotNull(responseBody);
    var resultBody = new JSONObject(responseBody);
    assertEquals(email, resultBody.getString("email"));
    assertEquals("ID", resultBody.getString("language"));
  }

  /**
   * <b>Given</b> a valid update language request to change language to EN<br>
   * <b>When</b> the PATCH /users/{email}/language endpoint is called<br>
   * <b>Then</b> the language should be updated and return status 200 with the
   * updated result
   */
  @Test
  void givenValidUpdateLanguageToEnRequest_WhenRequested_ThenShouldOkAndReturnUpdatedUser() {
    // Given
    var email = "user@email.com";
    var updateResult = UserResult.builder()
        .email(email)
        .language(Language.EN.name())
        .build();
    var result = Result.success(updateResult);
    when(usecase.process(any(Context.class), any(UpdateUserLanguageInput.class))).thenReturn(result);

    // When
    var response = Unirest.patch(baseUrl + "/users/" + email + "/language/EN").asString();

    // Then
    assertEquals(200, response.getStatus());

    var responseBody = response.getBody();
    assertNotNull(responseBody);
    assertTrue(responseBody.contains("\"email\":\"user@email.com\""));
    assertTrue(responseBody.contains("\"language\":\"EN\""));
  }

  /**
   * <b>Given</b> an update language request with email from path parameter<br>
   * <b>When</b> the endpoint processes the request<br>
   * <b>Then</b> the input should be built with email from path parameter
   */
  @Test
  void givenUpdateLanguageRequest_WhenRequested_ThenShouldBuildInputWithEmailFromPath() {
    // Given
    var email = "test@example.com";
    var updateResult = UserResult.builder()
        .email(email)
        .language(Language.ID.name())
        .build();
    var result = Result.success(updateResult);
    when(usecase.process(any(Context.class), any(UpdateUserLanguageInput.class))).thenReturn(result);

    // When
    var response = Unirest.patch(baseUrl + "/users/" + email + "/language/ID").asString();

    // Then
    assertEquals(200, response.getStatus());

    ArgumentCaptor<UpdateUserLanguageInput> inputCaptor = ArgumentCaptor.forClass(UpdateUserLanguageInput.class);
    verify(usecase, atLeast(0)).process(any(Context.class), inputCaptor.capture());

    var capturedInput = inputCaptor.getValue();
    assertEquals(email, capturedInput.email());
    assertEquals("ID", capturedInput.language());
  }

  /**
   * <b>Given</b> a valid update language request with empty result<br>
   * <b>When</b> the PATCH /users/{email}/language endpoint is called<br>
   * <b>Then</b> should return status 200 with empty response body
   */
  @Test
  void givenValidUpdateLanguageRequestAndEmptyResult_WhenRequested_ThenShouldOkWithEmptyResponse() {
    // Given
    var email = "user@email.com";
    var result = Result.<UserResult>success();
    when(usecase.process(any(Context.class), any(UpdateUserLanguageInput.class))).thenReturn(result);

    // When
    var response = Unirest.patch(baseUrl + "/users/" + email + "/language/ID").asString();

    // Then
    assertEquals(200, response.getStatus());

    var responseBody = response.getBody();
    assertEquals("", responseBody);
  }

  /**
   * <b>Given</b> an update language request for a non-existent user<br>
   * <b>When</b> the PATCH /users/{email}/language endpoint is called<br>
   * <b>Then</b> should return status 404 with "User not found" message
   */
  @Test
  void givenUpdateLanguageRequestForNonExistentUser_WhenRequested_ThenShouldReturn404() {
    // Given
    var email = "nonexistent@email.com";
    var result = Result.<UserResult>failure(Code.NOT_FOUND, "User not found");
    when(usecase.process(any(Context.class), any(UpdateUserLanguageInput.class))).thenReturn(result);

    // When
    var response = Unirest.patch(baseUrl + "/users/" + email + "/language/ID").asString();

    // Then
    assertEquals(404, response.getStatus());

    var responseBody = response.getBody();
    assertEquals("User not found", responseBody);
  }

  /**
   * <b>Given</b> an update language request with server error<br>
   * <b>When</b> the PATCH /users/{email}/language endpoint is called<br>
   * <b>Then</b> should return status 500 with error message
   */
  @Test
  void givenUpdateLanguageRequestAndProcessReturnsError_WhenRequested_ThenShouldReturn500() {
    // Given
    var email = "user@email.com";
    var result = Result.<UserResult>failure(Code.SERVER_ERROR, "Database connection failed");
    when(usecase.process(any(Context.class), any(UpdateUserLanguageInput.class))).thenReturn(result);

    // When
    var response = Unirest.patch(baseUrl + "/users/" + email + "/language/ID").asString();

    // Then
    assertEquals(500, response.getStatus());

    var responseBody = response.getBody();
    assertEquals("Database connection failed", responseBody);
  }

  /**
   * <b>Given</b> a request to an invalid URL path<br>
   * <b>When</b> the request is sent<br>
   * <b>Then</b> should return status 404 (Not Found)
   */
  @Test
  void givenInvalidTargetUrl_WhenRequested_ThenShouldReturnNotFound() {
    // Given
    // When
    var response = Unirest.patch(baseUrl + "/users/invalid/wrongpath").asString();

    // Then
    assertEquals(404, response.getStatus());
  }

  /**
   * <b>Given</b> an update language request using wrong HTTP method<br>
   * <b>When</b> using POST instead of PATCH<br>
   * <b>Then</b> should return status 404 (Method Not Allowed)
   */
  @Test
  void givenUpdateLanguageRequestWithWrongMethod_WhenRequested_ThenShouldReturnNotFound() {
    // Given
    var email = "user@email.com";

    // When
    var response = Unirest.post(baseUrl + "/users/" + email + "/language/ID").asString();

    // Then
    assertEquals(404, response.getStatus());
  }

  /**
   * <b>Given</b> an update language request with different email addresses<br>
   * <b>When</b> the PATCH /users/{email}/language endpoint is called for each<br>
   * <b>Then</b> each request should update the respective user's language
   */
  @Test
  void givenUpdateLanguageRequestsForDifferentUsers_WhenRequested_ThenShouldUpdateRespectiveUsers() {
    // Given
    var email1 = "user1@email.com";
    var updateResult1 = UserResult.builder()
        .email(email1)
        .language(Language.ID.name())
        .build();
    var result1 = Result.success(updateResult1);

    var email2 = "user2@email.com";
    var updateResult2 = UserResult.builder()
        .email(email2)
        .language(Language.ID.name())
        .build();
    var result2 = Result.success(updateResult2);

    when(usecase.process(any(Context.class), any(UpdateUserLanguageInput.class)))
        .thenReturn(result1)
        .thenReturn(result2);

    // When
    var response1 = Unirest.patch(baseUrl + "/users/" + email1 + "/language/ID").asString();
    var response2 = Unirest.patch(baseUrl + "/users/" + email2 + "/language/ID").asString();

    // Then
    assertEquals(200, response1.getStatus());
    assertTrue(response1.getBody().contains("\"email\":\"user1@email.com\""));

    assertEquals(200, response2.getStatus());
    assertTrue(response2.getBody().contains("\"email\":\"user2@email.com\""));
  }
}
