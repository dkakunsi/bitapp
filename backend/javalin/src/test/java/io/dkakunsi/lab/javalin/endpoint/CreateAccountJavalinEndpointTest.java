package io.dkakunsi.lab.javalin.endpoint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.dkakunsi.bitapp.account.dto.CreateAccountInput;
import io.dkakunsi.bitapp.account.dto.CreateAccountResult;
import io.dkakunsi.bitapp.account.model.Account;
import io.dkakunsi.bitapp.common.AppError;
import io.dkakunsi.bitapp.common.AppError.Code;
import io.dkakunsi.bitapp.common.Context;
import io.dkakunsi.bitapp.common.usecase.Result;
import io.dkakunsi.bitapp.common.usecase.UseCase;
import io.dkakunsi.lab.javalin.JavalinServer;
import kong.unirest.Unirest;

class CreateAccountJavalinEndpointTest {

  private static final String BASE_URL = "http://localhost:20003";

  private static UseCase<CreateAccountInput, CreateAccountResult> usecase;

  private static JavalinServer server;

  @SuppressWarnings("unchecked")
  @BeforeAll
  static void setup() throws Exception {
    usecase = (UseCase<CreateAccountInput, CreateAccountResult>) mock(UseCase.class);
    var endpoint = new CreateAccountJavalinEndpoint(usecase, null);
    server = JavalinServer.of(20003);
    server.addEndpoint(endpoint);
    server.start();
  }

  @AfterAll
  static void destroy() {
    server.stop();
  }

  @SuppressWarnings("unchecked")
  @Test
  void givenValidAccountRequest_WhenRequested_ThenShouldReturn201AndAccount() {
    // Given
    var body = """
        {"name":"Savings Account","themeColor":"#FF5733","type":"BANK"}
        """;
    var result = mock(Result.class);
    when(result.isSuccess()).thenReturn(true);
    when(result.isEmpty()).thenReturn(false);
    when(result.isFailed()).thenReturn(false);
    when(result.data()).thenReturn(Optional.of(CreateAccountResult.builder()
        .id("account-123")
        .name("Savings Account")
        .type(Account.Type.BANK)
        .themeColor("#FF5733")
        .balance(BigDecimal.ZERO)
        .user("user@email.com")
        .build()));
    when(usecase.process(any(Context.class), any(CreateAccountInput.class))).thenReturn(result);

    // When
    var response = Unirest.post(BASE_URL + "/accounts").body(body).asString();

    // Then
    assertEquals(200, response.getStatus());

    var responseBody = response.getBody();
    assertNotNull(responseBody);
    assertTrue(responseBody.contains("\"id\":\"account-123\""));
    assertTrue(responseBody.contains("\"name\":\"Savings Account\""));
    assertTrue(responseBody.contains("\"themeColor\":\"#FF5733\""));
    assertTrue(responseBody.contains("\"type\":\"BANK\""));
    assertTrue(responseBody.contains("\"balance\":0"));
  }

  @SuppressWarnings("unchecked")
  @Test
  void givenValidRequestWithEmptyOutput_WhenRequested_ThenShouldReturn200() {
    // Given
    var body = """
        {"name":"Checking Account","themeColor":"#3357FF","type":"CASH"}
        """;
    var result = mock(Result.class);
    when(result.isSuccess()).thenReturn(true);
    when(result.isEmpty()).thenReturn(true);
    when(result.isFailed()).thenReturn(false);
    when(usecase.process(any(Context.class), any(CreateAccountInput.class))).thenReturn(result);

    // When
    var response = Unirest.post(BASE_URL + "/accounts").body(body).asString();

    // Then
    assertEquals(200, response.getStatus());
  }

  @SuppressWarnings("unchecked")
  @Test
  void givenInvalidRequest_WhenRequested_ThenShouldReturn400() {
    // Given
    var body = """
        {"name":"","themeColor":"#FF5733","type":"BANK"}
        """;
    var error = new AppError(Code.BAD_REQUEST, "Account name cannot be empty");
    var result = mock(Result.class);
    when(result.isSuccess()).thenReturn(false);
    when(result.isEmpty()).thenReturn(false);
    when(result.isFailed()).thenReturn(true);
    when(result.error()).thenReturn(Optional.of(error));
    when(usecase.process(any(Context.class), any(CreateAccountInput.class))).thenReturn(result);

    // When
    var response = Unirest.post(BASE_URL + "/accounts").body(body).asString();

    // Then
    assertEquals(400, response.getStatus());
    assertEquals("Account name cannot be empty", response.getBody());
  }

  @SuppressWarnings("unchecked")
  @Test
  void givenServerError_WhenRequested_ThenShouldReturn500() {
    // Given
    var body = """
        {"name":"Investment Account","themeColor":"#33FF57","type":"EWALLET"}
        """;
    var error = new AppError(Code.SERVER_ERROR, "Failed to save account");
    var result = mock(Result.class);
    when(result.isSuccess()).thenReturn(false);
    when(result.isEmpty()).thenReturn(false);
    when(result.isFailed()).thenReturn(true);
    when(result.error()).thenReturn(Optional.of(error));
    when(usecase.process(any(Context.class), any(CreateAccountInput.class))).thenReturn(result);

    // When
    var response = Unirest.post(BASE_URL + "/accounts").body(body).asString();

    // Then
    assertEquals(500, response.getStatus());
    assertEquals("Failed to save account", response.getBody());
  }

  @SuppressWarnings("unchecked")
  @Test
  void givenDuplicateAccount_WhenRequested_ThenShouldReturn409() {
    // Given
    var body = """
        {"name":"Duplicate Account","themeColor":"#5733FF","type":"CASH"}
        """;
    var error = new AppError(Code.BAD_REQUEST, "Account with this name already exists");
    var result = mock(Result.class);
    when(result.isSuccess()).thenReturn(false);
    when(result.isEmpty()).thenReturn(false);
    when(result.isFailed()).thenReturn(true);
    when(result.error()).thenReturn(Optional.of(error));
    when(usecase.process(any(Context.class), any(CreateAccountInput.class))).thenReturn(result);

    // When
    var response = Unirest.post(BASE_URL + "/accounts").body(body).asString();

    // Then
    assertEquals(400, response.getStatus());
    assertEquals("Account with this name already exists", response.getBody());
  }
}
