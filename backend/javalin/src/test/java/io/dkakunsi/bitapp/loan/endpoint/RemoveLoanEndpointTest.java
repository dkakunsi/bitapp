package io.dkakunsi.bitapp.loan.endpoint;

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
import io.dkakunsi.bitapp.domain.usecase.Result;
import io.dkakunsi.bitapp.javalin.JavalinServer;
import io.dkakunsi.bitapp.loan.dto.LoanResult;
import io.dkakunsi.bitapp.loan.usecase.RemoveLoan;
import kong.unirest.Unirest;

class RemoveLoanEndpointTest {

  private static final int PORT = 20017;

  private static String baseUrl;

  private static RemoveLoan usecase;

  private static JavalinServer server;

  @BeforeAll
  static void setup() throws Exception {
    baseUrl = "http://localhost:" + PORT;
    usecase = mock(RemoveLoan.class);
    var endpoint = new RemoveLoanEndpoint(usecase);
    server = JavalinServer.of(PORT);
    server.addEndpoint(endpoint);
    server.start();
  }

  @AfterAll
  static void destroy() {
    server.stop();
  }

  @Test
  void givenValidLoanId_WhenDeleted_ThenShouldReturnLoanDetails() {
    // Given
    var loanId = "loan-123";
    var loanResult = LoanResult.builder()
        .id(loanId)
        .user("user@email.com")
        .type("BORROW")
        .date("2026-01-24")
        .time("10:00")
        .partyName("Test Bank")
        .title("Test Loan")
        .description("Test")
        .amount(new BigDecimal("1000000"))
        .remainingAmount(new BigDecimal("1000000"))
        .currency("IDR")
        .interestRate(5.0)
        .build();
    var result = Result.success(loanResult);
    when(usecase.process(any(String.class))).thenReturn(result);

    // When
    var response = Unirest.delete(baseUrl + "/loans/" + loanId).asString();

    // Then
    assertEquals(200, response.getStatus());

    var responseBody = response.getBody();
    assertNotNull(responseBody);
    var resultBody = new JSONObject(responseBody);
    assertEquals(loanId, resultBody.getString("id"));
    assertEquals("Test Loan", resultBody.getString("title"));
    assertEquals("BORROW", resultBody.getString("type"));
    assertEquals(1000000, resultBody.getBigDecimal("amount").intValue());
  }

  @Test
  void givenNonExistentLoanId_WhenDeleted_ThenShouldReturn404() {
    // Given
    var loanId = "loan-404";
    var result = Result.<LoanResult>failure(Code.NOT_FOUND, "Loan not found");
    when(usecase.process(any(String.class))).thenReturn(result);

    // When
    var response = Unirest.delete(baseUrl + "/loans/" + loanId).asString();

    // Then
    assertEquals(404, response.getStatus());
    assertEquals("Loan not found", response.getBody());
  }
}
