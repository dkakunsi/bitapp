package io.dkakunsi.bitapp.transaction.endpoint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.dkakunsi.bitapp.common.AppError.Code;
import io.dkakunsi.bitapp.common.Context;
import io.dkakunsi.bitapp.domain.usecase.Result;
import io.dkakunsi.bitapp.javalin.JavalinServer;
import io.dkakunsi.bitapp.transaction.dto.CreateTransactionInput;
import io.dkakunsi.bitapp.transaction.dto.TransactionResult;
import io.dkakunsi.bitapp.transaction.usecase.CreateTransaction;
import kong.unirest.Unirest;

class CreateTransactionEndpointTest {

  private static final int PORT = 20012;

  private static String baseUrl;

  private static CreateTransaction usecase;

  private static JavalinServer server;

  @BeforeAll
  static void setup() throws Exception {
    baseUrl = "http://localhost:" + PORT;
    usecase = mock(CreateTransaction.class);
    var endpoint = new CreateTransactionEndpoint(usecase);
    server = JavalinServer.of(PORT);
    server.addEndpoint(endpoint);
    server.start();
  }

  @AfterAll
  static void destroy() {
    server.stop();
  }

  @Test
  void givenValidDebitTransactionRequest_WhenRequested_ThenShouldReturn200AndTransaction() {
    // Given
    var transactionResult = TransactionResult.builder()
        .id("transaction-123")
        .user("test@email.com")
        .title("Grocery Shopping")
        .description("Monthly groceries")
        .date("2026-01-22")
        .time("10:30")
        .source("account-1")
        .amount(50000L)
        .currency("IDR")
        .category("FOOD")
        .type("DEBIT")
        .build();
    var result = Result.success(transactionResult);
    when(usecase.process(any(Context.class), any(CreateTransactionInput.class))).thenReturn(result);

    var requestBody = """
        {
          "title":"Grocery Shopping",
          "description":"Monthly groceries",
          "date":"2026-01-22",
          "time":"10:30",
          "source":"account-1",
          "amount":50000,
          "currency":"IDR",
          "category":"FOOD",
          "type":"DEBIT"
        }
        """;

    // When
    var response = Unirest.post(baseUrl + "/transactions").body(requestBody).asString();

    // Then
    assertEquals(200, response.getStatus());

    var responseBody = response.getBody();
    assertNotNull(responseBody);
    assertTrue(responseBody.contains("\"id\":\"transaction-123\""));
    assertTrue(responseBody.contains("\"title\":\"Grocery Shopping\""));
    assertTrue(responseBody.contains("\"type\":\"DEBIT\""));
    assertTrue(responseBody.contains("\"amount\":50000"));
  }

  @Test
  void givenValidCreditTransactionRequest_WhenRequested_ThenShouldReturn200AndTransaction() {
    // Given
    var transactionResult = TransactionResult.builder()
        .id("transaction-456")
        .user("test@email.com")
        .title("Salary")
        .description("Monthly salary")
        .date("2026-01-22")
        .time("08:00")
        .destination("account-1")
        .amount(5000000L)
        .currency("IDR")
        .category("INCOME")
        .type("CREDIT")
        .build();
    var result = Result.success(transactionResult);
    when(usecase.process(any(Context.class), any(CreateTransactionInput.class))).thenReturn(result);

    var requestBody = """
        {
          "title":"Salary",
          "description":"Monthly salary",
          "date":"2026-01-22",
          "time":"08:00",
          "destination":"account-1",
          "amount":5000000,
          "currency":"IDR",
          "category":"INCOME",
          "type":"CREDIT"
        }
        """;

    // When
    var response = Unirest.post(baseUrl + "/transactions").body(requestBody).asString();

    // Then
    assertEquals(200, response.getStatus());

    var responseBody = response.getBody();
    assertNotNull(responseBody);
    assertTrue(responseBody.contains("\"id\":\"transaction-456\""));
    assertTrue(responseBody.contains("\"title\":\"Salary\""));
    assertTrue(responseBody.contains("\"type\":\"CREDIT\""));
    assertTrue(responseBody.contains("\"amount\":5000000"));
  }

  @Test
  void givenValidTransferTransactionRequest_WhenRequested_ThenShouldReturn200AndTransaction() {
    // Given
    var transactionResult = TransactionResult.builder()
        .id("transaction-789")
        .user("test@email.com")
        .title("Transfer to Savings")
        .description("Monthly savings")
        .date("2026-01-22")
        .time("12:00")
        .source("account-1")
        .destination("account-2")
        .amount(100000L)
        .currency("IDR")
        .category("OTHER")
        .type("TRANSFER")
        .build();
    var result = Result.success(transactionResult);
    when(usecase.process(any(Context.class), any(CreateTransactionInput.class))).thenReturn(result);

    var requestBody = """
        {
          "title":"Transfer to Savings",
          "description":"Monthly savings",
          "date":"2026-01-22",
          "time":"12:00",
          "source":"account-1",
          "destination":"account-2",
          "amount":100000,
          "currency":"IDR",
          "category":"OTHER",
          "type":"TRANSFER"
        }
        """;

    // When
    var response = Unirest.post(baseUrl + "/transactions").body(requestBody).asString();

    // Then
    assertEquals(200, response.getStatus());

    var responseBody = response.getBody();
    assertNotNull(responseBody);
    assertTrue(responseBody.contains("\"id\":\"transaction-789\""));
    assertTrue(responseBody.contains("\"type\":\"TRANSFER\""));
    assertTrue(responseBody.contains("\"source\":\"account-1\""));
    assertTrue(responseBody.contains("\"destination\":\"account-2\""));
  }

  @Test
  void givenValidTransactionWithLoan_WhenRequested_ThenShouldReturn200() {
    // Given
    var transactionResult = TransactionResult.builder()
        .id("transaction-loan")
        .user("test@email.com")
        .title("Loan Payment")
        .source("account-1")
        .loan("loan-1")
        .amount(100000L)
        .currency("IDR")
        .category("LOAN")
        .type("DEBIT")
        .build();
    var result = Result.success(transactionResult);
    when(usecase.process(any(Context.class), any(CreateTransactionInput.class))).thenReturn(result);

    var requestBody = """
        {
          "title":"Loan Payment",
          "source":"account-1",
          "loan":"loan-1",
          "amount":100000,
          "currency":"IDR",
          "category":"LOAN",
          "type":"DEBIT"
        }
        """;

    // When
    var response = Unirest.post(baseUrl + "/transactions").body(requestBody).asString();

    // Then
    assertEquals(200, response.getStatus());

    var responseBody = response.getBody();
    assertNotNull(responseBody);
    assertTrue(responseBody.contains("\"loan\":\"loan-1\""));
  }

  @Test
  void givenValidRequestWithEmptyOutput_WhenRequested_ThenShouldReturn200() {
    // Given
    var result = Result.<TransactionResult>success();
    when(usecase.process(any(Context.class), any(CreateTransactionInput.class))).thenReturn(result);

    var requestBody = """
        {
          "title":"Test Transaction",
          "source":"account-1",
          "amount":10000,
          "category":"FOOD",
          "type":"DEBIT"
        }
        """;

    // When
    var response = Unirest.post(baseUrl + "/transactions").body(requestBody).asString();

    // Then
    assertEquals(200, response.getStatus());
    assertEquals("", response.getBody());
  }

  @Test
  void givenServerError_WhenRequested_ThenShouldReturn500() {
    // Given
    var result = Result.<TransactionResult>failure(Code.SERVER_ERROR, "Failed to save transaction");
    when(usecase.process(any(Context.class), any(CreateTransactionInput.class))).thenReturn(result);

    var requestBody = """
        {
          "title":"Transaction",
          "source":"account-1",
          "amount":50000,
          "category":"FOOD",
          "type":"DEBIT"
        }
        """;

    // When
    var response = Unirest.post(baseUrl + "/transactions").body(requestBody).asString();

    // Then
    assertEquals(500, response.getStatus());
    assertEquals("Failed to save transaction", response.getBody());
  }

  @Test
  void givenNotFoundError_WhenRequested_ThenShouldReturn404() {
    // Given
    var result = Result.<TransactionResult>failure(Code.NOT_FOUND, "source account not found");
    when(usecase.process(any(Context.class), any(CreateTransactionInput.class))).thenReturn(result);

    var requestBody = """
        {
          "title":"Transaction",
          "source":"non-existent-account",
          "amount":50000,
          "category":"FOOD",
          "type":"DEBIT"
        }
        """;

    // When
    var response = Unirest.post(baseUrl + "/transactions").body(requestBody).asString();

    // Then
    assertEquals(404, response.getStatus());
    assertEquals("source account not found", response.getBody());
  }

  @Test
  void givenBadRequest_WhenRequested_ThenShouldReturn400() {
    // Given
    var result = Result.<TransactionResult>failure(Code.BAD_REQUEST, "Invalid transaction type");
    when(usecase.process(any(Context.class), any(CreateTransactionInput.class))).thenReturn(result);

    var requestBody = """
        {
          "title":"Transaction",
          "source":"account-1",
          "amount":50000,
          "category":"FOOD",
          "type":"INVALID_TYPE"
        }
        """;

    // When
    var response = Unirest.post(baseUrl + "/transactions").body(requestBody).asString();

    // Then
    assertEquals(400, response.getStatus());
    assertEquals("Invalid transaction type", response.getBody());
  }
}
