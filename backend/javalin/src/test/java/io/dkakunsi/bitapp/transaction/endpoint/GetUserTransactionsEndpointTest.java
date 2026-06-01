package io.dkakunsi.bitapp.transaction.endpoint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.json.JSONArray;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.dkakunsi.bitapp.common.AppError.Code;
import io.dkakunsi.bitapp.common.DateTimeConverter;
import io.dkakunsi.bitapp.domain.usecase.Result;
import io.dkakunsi.bitapp.javalin.JavalinServer;
import io.dkakunsi.bitapp.transaction.dto.TransactionResult;
import io.dkakunsi.bitapp.transaction.usecase.GetUserTransactions;
import kong.unirest.Unirest;

class GetUserTransactionsEndpointTest {

  private static final int PORT = 20014;

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
    var date1 = LocalDate.of(2026, 1, 23).toEpochDay() * 24 * 60 * 60; // Convert to seconds
    var time1 = LocalTime.of(10, 30).toSecondOfDay(); //
    var transactionItem1 = TransactionResult.builder()
        .id("trans1")
        .user(USER_ID)
        .type("DEBIT")
        .title("Grocery Shopping")
        .description("Weekly groceries")
        .date(date1)
        .time(time1)
        .source("account-1")
        .amount(BigDecimal.valueOf(150000))
        .currency("IDR")
        .category("FOOD")
        .build();

    var date2 = LocalDate.of(2026, 1, 23).toEpochDay() * 24 * 60 * 60; // Convert to seconds
    var time2 = LocalTime.of(8, 0).toSecondOfDay(); //
    var transactionItem2 = TransactionResult.builder()
        .id("trans2")
        .user(USER_ID)
        .type("CREDIT")
        .title("Salary")
        .description("Monthly salary")
        .date(date2)
        .time(time2)
        .destination("account-2")
        .amount(BigDecimal.valueOf(5000000))
        .currency("IDR")
        .category("SALARY")
        .build();

    var getResult = List.of(transactionItem1, transactionItem2);
    var result = Result.success(getResult);
    when(usecase.process(any(String.class))).thenReturn(result);

    // When
    var response = Unirest.get(baseUrl + "/v1/users/{userId}/transactions")
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
    when(usecase.process(any(String.class))).thenReturn(result);

    // When
    var response = Unirest.get(baseUrl + "/v1/users/{userId}/transactions")
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
    var date = DateTimeConverter.epochMilli(LocalDate.of(2026, 1, 23));
    var time = DateTimeConverter.minutesSinceMidnight(LocalTime.of(14, 30));
    var transactionItem = TransactionResult.builder()
        .id("trans1")
        .user(USER_ID)
        .type("TRANSFER")
        .title("Transfer to Savings")
        .description("Monthly savings")
        .date(date)
        .time(time)
        .source("account-1")
        .destination("account-2")
        .amount(BigDecimal.valueOf(100000))
        .currency("IDR")
        .category("OTHER")
        .build();
    var result = Result.success(List.of(transactionItem));
    when(usecase.process(any(String.class))).thenReturn(result);

    // When
    var response = Unirest.get(baseUrl + "/v1/users/{userId}/transactions")
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
    when(usecase.process(any(String.class))).thenReturn(result);

    // When
    var response = Unirest.get(baseUrl + "/v1/users/{userId}/transactions")
        .routeParam("userId", USER_ID)
        .asString();

    // Then
    assertEquals(200, response.getStatus());
  }

  @Test
  void givenValidRequest_WhenUseCaseFails_ThenShouldReturn500() {
    // Given
    var result = Result.<List<TransactionResult>>failure(Code.SERVER_ERROR, "Database error");
    when(usecase.process(any(String.class))).thenReturn(result);

    // When
    var response = Unirest.get(baseUrl + "/v1/users/{userId}/transactions")
        .routeParam("userId", USER_ID)
        .asString();

    // Then
    assertEquals(500, response.getStatus());
    assertEquals("Database error", response.getBody());
  }
}
