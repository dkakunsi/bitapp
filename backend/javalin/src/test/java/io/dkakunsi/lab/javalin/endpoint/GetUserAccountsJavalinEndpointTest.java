package io.dkakunsi.lab.javalin.endpoint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.dkakunsi.bitapp.account.dto.GetUserAccountsInput;
import io.dkakunsi.bitapp.account.dto.GetUserAccountsResult;
import io.dkakunsi.bitapp.account.model.Account;
import io.dkakunsi.bitapp.common.AppError;
import io.dkakunsi.bitapp.common.AppError.Code;
import io.dkakunsi.bitapp.common.Context;
import io.dkakunsi.bitapp.common.usecase.Result;
import io.dkakunsi.bitapp.common.usecase.UseCase;
import io.dkakunsi.lab.javalin.JavalinServer;
import kong.unirest.Unirest;

class GetUserAccountsJavalinEndpointTest {

  private static final String BASE_URL = "http://localhost:20004";
  private static final String USER_ID = "user123";

  private static UseCase<GetUserAccountsInput, GetUserAccountsResult> usecase;

  private static JavalinServer server;

  @SuppressWarnings("unchecked")
  @BeforeAll
  static void setup() throws Exception {
    usecase = (UseCase<GetUserAccountsInput, GetUserAccountsResult>) mock(UseCase.class);
    var endpoint = new GetUserAccountsJavalinEndpoint(usecase, null);
    server = JavalinServer.of(20004);
    server.addEndpoint(endpoint);
    server.start();
  }

  @AfterAll
  static void destroy() {
    server.stop();
  }

  @SuppressWarnings("unchecked")
  @AfterEach
  void resetMocks() {
    reset(usecase);
  }

  @SuppressWarnings("unchecked")
  @Test
  void givenValidUserIdWithMultipleAccounts_WhenRequested_ThenShouldReturn200AndAccountsList() {
    // Given
    var accountItem1 = GetUserAccountsResult.AccountItem.builder()
        .id("account1")
        .name("Savings Account")
        .type(Account.Type.BANK)
        .themeColor("#FF5733")
        .balance(BigDecimal.valueOf(1000.00))
        .userId(USER_ID)
        .build();

    var accountItem2 = GetUserAccountsResult.AccountItem.builder()
        .id("account2")
        .name("Checking Account")
        .type(Account.Type.CASH)
        .themeColor("#3357FF")
        .balance(BigDecimal.valueOf(500.00))
        .userId(USER_ID)
        .build();

    var result = mock(Result.class);
    when(result.isSuccess()).thenReturn(true);
    when(result.isEmpty()).thenReturn(false);
    when(result.isFailed()).thenReturn(false);
    when(result.data()).thenReturn(Optional.of(
        GetUserAccountsResult.builder()
            .accounts(Arrays.asList(accountItem1, accountItem2))
            .build()));
    when(usecase.process(any(Context.class), any(GetUserAccountsInput.class))).thenReturn(result);

    // When
    var response = Unirest.get(BASE_URL + "/users/{userId}/accounts")
        .routeParam("userId", USER_ID)
        .asString();

    // Then
    assertEquals(200, response.getStatus());

    var responseBody = response.getBody();
    assertNotNull(responseBody);
    assertTrue(responseBody.contains("\"accounts\":["));
    assertTrue(responseBody.contains("\"id\":\"account1\""));
    assertTrue(responseBody.contains("\"name\":\"Savings Account\""));
    assertTrue(responseBody.contains("\"type\":\"BANK\""));
    assertTrue(responseBody.contains("\"themeColor\":\"#FF5733\""));
    assertTrue(responseBody.contains("\"balance\":1000"));
    assertTrue(responseBody.contains("\"id\":\"account2\""));
    assertTrue(responseBody.contains("\"name\":\"Checking Account\""));
    assertTrue(responseBody.contains("\"type\":\"CASH\""));
    assertTrue(responseBody.contains("\"balance\":500"));
  }

  @SuppressWarnings("unchecked")
  @Test
  void givenValidUserIdWithNoAccounts_WhenRequested_ThenShouldReturn200AndEmptyList() {
    // Given
    var result = mock(Result.class);
    when(result.isSuccess()).thenReturn(true);
    when(result.isEmpty()).thenReturn(false);
    when(result.isFailed()).thenReturn(false);
    when(result.data()).thenReturn(Optional.of(
        GetUserAccountsResult.builder()
            .accounts(Collections.emptyList())
            .build()));
    when(usecase.process(any(Context.class), any(GetUserAccountsInput.class))).thenReturn(result);

    // When
    var response = Unirest.get(BASE_URL + "/users/{userId}/accounts")
        .routeParam("userId", USER_ID)
        .asString();

    // Then
    assertEquals(200, response.getStatus());

    var responseBody = response.getBody();
    assertNotNull(responseBody);
    assertTrue(responseBody.contains("\"accounts\":[]"));
  }

  @SuppressWarnings("unchecked")
  @Test
  void givenValidUserIdWithSingleAccount_WhenRequested_ThenShouldReturn200AndSingleAccountList() {
    // Given
    var accountItem = GetUserAccountsResult.AccountItem.builder()
        .id("account1")
        .name("E-Wallet")
        .type(Account.Type.EWALLET)
        .themeColor("#00FF00")
        .balance(BigDecimal.valueOf(250.50))
        .userId(USER_ID)
        .build();

    var result = mock(Result.class);
    when(result.isSuccess()).thenReturn(true);
    when(result.isEmpty()).thenReturn(false);
    when(result.isFailed()).thenReturn(false);
    when(result.data()).thenReturn(Optional.of(
        GetUserAccountsResult.builder()
            .accounts(List.of(accountItem))
            .build()));
    when(usecase.process(any(Context.class), any(GetUserAccountsInput.class))).thenReturn(result);

    // When
    var response = Unirest.get(BASE_URL + "/users/{userId}/accounts")
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

  @SuppressWarnings("unchecked")
  @Test
  void givenValidRequest_WhenUseCaseReturnsEmpty_ThenShouldReturn200() {
    // Given
    var result = mock(Result.class);
    when(result.isSuccess()).thenReturn(true);
    when(result.isEmpty()).thenReturn(true);
    when(result.isFailed()).thenReturn(false);
    when(usecase.process(any(Context.class), any(GetUserAccountsInput.class))).thenReturn(result);

    // When
    var response = Unirest.get(BASE_URL + "/users/{userId}/accounts")
        .routeParam("userId", USER_ID)
        .asString();

    // Then
    assertEquals(200, response.getStatus());
  }

  @SuppressWarnings("unchecked")
  @Test
  void givenValidRequest_WhenUseCaseFails_ThenShouldReturn500() {
    // Given
    var result = mock(Result.class);
    when(result.isSuccess()).thenReturn(false);
    when(result.isEmpty()).thenReturn(false);
    when(result.isFailed()).thenReturn(true);
    when(result.error()).thenReturn(Optional.of(new AppError(Code.SERVER_ERROR, "Database error")));
    when(usecase.process(any(Context.class), any(GetUserAccountsInput.class))).thenReturn(result);

    // When
    var response = Unirest.get(BASE_URL + "/users/{userId}/accounts")
        .routeParam("userId", USER_ID)
        .asString();

    // Then
    assertEquals(500, response.getStatus());
    assertEquals("Database error", response.getBody());
  }
}
