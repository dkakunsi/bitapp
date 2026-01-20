package io.dkakunsi.bitapp.javalin.endpoint.account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.json.JSONArray;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.dkakunsi.bitapp.account.dto.AccountResult;
import io.dkakunsi.bitapp.account.dto.GetUserAccountsInput;
import io.dkakunsi.bitapp.account.entity.Account;
import io.dkakunsi.bitapp.account.usecase.GetUserAccounts;
import io.dkakunsi.bitapp.common.AppError.Code;
import io.dkakunsi.bitapp.common.Context;
import io.dkakunsi.bitapp.common.usecase.Result;
import io.dkakunsi.bitapp.javalin.JavalinServer;
import kong.unirest.Unirest;

class GetUserAccountsJavalinEndpointTest {

  private static final int PORT = 20002;

  private static String baseUrl;

  private static final String USER_ID = "user123";

  private static GetUserAccounts usecase;

  private static JavalinServer server;

  @BeforeAll
  static void setup() throws Exception {
    baseUrl = "http://localhost:" + PORT;
    usecase = mock(GetUserAccounts.class);
    var endpoint = new GetUserAccountsJavalinEndpoint(usecase);
    server = JavalinServer.of(PORT);
    server.addEndpoint(endpoint);
    server.start();
  }

  @AfterAll
  static void destroy() {
    server.stop();
  }

  @AfterEach
  void resetMocks() {
    reset(usecase);
  }

  @Test
  void givenValidUserIdWithMultipleAccounts_WhenRequested_ThenShouldReturn200AndAccountsList() {
    // Given
    var accountItem1 = AccountResult.builder()
        .id("account1")
        .name("Savings Account")
        .type(Account.Type.BANK.name())
        .themeColor("#FF5733")
        .balance(BigDecimal.valueOf(1000.00))
        .user(USER_ID)
        .build();
    var accountItem2 = AccountResult.builder()
        .id("account2")
        .name("Checking Account")
        .type(Account.Type.CASH.name())
        .themeColor("#3357FF")
        .balance(BigDecimal.valueOf(500.00))
        .user(USER_ID)
        .build();
    var getResult = List.of(accountItem1, accountItem2);
    var result = Result.success(getResult);
    when(usecase.process(any(Context.class), any(GetUserAccountsInput.class))).thenReturn(result);

    // When
    var response = Unirest.get(baseUrl + "/users/{userId}/accounts")
        .routeParam("userId", USER_ID)
        .asString();

    // Then
    assertEquals(200, response.getStatus());

    var responseBody = response.getBody();
    assertNotNull(responseBody);
    var resultBody = new JSONArray(responseBody);
    assertEquals(2, resultBody.length());
    var account1 = resultBody.getJSONObject(0);
    assertEquals("account1", account1.getString("id"));
    assertEquals("Savings Account", account1.getString("name"));
    assertEquals("BANK", account1.getString("type"));
    assertEquals("#FF5733", account1.getString("themeColor"));
    assertEquals(1000, account1.getDouble("balance"));
    var account2 = resultBody.getJSONObject(1);
    assertEquals("account2", account2.getString("id"));
    assertEquals("Checking Account", account2.getString("name"));
    assertEquals("CASH", account2.getString("type"));
    assertEquals("#3357FF", account2.getString("themeColor"));
    assertEquals(500, account2.getDouble("balance"));
  }

  @Test
  void givenValidUserIdWithNoAccounts_WhenRequested_ThenShouldReturn200AndEmptyList() {
    // Given
    var result = Result.<List<AccountResult>>success(List.of());
    when(usecase.process(any(Context.class), any(GetUserAccountsInput.class))).thenReturn(result);

    // When
    var response = Unirest.get(baseUrl + "/users/{userId}/accounts")
        .routeParam("userId", USER_ID)
        .asString();

    // Then
    assertEquals(200, response.getStatus());

    var responseBody = response.getBody();
    assertNotNull(responseBody);
    var resultBody = new JSONArray(responseBody);
    assertEquals(0, resultBody.length());
  }

  @Test
  void givenValidUserIdWithSingleAccount_WhenRequested_ThenShouldReturn200AndSingleAccountList() {
    // Given
    var accountItem = AccountResult.builder()
        .id("account1")
        .name("E-Wallet")
        .type(Account.Type.EWALLET.name())
        .themeColor("#00FF00")
        .balance(BigDecimal.valueOf(250.50))
        .user(USER_ID)
        .build();
    var result = Result.success(List.of(accountItem));
    when(usecase.process(any(Context.class), any(GetUserAccountsInput.class))).thenReturn(result);

    // When
    var response = Unirest.get(baseUrl + "/users/{userId}/accounts")
        .routeParam("userId", USER_ID)
        .asString();

    // Then
    assertEquals(200, response.getStatus());

    var responseBody = response.getBody();
    assertNotNull(responseBody);
    assertTrue(responseBody.contains("\"id\":\"account1\""));
    assertTrue(responseBody.contains("\"name\":\"E-Wallet\""));
    assertTrue(responseBody.contains("\"type\":\"EWALLET\""));
    assertTrue(responseBody.contains("\"balance\":250.5"));
  }

  @Test
  void givenValidRequest_WhenUseCaseReturnsEmpty_ThenShouldReturn200() {
    // Given
    var result = Result.<List<AccountResult>>success(List.of());
    when(usecase.process(any(Context.class), any(GetUserAccountsInput.class))).thenReturn(result);

    // When
    var response = Unirest.get(baseUrl + "/users/{userId}/accounts")
        .routeParam("userId", USER_ID)
        .asString();

    // Then
    assertEquals(200, response.getStatus());
  }

  @Test
  void givenValidRequest_WhenUseCaseFails_ThenShouldReturn500() {
    // Given
    var result = Result.<List<AccountResult>>failure(Code.SERVER_ERROR, "Database error");
    when(usecase.process(any(Context.class), any(GetUserAccountsInput.class))).thenReturn(result);

    // When
    var response = Unirest.get(baseUrl + "/users/{userId}/accounts")
        .routeParam("userId", USER_ID)
        .asString();

    // Then
    assertEquals(500, response.getStatus());
    assertEquals("Database error", response.getBody());
  }
}
