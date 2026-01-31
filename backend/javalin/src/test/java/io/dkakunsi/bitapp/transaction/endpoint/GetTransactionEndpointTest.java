package io.dkakunsi.bitapp.transaction.endpoint;

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

import io.dkakunsi.bitapp.common.AppError.Code;
import io.dkakunsi.bitapp.common.Context;
import io.dkakunsi.bitapp.domain.usecase.Result;
import io.dkakunsi.bitapp.javalin.JavalinServer;
import io.dkakunsi.bitapp.transaction.dto.TransactionResult;
import io.dkakunsi.bitapp.transaction.usecase.GetTransaction;
import kong.unirest.Unirest;

class GetTransactionEndpointTest {

  private static final int PORT = 20013;

  private static String baseUrl;

  private static final String USER_ID = "user@email.com";

  private static GetTransaction usecase;

  private static JavalinServer server;

  @BeforeAll
  static void setup() throws Exception {
    baseUrl = "http://localhost:" + PORT;
    usecase = mock(GetTransaction.class);
    var endpoint = new GetTransactionEndpoint(usecase);
    server = JavalinServer.of(PORT);
    server.addEndpoint(endpoint);
    server.start();
  }

  @AfterAll
  static void destroy() {
    server.stop();
  }

  @Test
  void givenValidTransactionId_WhenRequested_ThenShouldReturnTransaction() {
    // Given
    var transactionId = "trans-123";
    var getResult = TransactionResult.builder()
        .id(transactionId)
        .user(USER_ID)
        .type("DEBIT")
        .title("Grocery Shopping")
        .description("Weekly groceries")
        .date("2026-01-23")
        .time("14:30")
        .source("account-1")
        .destination(null)
        .loan(null)
        .amount(BigDecimal.valueOf(150000))
        .currency("IDR")
        .category("FOOD")
        .build();
    var result = Result.success(getResult);
    when(usecase.process(any(Context.class), any(String.class))).thenReturn(result);

    // When
    var response = Unirest.get(baseUrl + "/transactions/" + transactionId).asString();

    // Then
    assertEquals(200, response.getStatus());

    var responseBody = response.getBody();
    assertNotNull(responseBody);
    var resultBody = new JSONObject(responseBody);
    assertEquals(transactionId, resultBody.getString("id"));
    assertEquals(USER_ID, resultBody.getString("user"));
    assertEquals("DEBIT", resultBody.getString("type"));
    assertEquals("Grocery Shopping", resultBody.getString("title"));
    assertEquals("Weekly groceries", resultBody.getString("description"));
    assertEquals("2026-01-23", resultBody.getString("date"));
    assertEquals("14:30", resultBody.getString("time"));
    assertEquals("account-1", resultBody.getString("source"));
    assertEquals(150000, resultBody.getBigDecimal("amount").intValue());
    assertEquals("IDR", resultBody.getString("currency"));
    assertEquals("FOOD", resultBody.getString("category"));
  }

  @Test
  void givenValidCreditTransactionId_WhenRequested_ThenShouldReturnCreditTransaction() {
    // Given
    var transactionId = "trans-456";
    var getResult = TransactionResult.builder()
        .id(transactionId)
        .user(USER_ID)
        .type("CREDIT")
        .title("Salary Payment")
        .description("Monthly salary")
        .date("2026-01-23")
        .time("09:00")
        .source(null)
        .destination("account-2")
        .loan(null)
        .amount(BigDecimal.valueOf(5000000))
        .currency("IDR")
        .category("INCOME")
        .build();
    var result = Result.success(getResult);
    when(usecase.process(any(Context.class), any(String.class))).thenReturn(result);

    // When
    var response = Unirest.get(baseUrl + "/transactions/" + transactionId).asString();

    // Then
    assertEquals(200, response.getStatus());

    var responseBody = response.getBody();
    assertNotNull(responseBody);
    var resultBody = new JSONObject(responseBody);
    assertEquals(transactionId, resultBody.getString("id"));
    assertEquals("CREDIT", resultBody.getString("type"));
    assertEquals("Salary Payment", resultBody.getString("title"));
    assertEquals("account-2", resultBody.getString("destination"));
    assertEquals(5000000, resultBody.getLong("amount"));
    assertEquals("INCOME", resultBody.getString("category"));
  }

  @Test
  void givenValidTransferTransactionId_WhenRequested_ThenShouldReturnTransferTransaction() {
    // Given
    var transactionId = "trans-789";
    var getResult = TransactionResult.builder()
        .id(transactionId)
        .user(USER_ID)
        .type("TRANSFER")
        .title("Internal Transfer")
        .description("Moving funds")
        .date("2026-01-23")
        .time("15:45")
        .source("account-1")
        .destination("account-2")
        .loan(null)
        .amount(BigDecimal.valueOf(200000))
        .currency("IDR")
        .category(null)
        .build();
    var result = Result.success(getResult);
    when(usecase.process(any(Context.class), any(String.class))).thenReturn(result);

    // When
    var response = Unirest.get(baseUrl + "/transactions/" + transactionId).asString();

    // Then
    assertEquals(200, response.getStatus());

    var responseBody = response.getBody();
    assertNotNull(responseBody);
    var resultBody = new JSONObject(responseBody);
    assertEquals(transactionId, resultBody.getString("id"));
    assertEquals("TRANSFER", resultBody.getString("type"));
    assertEquals("account-1", resultBody.getString("source"));
    assertEquals("account-2", resultBody.getString("destination"));
    assertEquals(200000, resultBody.getLong("amount"));
  }

  @Test
  void givenTransactionWithLoan_WhenRequested_ThenShouldIncludeLoanReference() {
    // Given
    var transactionId = "trans-loan";
    var loanId = "loan-123";
    var getResult = TransactionResult.builder()
        .id(transactionId)
        .user(USER_ID)
        .type("DEBIT")
        .title("Loan Payment")
        .description("Monthly loan payment")
        .date("2026-01-23")
        .time("11:00")
        .source("account-1")
        .destination(null)
        .loan(loanId)
        .amount(BigDecimal.valueOf(100000))
        .currency("IDR")
        .category("LOAN")
        .build();
    var result = Result.success(getResult);
    when(usecase.process(any(Context.class), any(String.class))).thenReturn(result);

    // When
    var response = Unirest.get(baseUrl + "/transactions/" + transactionId).asString();

    // Then
    assertEquals(200, response.getStatus());

    var responseBody = response.getBody();
    assertNotNull(responseBody);
    var resultBody = new JSONObject(responseBody);
    assertEquals(transactionId, resultBody.getString("id"));
    assertEquals(loanId, resultBody.getString("loan"));
    assertEquals("LOAN", resultBody.getString("category"));
  }

  @Test
  void givenNonExistentTransactionId_WhenRequested_ThenShouldReturn404() {
    // Given
    var transactionId = "nonexistent-transaction";
    var result = Result.<TransactionResult>failure(Code.NOT_FOUND, "Transaction not found");
    when(usecase.process(any(Context.class), any(String.class))).thenReturn(result);

    // When
    var response = Unirest.get(baseUrl + "/transactions/" + transactionId).asString();

    // Then
    assertEquals(404, response.getStatus());
    assertEquals("Transaction not found", response.getBody());
  }

  @Test
  void givenServerError_WhenRequested_ThenShouldReturn500() {
    // Given
    var transactionId = "trans-123";
    var result = Result.<TransactionResult>failure(Code.SERVER_ERROR, "Database connection failed");
    when(usecase.process(any(Context.class), any(String.class))).thenReturn(result);

    // When
    var response = Unirest.get(baseUrl + "/transactions/" + transactionId).asString();

    // Then
    assertEquals(500, response.getStatus());
    assertEquals("Database connection failed", response.getBody());
  }
}
