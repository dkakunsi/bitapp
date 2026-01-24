package io.dkakunsi.bitapp.account.endpoint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.dkakunsi.bitapp.account.dto.AccountResult;
import io.dkakunsi.bitapp.account.dto.CreateAccountInput;
import io.dkakunsi.bitapp.account.entity.Account;
import io.dkakunsi.bitapp.account.usecase.CreateAccount;
import io.dkakunsi.bitapp.common.AppError.Code;
import io.dkakunsi.bitapp.common.Context;
import io.dkakunsi.bitapp.domain.usecase.Result;
import io.dkakunsi.bitapp.javalin.JavalinServer;
import kong.unirest.Unirest;

class CreateAccountEndpointTest {

  private static final int PORT = 20001;

  private static String baseUrl;

  private static CreateAccount usecase;

  private static JavalinServer server;

  @BeforeAll
  static void setup() throws Exception {
    baseUrl = "http://localhost:" + PORT;
    usecase = mock(CreateAccount.class);
    var endpoint = new CreateAccountEndpoint(usecase);
    server = JavalinServer.of(PORT);
    server.addEndpoint(endpoint);
    server.start();
  }

  @AfterAll
  static void destroy() {
    server.stop();
  }

  @Test
  void givenValidAccountRequest_WhenRequested_ThenShouldReturn200AndAccount() {
    // Given
    var createAccountResult = AccountResult.builder()
        .id("account-123")
        .name("Savings Account")
        .type(Account.Type.BANK.name())
        .themeColor("#FF5733")
        .balance(BigDecimal.ZERO)
        .user("user@email.com")
        .build();
    var result = Result.success(createAccountResult);
    when(usecase.process(any(Context.class), any(CreateAccountInput.class))).thenReturn(result);

    var requestBody = """
        {
          "name":"Savings Account",
          "themeColor":"#FF5733",
          "type":"BANK"
        }
        """;

    // When
    var response = Unirest.post(baseUrl + "/accounts").body(requestBody).asString();

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

  @Test
  void givenValidRequestWithEmptyOutput_WhenRequested_ThenShouldReturn200() {
    // Given
    var result = Result.<AccountResult>success();
    when(usecase.process(any(Context.class), any(CreateAccountInput.class))).thenReturn(result);

    var requestBody = """
        {
          "name":"Checking Account",
          "themeColor":"#3357FF",
          "type":"CASH"
        }
        """;

    // When
    var response = Unirest.post(baseUrl + "/accounts").body(requestBody).asString();

    // Then
    assertEquals(200, response.getStatus());
    assertEquals("", response.getBody());
  }

  @Test
  void givenServerError_WhenRequested_ThenShouldReturn500() {
    // Given
    var result = Result.<AccountResult>failure(Code.SERVER_ERROR, "Failed to save account");
    when(usecase.process(any(Context.class), any(CreateAccountInput.class))).thenReturn(result);

    var requestBody = """
        {
          "name":"Investment Account",
          "themeColor":"#33FF57",
          "type":"EWALLET"
        }
        """;

    // When
    var response = Unirest.post(baseUrl + "/accounts").body(requestBody).asString();

    // Then
    assertEquals(500, response.getStatus());
    assertEquals("Failed to save account", response.getBody());
  }

  @Test
  void givenDuplicateAccount_WhenRequested_ThenShouldReturn400() {
    // Given
    var result = Result.<AccountResult>failure(Code.BAD_REQUEST, "Account with this name already exists");
    when(usecase.process(any(Context.class), any(CreateAccountInput.class))).thenReturn(result);

    var requestBody = """
        {
          "name":"Duplicate Account",
          "themeColor":"#5733FF",
          "type":"CASH"
        }
        """;

    // When
    var response = Unirest.post(baseUrl + "/accounts").body(requestBody).asString();

    // Then
    assertEquals(400, response.getStatus());
    assertEquals("Account with this name already exists", response.getBody());
  }
}
