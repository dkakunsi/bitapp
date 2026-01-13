package io.dkakunsi.lab.javalin.endpoint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import io.dkakunsi.bitapp.common.AppError;
import io.dkakunsi.bitapp.common.AppError.Code;
import io.dkakunsi.bitapp.common.Context;
import io.dkakunsi.bitapp.common.usecase.Result;
import io.dkakunsi.bitapp.user.dto.UpdateUserLanguageInput;
import io.dkakunsi.bitapp.user.dto.UpdateUserLanguageResult;
import io.dkakunsi.bitapp.user.model.User.Language;
import io.dkakunsi.bitapp.user.usecase.UpdateUserLanguage;
import io.dkakunsi.lab.javalin.JavalinServer;
import kong.unirest.Unirest;

class UpdateUserLanguageJavalinEndpointTest {

  private static final String BASE_URL = "http://localhost:20004";

  private static UpdateUserLanguage usecase;

  private static JavalinServer server;

  @BeforeAll
  static void setup() throws Exception {
    usecase = mock(UpdateUserLanguage.class);
    var endpoint = new UpdateUserLanguageJavalinEndpoint(usecase)
        .withValidator();
    server = JavalinServer.of(20004);
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
  @SuppressWarnings("unchecked")
  @Test
  void givenValidUpdateLanguageToIdRequest_WhenRequested_ThenShouldOkAndReturnUpdatedUser() {
    // Given
    var email = "user@email.com";
    var result = mock(Result.class);
    when(result.isSuccess()).thenReturn(true);
    when(result.isEmpty()).thenReturn(false);
    when(result.isFailed()).thenReturn(false);
    when(result.data()).thenReturn(Optional.of(UpdateUserLanguageResult.builder()
        .email(email)
        .language(Language.ID)
        .build()));
    when(usecase.process(any(Context.class), any(UpdateUserLanguageInput.class))).thenReturn(result);

    // When
    var response = Unirest.patch(BASE_URL + "/users/" + email + "/language/ID").asString();

    // Then
    assertEquals(200, response.getStatus());

    var responseBody = response.getBody();
    assertNotNull(responseBody);
    assertTrue(responseBody.contains("\"email\":\"user@email.com\""));
    assertTrue(responseBody.contains("\"language\":\"ID\""));
  }

  /**
   * <b>Given</b> a valid update language request to change language to EN<br>
   * <b>When</b> the PATCH /users/{email}/language endpoint is called<br>
   * <b>Then</b> the language should be updated and return status 200 with the
   * updated result
   */
  @SuppressWarnings("unchecked")
  @Test
  void givenValidUpdateLanguageToEnRequest_WhenRequested_ThenShouldOkAndReturnUpdatedUser() {
    // Given
    var email = "user@email.com";
    var result = mock(Result.class);
    when(result.isSuccess()).thenReturn(true);
    when(result.isEmpty()).thenReturn(false);
    when(result.isFailed()).thenReturn(false);
    when(result.data()).thenReturn(Optional.of(UpdateUserLanguageResult.builder()
        .email(email)
        .language(Language.EN)
        .build()));
    when(usecase.process(any(Context.class), any(UpdateUserLanguageInput.class))).thenReturn(result);

    // When
    var response = Unirest.patch(BASE_URL + "/users/" + email + "/language/EN").asString();

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
  @SuppressWarnings("unchecked")
  @Test
  void givenUpdateLanguageRequest_WhenRequested_ThenShouldBuildInputWithEmailFromPath() {
    // Given
    var email = "test@example.com";
    var result = mock(Result.class);
    when(result.isSuccess()).thenReturn(true);
    when(result.isEmpty()).thenReturn(false);
    when(result.isFailed()).thenReturn(false);
    when(result.data()).thenReturn(Optional.of(UpdateUserLanguageResult.builder()
        .email(email)
        .language(Language.ID)
        .build()));
    when(usecase.process(any(Context.class), any(UpdateUserLanguageInput.class))).thenReturn(result);

    // When
    var response = Unirest.patch(BASE_URL + "/users/" + email + "/language/ID").asString();

    // Then
    assertEquals(200, response.getStatus());

    ArgumentCaptor<UpdateUserLanguageInput> inputCaptor = ArgumentCaptor.forClass(UpdateUserLanguageInput.class);
    verify(usecase, atLeast(0)).process(any(Context.class), inputCaptor.capture());

    var capturedInput = inputCaptor.getValue();
    assertEquals(email, capturedInput.email());
    assertEquals(Language.ID, capturedInput.language());
  }

  /**
   * <b>Given</b> a valid update language request with empty result<br>
   * <b>When</b> the PATCH /users/{email}/language endpoint is called<br>
   * <b>Then</b> should return status 200 with empty response body
   */
  @SuppressWarnings("unchecked")
  @Test
  void givenValidUpdateLanguageRequestAndEmptyOutput_WhenRequested_ThenShouldOkWithEmptyResponse() {
    // Given
    var email = "user@email.com";
    var output = mock(Result.class);
    when(output.isSuccess()).thenReturn(true);
    when(output.isEmpty()).thenReturn(true);
    when(output.isFailed()).thenReturn(false);
    when(usecase.process(any(Context.class), any(UpdateUserLanguageInput.class))).thenReturn(output);

    // When
    var response = Unirest.patch(BASE_URL + "/users/" + email + "/language/ID").asString();

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
  @SuppressWarnings("unchecked")
  @Test
  void givenUpdateLanguageRequestForNonExistentUser_WhenRequested_ThenShouldReturn404() {
    // Given
    var email = "nonexistent@email.com";
    var output = mock(Result.class);
    when(output.isSuccess()).thenReturn(false);
    when(output.isFailed()).thenReturn(true);
    when(output.error()).thenReturn(Optional.of(new AppError(Code.NOT_FOUND, "User not found")));
    when(usecase.process(any(Context.class), any(UpdateUserLanguageInput.class))).thenReturn(output);

    // When
    var response = Unirest.patch(BASE_URL + "/users/" + email + "/language/ID").asString();

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
  @SuppressWarnings("unchecked")
  @Test
  void givenUpdateLanguageRequestAndProcessReturnsError_WhenRequested_ThenShouldReturn500() {
    // Given
    var email = "user@email.com";
    var output = mock(Result.class);
    when(output.isSuccess()).thenReturn(false);
    when(output.isFailed()).thenReturn(true);
    when(output.error()).thenReturn(Optional.of(new AppError(Code.SERVER_ERROR, "Database connection failed")));
    when(usecase.process(any(Context.class), any(UpdateUserLanguageInput.class))).thenReturn(output);

    // When
    var response = Unirest.patch(BASE_URL + "/users/" + email + "/language/ID").asString();

    // Then
    assertEquals(500, response.getStatus());

    var responseBody = response.getBody();
    assertEquals("Database connection failed", responseBody);
  }

  /**
   * <b>Given</b> an update language request with invalid data<br>
   * <b>When</b> the PATCH /users/{email}/language endpoint is called<br>
   * <b>Then</b> should return status 400 with error message
   */
  @SuppressWarnings("unchecked")
  @Test
  void givenUpdateLanguageRequestWithInvalidData_WhenRequested_ThenShouldReturn400() {
    // Given
    var email = "user@email.com";
    var output = mock(Result.class);
    when(output.isSuccess()).thenReturn(false);
    when(output.isFailed()).thenReturn(true);
    when(output.error()).thenReturn(Optional.of(new AppError(Code.BAD_REQUEST, "Invalid language")));
    when(usecase.process(any(Context.class), any(UpdateUserLanguageInput.class))).thenReturn(output);

    // When
    var response = Unirest.patch(BASE_URL + "/users/" + email + "/language/INVALID_LANG").asString();

    // Then
    assertEquals(400, response.getStatus());

    var responseBody = response.getBody();
    assertEquals("Invalid language: INVALID_LANG", responseBody);
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
    var response = Unirest.patch(BASE_URL + "/users/invalid/wrongpath").asString();

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
    var response = Unirest.post(BASE_URL + "/users/" + email + "/language/ID").asString();

    // Then
    assertEquals(404, response.getStatus());
  }

  /**
   * <b>Given</b> an update language request with different email addresses<br>
   * <b>When</b> the PATCH /users/{email}/language endpoint is called for each<br>
   * <b>Then</b> each request should update the respective user's language
   */
  @SuppressWarnings("unchecked")
  @Test
  void givenUpdateLanguageRequestsForDifferentUsers_WhenRequested_ThenShouldUpdateRespectiveUsers() {
    // Given
    var email1 = "user1@email.com";
    var email2 = "user2@email.com";

    var result1 = mock(Result.class);
    when(result1.isSuccess()).thenReturn(true);
    when(result1.isEmpty()).thenReturn(false);
    when(result1.isFailed()).thenReturn(false);
    when(result1.data()).thenReturn(Optional.of(UpdateUserLanguageResult.builder()
        .email(email1)
        .language(Language.ID)
        .build()));

    var result2 = mock(Result.class);
    when(result2.isSuccess()).thenReturn(true);
    when(result2.isEmpty()).thenReturn(false);
    when(result2.isFailed()).thenReturn(false);
    when(result2.data()).thenReturn(Optional.of(UpdateUserLanguageResult.builder()
        .email(email2)
        .language(Language.ID)
        .build()));

    when(usecase.process(any(Context.class), any(UpdateUserLanguageInput.class)))
        .thenReturn(result1)
        .thenReturn(result2);

    // When
    var response1 = Unirest.patch(BASE_URL + "/users/" + email1 + "/language/ID").asString();
    var response2 = Unirest.patch(BASE_URL + "/users/" + email2 + "/language/ID").asString();

    // Then
    assertEquals(200, response1.getStatus());
    assertTrue(response1.getBody().contains("\"email\":\"user1@email.com\""));

    assertEquals(200, response2.getStatus());
    assertTrue(response2.getBody().contains("\"email\":\"user2@email.com\""));
  }
}
