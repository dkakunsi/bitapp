package io.dkakunsi.bitapp.loan.endpoint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.dkakunsi.bitapp.common.AppError.Code;
import io.dkakunsi.bitapp.domain.usecase.Result;
import io.dkakunsi.bitapp.javalin.JavalinServer;
import io.dkakunsi.bitapp.loan.dto.LoanResult;
import io.dkakunsi.bitapp.loan.dto.UpdateLoanInput;
import io.dkakunsi.bitapp.loan.usecase.UpdateLoan;
import kong.unirest.Unirest;

class UpdateLoanEndpointTest {

  private static final int PORT = 20008;

  private static String baseUrl;

  private static UpdateLoan usecase;

  private static JavalinServer server;

  @BeforeAll
  static void setup() throws Exception {
    baseUrl = "http://localhost:" + PORT;
    usecase = mock(UpdateLoan.class);
    var endpoint = new UpdateLoanEndpoint(usecase);
    server = JavalinServer.of(PORT);
    server.addEndpoint(endpoint);
    server.start();
  }

  @AfterAll
  static void destroy() {
    server.stop();
  }

  @Test
  void givenValidUpdateRequest_WhenRequested_ThenShouldReturn200AndUpdatedLoan() {
    // Given
    var loanId = "loan-123";
    var updateLoanResult = LoanResult.builder()
        .id(loanId)
        .user("user@email.com")
        .type("BORROW")
        .date("2026-01-15")
        .time("14:30")
        .partyName("Jane Smith")
        .title("Updated Personal Loan")
        .description("Updated loan description")
        .amount(new BigDecimal("7500.00"))
        .remainingAmount(new BigDecimal("7500.00"))
        .currency("USD")
        .interestRate(6.0)
        .build();
    var result = Result.success(updateLoanResult);
    when(usecase.process(any(UpdateLoanInput.class))).thenReturn(result);

    var requestBody = """
        {
          "partyName":"Jane Smith",
          "title":"Updated Personal Loan",
          "description":"Updated loan description",
          "amount":7500.00,
          "currency":"USD",
          "interestRate":6.0
        }
        """;

    // When
    var response = Unirest.put(baseUrl + "/loans/" + loanId).body(requestBody).asString();

    // Then
    assertEquals(200, response.getStatus());

    var responseBody = response.getBody();
    assertNotNull(responseBody);
    assertTrue(responseBody.contains("\"id\":\"loan-123\""));
    assertTrue(responseBody.contains("\"partyName\":\"Jane Smith\""));
    assertTrue(responseBody.contains("\"title\":\"Updated Personal Loan\""));
    assertTrue(responseBody.contains("\"description\":\"Updated loan description\""));
    assertTrue(responseBody.contains("\"amount\":7500"));
    assertTrue(responseBody.contains("\"interestRate\":6.0"));
  }

  @Test
  void givenUpdateRequestWithMinimalFields_WhenRequested_ThenShouldReturn200() {
    // Given
    var loanId = "loan-456";
    var updateLoanResult = LoanResult.builder()
        .id(loanId)
        .user("user@email.com")
        .type("LEND")
        .date("2026-02-20")
        .time("10:00")
        .partyName("Bob Johnson")
        .title("Updated Title Only")
        .description("Original description")
        .amount(new BigDecimal("5000.00"))
        .remainingAmount(new BigDecimal("5000.00"))
        .currency("IDR")
        .interestRate(3.5)
        .build();
    var result = Result.success(updateLoanResult);
    when(usecase.process(any(UpdateLoanInput.class))).thenReturn(result);

    var requestBody = """
        {
          "partyName":"Bob Johnson",
          "title":"Updated Title Only",
          "description":"Original description"
        }
        """;

    // When
    var response = Unirest.put(baseUrl + "/loans/" + loanId).body(requestBody).asString();

    // Then
    assertEquals(200, response.getStatus());

    var responseBody = response.getBody();
    assertNotNull(responseBody);
    assertTrue(responseBody.contains("\"title\":\"Updated Title Only\""));
  }

  @Test
  void givenUpdateRequestWithAllFields_WhenRequested_ThenShouldReturn200() {
    // Given
    var loanId = "loan-789";
    var updateLoanResult = LoanResult.builder()
        .id(loanId)
        .user("user@email.com")
        .type("BORROW")
        .date("2027-03-10")
        .time("15:45")
        .partyName("Alice Cooper")
        .title("Comprehensive Update")
        .description("All fields updated")
        .amount(new BigDecimal("12000.00"))
        .remainingAmount(new BigDecimal("12000.00"))
        .currency("EUR")
        .interestRate(4.2)
        .build();
    var result = Result.success(updateLoanResult);
    when(usecase.process(any(UpdateLoanInput.class))).thenReturn(result);

    var requestBody = """
        {
          "partyName":"Alice Cooper",
          "title":"Comprehensive Update",
          "description":"All fields updated",
          "amount":12000.00,
          "currency":"EUR",
          "interestRate":4.2,
          "date":"2027-03-10",
          "time":"15:45:30"
        }
        """;

    // When
    var response = Unirest.put(baseUrl + "/loans/" + loanId).body(requestBody).asString();

    // Then
    assertEquals(200, response.getStatus());

    var responseBody = response.getBody();
    assertNotNull(responseBody);
    assertTrue(responseBody.contains("\"partyName\":\"Alice Cooper\""));
    assertTrue(responseBody.contains("\"amount\":12000"));
    assertTrue(responseBody.contains("\"currency\":\"EUR\""));
    assertTrue(responseBody.contains("\"date\":\"2027-03-10\""));
  }

  @Test
  void givenEmptyOutput_WhenRequested_ThenShouldReturn200() {
    // Given
    var loanId = "loan-empty";
    var result = Result.<LoanResult>success();
    when(usecase.process(any(UpdateLoanInput.class))).thenReturn(result);

    var requestBody = """
        {
          "partyName":"Test Party",
          "title":"Test Title",
          "description":"Test description"
        }
        """;

    // When
    var response = Unirest.put(baseUrl + "/loans/" + loanId).body(requestBody).asString();

    // Then
    assertEquals(200, response.getStatus());
    assertEquals("", response.getBody());
  }

  @Test
  void givenNotFoundError_WhenRequested_ThenShouldReturn404() {
    // Given
    var loanId = "non-existent";
    var result = Result.<LoanResult>failure(Code.NOT_FOUND, "Loan not found");
    when(usecase.process(any(UpdateLoanInput.class))).thenReturn(result);

    var requestBody = """
        {
          "partyName":"Test Party",
          "title":"Test Title",
          "description":"Test description"
        }
        """;

    // When
    var response = Unirest.put(baseUrl + "/loans/" + loanId).body(requestBody).asString();

    // Then
    assertEquals(404, response.getStatus());
    assertEquals("Loan not found", response.getBody());
  }

  @Test
  void givenForbiddenError_WhenRequested_ThenShouldReturn403() {
    // Given
    var loanId = "loan-forbidden";
    var result = Result.<LoanResult>failure(Code.FORBIDDEN, "You are not authorized to update this loan");
    when(usecase.process(any(UpdateLoanInput.class))).thenReturn(result);

    var requestBody = """
        {
          "partyName":"Test Party",
          "title":"Test Title",
          "description":"Test description"
        }
        """;

    // When
    var response = Unirest.put(baseUrl + "/loans/" + loanId).body(requestBody).asString();

    // Then
    assertEquals(403, response.getStatus());
    assertEquals("You are not authorized to update this loan", response.getBody());
  }

  @Test
  void givenBadRequestError_WhenRequested_ThenShouldReturn400() {
    // Given
    var loanId = "loan-bad";
    var result = Result.<LoanResult>failure(Code.BAD_REQUEST, "title: invalid value");
    when(usecase.process(any(UpdateLoanInput.class))).thenReturn(result);

    var requestBody = """
        {
          "partyName":"Test Party",
          "title":"",
          "description":"Test description"
        }
        """;

    // When
    var response = Unirest.put(baseUrl + "/loans/" + loanId).body(requestBody).asString();

    // Then
    assertEquals(400, response.getStatus());
    assertEquals("title: invalid value", response.getBody());
  }

  @Test
  void givenServerError_WhenRequested_ThenShouldReturn500() {
    // Given
    var loanId = "loan-error";
    var result = Result.<LoanResult>failure(Code.SERVER_ERROR, "Internal server error");
    when(usecase.process(any(UpdateLoanInput.class))).thenReturn(result);

    var requestBody = """
        {
          "partyName":"Test Party",
          "title":"Test Title",
          "description":"Test description"
        }
        """;

    // When
    var response = Unirest.put(baseUrl + "/loans/" + loanId).body(requestBody).asString();

    // Then
    assertEquals(500, response.getStatus());
    assertEquals("Internal server error", response.getBody());
  }

  @Test
  void givenUpdateWithNewCurrency_WhenRequested_ThenShouldReturn200() {
    // Given
    var loanId = "loan-currency";
    var updateLoanResult = LoanResult.builder()
        .id(loanId)
        .user("user@email.com")
        .type("BORROW")
        .date("2026-01-15")
        .time("10:00")
        .partyName("Test Party")
        .title("Currency Update Test")
        .description("Testing currency update")
        .amount(new BigDecimal("10000.00"))
        .remainingAmount(new BigDecimal("10000.00"))
        .currency("JPY")
        .interestRate(2.5)
        .build();
    var result = Result.success(updateLoanResult);
    when(usecase.process(any(UpdateLoanInput.class))).thenReturn(result);

    var requestBody = """
        {
          "partyName":"Test Party",
          "title":"Currency Update Test",
          "description":"Testing currency update",
          "currency":"JPY"
        }
        """;

    // When
    var response = Unirest.put(baseUrl + "/loans/" + loanId).body(requestBody).asString();

    // Then
    assertEquals(200, response.getStatus());
    var responseBody = response.getBody();
    assertTrue(responseBody.contains("\"currency\":\"JPY\""));
  }

  @Test
  void givenUpdateWithNewDateAndTime_WhenRequested_ThenShouldReturn200() {
    // Given
    var loanId = "loan-datetime";
    var updateLoanResult = LoanResult.builder()
        .id(loanId)
        .user("user@email.com")
        .type("LEND")
        .date("2026-12-25")
        .time("23:59")
        .partyName("Holiday Party")
        .title("Holiday Loan")
        .description("Special holiday loan")
        .amount(new BigDecimal("5000.00"))
        .remainingAmount(new BigDecimal("5000.00"))
        .currency("USD")
        .interestRate(3.0)
        .build();
    var result = Result.success(updateLoanResult);
    when(usecase.process(any(UpdateLoanInput.class))).thenReturn(result);

    var requestBody = """
        {
          "partyName":"Holiday Party",
          "title":"Holiday Loan",
          "description":"Special holiday loan",
          "date":"2026-12-25",
          "time":"23:59:00"
        }
        """;

    // When
    var response = Unirest.put(baseUrl + "/loans/" + loanId).body(requestBody).asString();

    // Then
    assertEquals(200, response.getStatus());
    var responseBody = response.getBody();
    assertTrue(responseBody.contains("\"date\":\"2026-12-25\""));
    assertTrue(responseBody.contains("\"time\":\"23:59\""));
  }
}
