package io.dkakunsi.bitapp.transaction.endpoint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.util.List;

import org.json.JSONArray;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.dkakunsi.bitapp.common.AppError.Code;
import io.dkakunsi.bitapp.common.Context;
import io.dkakunsi.bitapp.common.usecase.Result;
import io.dkakunsi.bitapp.javalin.JavalinServer;
import io.dkakunsi.bitapp.transaction.dto.TransactionResult;
import io.dkakunsi.bitapp.transaction.usecase.GetUserTransactions;
import kong.unirest.Unirest;

class GetUserTransactionsEndpointTest {

  private static final int PORT = 20012;

  private static String baseUrl;

  private static final String USER_ID = "user123";

  private static GetUserTransactions usecase;

  private static JavalinServer server;

  @BeforeAll
  static void setup() throws Exception {
    baseUrl = "http://localhost:" + PORT;
    usecase = mock(GetUserTransactions.class);

    var endpoint = new GetUserTransactionsEndpoint(usecase);
    server = JavalinServer.of(PORT)
        .addEndpoint(endpoint)
        .start();
  }

  @AfterAll
  static void destroy() {
    if (server != null) {
      server.stop();
    }
  }

  @AfterEach
  void resetMocks() {
    reset(usecase);
  }

  @Test
  void givenValidUserIdWithMultipleTransactions_WhenRequested_ThenShouldReturn200AndTransactionsList() {
    // Given
    var transactionItem1 = TransactionResult.builder()
        .id("trans1")
        .user(USER_ID)
        .type("DEBIT")
        .title("Grocery Shopping")
        .description("Weekly groceries")
        .date("2026-01-23")
        .time("10:30")
        .source("account-1")
        .amount(50000L)
        .currency("IDR")
        .category("FOOD")
        .build();

    var transactionItem2 = TransactionResult.builder()
        .id("trans2")
        .user(USER_ID)
        .type("CREDIT")
        .title("Salary")
        .description("Monthly salary")
        .date("2026-01-23")
        .time("08:00")
        .destination("account-2")
        .amount(5000000L)
        .currency("IDR")
        .category("INCOME")
        .build();

    var getResult = List.of(transactionItem1, transactionItem2);
    var result = Result.success(getResult);
    when(usecase.process(any(Context.class), any(String.class))).thenReturn(result);

    // When
    var response = Unirest.get(baseUrl + "/users/{userId}/transactions")
        .routeParam("userId", USER_ID)
        .asString();

    // Then
    assertEquals(200, response.getStatus());

    var responseBody = response.getBody();
    assertNotNull(responseBody);
    var resultBody = new JSONArray(responseBody);
    assertEquals(2, resultBody.length());

    var firstTransaction = resultBody.getJSONObject(0);
    assertEquals("trans1", firstTransaction.getString("id"));
    assertEquals("Grocery Shopping", firstTransaction.getString("title"));
    assertEquals("DEBIT", firstTransaction.getString("type"));

    var secondTransaction = resultBody.getJSONObject(1);
    assertEquals("trans2", secondTransaction.getString("id"));
    assertEquals("Salary", secondTransaction.getString("title"));
    assertEquals("CREDIT", secondTransaction.getString("type"));
  }

  @Test
  void givenValidUserIdWithNoTransactions_WhenRequested_ThenShouldReturn200AndEmptyList() {
    // Given
    var result = Result.<List<TransactionResult>>success(List.of());
    when(usecase.process(any(Context.class), any(String.class))).thenReturn(result);

    // When
    var response = Unirest.get(baseUrl + "/users/{userId}/transactions")
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
  void givenValidUserIdWithSingleTransaction_WhenRequested_ThenShouldReturn200AndSingleTransactionList() {
    // Given
    var transactionItem = TransactionResult.builder()
        .id("trans1")
        .user(USER_ID)
        .type("TRANSFER")
        .title("Transfer to Savings")
        .description("Monthly savings")
        .date("2026-01-23")
        .time("14:30")
        .source("account-1")
        .destination("account-2")
        .amount(100000L)
        .currency("IDR")
        .category("OTHER")
        .build();
    var result = Result.success(List.of(transactionItem));
    when(usecase.process(any(Context.class), any(String.class))).thenReturn(result);

    // When
    var response = Unirest.get(baseUrl + "/users/{userId}/transactions")
        .routeParam("userId", USER_ID)
        .asString();

    // Then
    assertEquals(200, response.getStatus());

    var responseBody = response.getBody();
    assertNotNull(responseBody);
    var resultBody = new JSONArray(responseBody);
    assertEquals(1, resultBody.length());

    var transaction = resultBody.getJSONObject(0);
    assertEquals("trans1", transaction.getString("id"));
    assertEquals("Transfer to Savings", transaction.getString("title"));
    assertEquals("TRANSFER", transaction.getString("type"));
    assertEquals("account-1", transaction.getString("source"));
    assertEquals("account-2", transaction.getString("destination"));
  }

  @Test
  void givenValidRequest_WhenUseCaseReturnsEmpty_ThenShouldReturn200() {
    // Given
    var result = Result.<List<TransactionResult>>success(List.of());
    when(usecase.process(any(Context.class), any(String.class))).thenReturn(result);

    // When
    var response = Unirest.get(baseUrl + "/users/{userId}/transactions")
        .routeParam("userId", USER_ID)
        .asString();

    // Then
    assertEquals(200, response.getStatus());
  }

  @Test
  void givenValidRequest_WhenUseCaseFails_ThenShouldReturn500() {
    // Given
    var result = Result.<List<TransactionResult>>failure(Code.SERVER_ERROR, "Database error");
    when(usecase.process(any(Context.class), any(String.class))).thenReturn(result);

    // When
    var response = Unirest.get(baseUrl + "/users/{userId}/transactions")
        .routeParam("userId", USER_ID)
        .asString();

    // Then
    assertEquals(500, response.getStatus());
    assertEquals("Database error", response.getBody());
  }
}
