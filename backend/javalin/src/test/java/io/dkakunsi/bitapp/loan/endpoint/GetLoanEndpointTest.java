package io.dkakunsi.bitapp.loan.endpoint;

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
import io.dkakunsi.bitapp.domain.usecase.Result;
import io.dkakunsi.bitapp.javalin.JavalinServer;
import io.dkakunsi.bitapp.loan.dto.LoanResult;
import io.dkakunsi.bitapp.loan.usecase.GetLoan;
import io.dkakunsi.bitapp.common.DateTimeConverter;
import kong.unirest.Unirest;

class GetLoanEndpointTest {

  private static final int PORT = 20011;

  private static String baseUrl;

  private static GetLoan usecase;

  private static JavalinServer server;

  @BeforeAll
  static void setup() throws Exception {
    baseUrl = "http://localhost:" + PORT;
    usecase = mock(GetLoan.class);
    var endpoint = new GetLoanEndpoint(usecase);
    server = JavalinServer.of(PORT);
    server.addEndpoint(endpoint);
    server.start();
  }

  @AfterAll
  static void destroy() {
    server.stop();
  }

  @Test
  void givenValidLoanId_WhenRequested_ThenShouldReturnLoan() {
    // Given
    var loanId = "loan-123";
    var date = DateTimeConverter.epochMilli(LocalDate.of(2026, 1, 15));
    var time = DateTimeConverter.minutesSinceMidnight(LocalTime.of(10, 30));
    var getResult = LoanResult.builder()
        .id(loanId)
        .user("user@email.com")
        .type("BORROW")
        .date(date)
        .time(time)
        .partyName("John Doe")
        .title("Personal Loan")
        .description("Loan for personal use")
        .amount(BigDecimal.valueOf(10000))
        .remainingAmount(BigDecimal.valueOf(8000))
        .currency("IDR")
        .interestRate(5.5)
        .build();
    var result = Result.success(getResult);
    when(usecase.process(any(String.class))).thenReturn(result);

    // When
    var response = Unirest.get(baseUrl + "/v1/loans/" + loanId).asString();

    // Then
    assertEquals(200, response.getStatus());

    var responseBody = response.getBody();
    assertNotNull(responseBody);
    var resultBody = new JSONObject(responseBody);
    assertEquals(loanId, resultBody.getString("id"));
    assertEquals("user@email.com", resultBody.getString("user"));
    assertEquals("BORROW", resultBody.getString("type"));
    assertEquals(1768435200000L, resultBody.getLong("date"));
    assertEquals(630, resultBody.getInt("time"));
    assertEquals("John Doe", resultBody.getString("partyName"));
    assertEquals("Personal Loan", resultBody.getString("title"));
    assertEquals("Loan for personal use", resultBody.getString("description"));
    assertEquals(BigDecimal.valueOf(10000), resultBody.getBigDecimal("amount"));
    assertEquals(BigDecimal.valueOf(8000), resultBody.getBigDecimal("remainingAmount"));
    assertEquals("IDR", resultBody.getString("currency"));
    assertEquals(5.5, resultBody.getDouble("interestRate"));
  }

  @Test
  void givenNonExistentLoanId_WhenRequested_ThenShouldReturn404() {
    // Given
    var loanId = "nonexistent-loan";
    var result = Result.<LoanResult>failure(Code.NOT_FOUND, "Loan not found");
    when(usecase.process(any(String.class))).thenReturn(result);

    // When
    var response = Unirest.get(baseUrl + "/v1/loans/" + loanId).asString();

    // Then
    assertEquals(404, response.getStatus());
    assertEquals("Loan not found", response.getBody());
  }

  @Test
  void givenServerError_WhenRequested_ThenShouldReturn500() {
    // Given
    var loanId = "loan-123";
    var result = Result.<LoanResult>failure(Code.SERVER_ERROR, "Database connection failed");
    when(usecase.process(any(String.class))).thenReturn(result);

    // When
    var response = Unirest.get(baseUrl + "/v1/loans/" + loanId).asString();

    // Then
    assertEquals(500, response.getStatus());
    assertEquals("Database connection failed", response.getBody());
  }
}
