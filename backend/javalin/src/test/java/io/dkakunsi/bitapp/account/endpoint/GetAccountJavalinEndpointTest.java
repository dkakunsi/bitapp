package io.dkakunsi.bitapp.account.endpoint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.dkakunsi.bitapp.account.dto.AccountResult;
import io.dkakunsi.bitapp.account.usecase.GetAccount;
import io.dkakunsi.bitapp.common.AppError.Code;
import io.dkakunsi.bitapp.common.Context;
import io.dkakunsi.bitapp.common.usecase.Result;
import io.dkakunsi.bitapp.javalin.JavalinServer;
import kong.unirest.Unirest;

class GetAccountJavalinEndpointTest {

  private static final int PORT = 20010;

  private static String baseUrl;

  private static GetAccount usecase;

  private static JavalinServer server;

  @BeforeAll
  static void setup() throws Exception {
    baseUrl = "http://localhost:" + PORT;
    usecase = mock(GetAccount.class);
    var endpoint = new GetAccountJavalinEndpoint(usecase);
    server = JavalinServer.of(PORT);
    server.addEndpoint(endpoint);
    server.start();
  }

  @AfterAll
  static void destroy() {
    server.stop();
  }

  @Test
  void givenValidAccountId_WhenRequested_ThenShouldReturnAccount() {
    // Given
    var accountId = "account-123";
    var getResult = AccountResult.builder()
        .id(accountId)
        .name("My Savings")
        .type("BANK")
        .themeColor("#FF5733")
        .balance(BigDecimal.valueOf(1000))
        .user("user@email.com")
        .build();
    var result = Result.success(getResult);
    when(usecase.process(any(Context.class), any(String.class))).thenReturn(result);

    // When
    var response = Unirest.get(baseUrl + "/accounts/" + accountId).asString();

    // Then
    assertEquals(200, response.getStatus());

    var responseBody = response.getBody();
    assertNotNull(responseBody);
    var resultBody = new JSONObject(responseBody);
    assertEquals(accountId, resultBody.getString("id"));
    assertEquals("My Savings", resultBody.getString("name"));
    assertEquals("BANK", resultBody.getString("type"));
    assertEquals("#FF5733", resultBody.getString("themeColor"));
    assertEquals(BigDecimal.valueOf(1000), resultBody.getBigDecimal("balance"));
    assertEquals("user@email.com", resultBody.getString("user"));
  }

  @Test
  void givenNonExistentAccountId_WhenRequested_ThenShouldReturn404() {
    // Given
    var accountId = "nonexistent-account";
    var result = Result.<AccountResult>failure(Code.NOT_FOUND, "Account not found");
    when(usecase.process(any(Context.class), any(String.class))).thenReturn(result);

    // When
    var response = Unirest.get(baseUrl + "/accounts/" + accountId).asString();

    // Then
    assertEquals(404, response.getStatus());
    assertEquals("Account not found", response.getBody());
  }

  @Test
  void givenServerError_WhenRequested_ThenShouldReturn500() {
    // Given
    var accountId = "account-123";
    var result = Result.<AccountResult>failure(Code.SERVER_ERROR, "Database connection failed");
    when(usecase.process(any(Context.class), any(String.class))).thenReturn(result);

    // When
    var response = Unirest.get(baseUrl + "/accounts/" + accountId).asString();

    // Then
    assertEquals(500, response.getStatus());
    assertEquals("Database connection failed", response.getBody());
  }
}
