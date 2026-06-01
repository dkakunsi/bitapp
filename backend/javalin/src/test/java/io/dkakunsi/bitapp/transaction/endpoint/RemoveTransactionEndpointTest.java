package io.dkakunsi.bitapp.transaction.endpoint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.dkakunsi.bitapp.common.AppError.Code;
import io.dkakunsi.bitapp.common.DateTimeConverter;
import io.dkakunsi.bitapp.domain.usecase.Result;
import io.dkakunsi.bitapp.javalin.JavalinServer;
import io.dkakunsi.bitapp.transaction.dto.TransactionResult;
import io.dkakunsi.bitapp.transaction.usecase.RemoveTransaction;
import kong.unirest.Unirest;

class RemoveTransactionEndpointTest {

  private static final int PORT = 20016;

  private static String baseUrl;

  private static RemoveTransaction usecase;

  private static JavalinServer server;

  @BeforeAll
  static void setup() throws Exception {
    baseUrl = "http://localhost:" + PORT;
    usecase = mock(RemoveTransaction.class);
    var endpoint = new RemoveTransactionEndpoint(usecase);
    server = JavalinServer.of(PORT);
    server.addEndpoint(endpoint);
    server.start();
  }

  @AfterAll
  static void destroy() {
    server.stop();
  }

  @Test
  void givenValidTransactionId_WhenDeleted_ThenShouldReturnTransactionDetails() {
    // Given
    var transactionId = "trans-123";
    var date = DateTimeConverter.epochMilli(LocalDate.of(2026, 1, 24));
    var time = DateTimeConverter.minutesSinceMidnight(LocalTime.of(10, 0));
    var transactionResult = TransactionResult.builder()
        .id(transactionId)
        .user("user@email.com")
        .title("Grocery Shopping")
        .description("Weekly groceries")
        .date(date)
        .time(time)
        .source("account-1")
        .amount(BigDecimal.valueOf(150000))
        .currency("IDR")
        .category("FOOD")
        .type("DEBIT")
        .build();
    var result = Result.success(transactionResult);
    when(usecase.process(any(String.class))).thenReturn(result);

    // When
    var response = Unirest.delete(baseUrl + "/v1/transactions/" + transactionId).asString();

    // Then
    assertEquals(200, response.getStatus());

    var responseBody = response.getBody();
    assertNotNull(responseBody);
    var resultBody = new JSONObject(responseBody);
    assertEquals(transactionId, resultBody.getString("id"));
    assertEquals("Grocery Shopping", resultBody.getString("title"));
    assertEquals("DEBIT", resultBody.getString("type"));
    assertEquals(150000, resultBody.getLong("amount"));
  }

  @Test
  void givenNonExistentTransactionId_WhenDeleted_ThenShouldReturn404() {
    // Given
    var transactionId = "trans-404";
    var result = Result.<TransactionResult>failure(Code.NOT_FOUND, "Transaction not found");
    when(usecase.process(any(String.class))).thenReturn(result);

    // When
    var response = Unirest.delete(baseUrl + "/v1/transactions/" + transactionId).asString();

    // Then
    assertEquals(404, response.getStatus());
    assertEquals("Transaction not found", response.getBody());
  }
}
