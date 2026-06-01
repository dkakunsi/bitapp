package io.dkakunsi.bitapp.loan.endpoint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.dkakunsi.bitapp.common.AppError.Code;
import io.dkakunsi.bitapp.common.DateTimeConverter;
import io.dkakunsi.bitapp.domain.usecase.Result;
import io.dkakunsi.bitapp.javalin.JavalinServer;
import io.dkakunsi.bitapp.loan.dto.CreateLoanInput;
import io.dkakunsi.bitapp.loan.dto.LoanResult;
import io.dkakunsi.bitapp.loan.usecase.CreateLoan;
import kong.unirest.Unirest;

class CreateLoanEndpointTest {

  private static final int PORT = 20007;

  private static String baseUrl;

  private static CreateLoan usecase;

  private static JavalinServer server;

  @BeforeAll
  static void setup() throws Exception {
    baseUrl = "http://localhost:" + PORT;
    usecase = mock(CreateLoan.class);
    var endpoint = new CreateLoanEndpoint(usecase);
    server = JavalinServer.of(PORT);
    server.addEndpoint(endpoint);
    server.start();
  }

  @AfterAll
  static void destroy() {
    server.stop();
  }

  @Test
  void givenValidBorrowLoanRequest_WhenRequested_ThenShouldReturn200AndLoan() {
    // Given
    var date = DateTimeConverter.epochMilli(LocalDate.of(2026, 1, 15));
    var time = DateTimeConverter.minutesSinceMidnight(LocalTime.of(14, 30));
    var createLoanResult = LoanResult.builder()
        .id("loan-123")
        .user("user@email.com")
        .type("BORROW")
        .date(date)
        .time(time)
        .partyName("John Doe")
        .title("Personal Loan")
        .description("Emergency loan")
        .amount(new BigDecimal("5000.00"))
        .remainingAmount(new BigDecimal("5000.00"))
        .currency("USD")
        .interestRate(5.5)
        .build();
    var result = Result.success(createLoanResult);
    when(usecase.process(any(CreateLoanInput.class))).thenReturn(result);

    var requestBody = """
        {
          "type":"BORROW",
          "date": %d,
          "time": %d,
          "partyName":"John Doe",
          "title":"Personal Loan",
          "description":"Emergency loan",
          "amount":5000.00,
          "currency":"USD",
          "interestRate":5.5
        }
        """.formatted(date, time);

    // When
    var response = Unirest.post(baseUrl + "/v1/loans").body(requestBody).asString();

    // Then
    assertEquals(200, response.getStatus());

    var responseBody = response.getBody();
    assertNotNull(responseBody);
    assertTrue(responseBody.contains("\"id\":\"loan-123\""));
    assertTrue(responseBody.contains("\"type\":\"BORROW\""));
    assertTrue(responseBody.contains("\"partyName\":\"John Doe\""));
    assertTrue(responseBody.contains("\"title\":\"Personal Loan\""));
    assertTrue(responseBody.contains("\"amount\":5000"));
    assertTrue(responseBody.contains("\"currency\":\"USD\""));
    assertTrue(responseBody.contains("\"interestRate\":5.5"));
  }

  @Test
  void givenValidLendLoanRequest_WhenRequested_ThenShouldReturn200AndLoan() {
    // Given
    var date = DateTimeConverter.epochMilli(LocalDate.of(2026, 1, 20));
    var time = DateTimeConverter.minutesSinceMidnight(LocalTime.of(10, 0));
    var createLoanResult = LoanResult.builder()
        .id("loan-456")
        .user("user@email.com")
        .type("LEND")
        .date(date)
        .time(time)
        .partyName("Jane Smith")
        .title("Business Loan")
        .description("Investment")
        .amount(new BigDecimal("10000.00"))
        .remainingAmount(new BigDecimal("10000.00"))
        .currency("IDR")
        .interestRate(3.0)
        .build();
    var result = Result.success(createLoanResult);
    when(usecase.process(any(CreateLoanInput.class))).thenReturn(result);

    var requestBody = """
        {
          "type":"LEND",
          "date": %d,
          "time": %d,
          "partyName":"Jane Smith",
          "title":"Business Loan",
          "description":"Investment",
          "amount":10000.00,
          "currency":"IDR",
          "interestRate":3.0
        }
        """.formatted(date, time);

    // When
    var response = Unirest.post(baseUrl + "/v1/loans").body(requestBody).asString();

    // Then
    assertEquals(200, response.getStatus());

    var responseBody = response.getBody();
    assertNotNull(responseBody);
    assertTrue(responseBody.contains("\"id\":\"loan-456\""));
    assertTrue(responseBody.contains("\"type\":\"LEND\""));
    assertTrue(responseBody.contains("\"partyName\":\"Jane Smith\""));
    assertTrue(responseBody.contains("\"title\":\"Business Loan\""));
  }

  @Test
  void givenValidLoanRequestWithMinimalFields_WhenRequested_ThenShouldReturn200() {
    // Given
    var date = DateTimeConverter.epochMilli(LocalDate.now());
    var time = DateTimeConverter.minutesSinceMidnight(LocalTime.now());
    var createLoanResult = LoanResult.builder()
        .id("loan-789")
        .user("user@email.com")
        .type("BORROW")
        .date(date)
        .time(time)
        .partyName("Test Party")
        .title("Test Loan")
        .amount(new BigDecimal("1000.00"))
        .remainingAmount(new BigDecimal("1000.00"))
        .currency("IDR")
        .interestRate(0.0)
        .build();
    var result = Result.success(createLoanResult);
    when(usecase.process(any(CreateLoanInput.class))).thenReturn(result);

    var requestBody = """
        {
          "type":"BORROW",
          "partyName":"Test Party",
          "title":"Test Loan",
          "amount":1000.00,
          "interestRate":0.0
        }
        """;

    // When
    var response = Unirest.post(baseUrl + "/v1/loans").body(requestBody).asString();

    // Then
    assertEquals(200, response.getStatus());
    assertNotNull(response.getBody());
  }

  @Test
  void givenValidRequestWithEmptyOutput_WhenRequested_ThenShouldReturn200() {
    // Given
    var result = Result.<LoanResult>success();
    when(usecase.process(any(CreateLoanInput.class))).thenReturn(result);

    var requestBody = """
        {
          "type":"BORROW",
          "partyName":"Party Name",
          "title":"Loan Title",
          "amount":2000.00,
          "interestRate":2.5
        }
        """;

    // When
    var response = Unirest.post(baseUrl + "/v1/loans").body(requestBody).asString();

    // Then
    assertEquals(200, response.getStatus());
    assertEquals("", response.getBody());
  }

  @Test
  void givenServerError_WhenRequested_ThenShouldReturn500() {
    // Given
    var result = Result.<LoanResult>failure(Code.SERVER_ERROR, "Failed to save loan");
    when(usecase.process(any(CreateLoanInput.class))).thenReturn(result);

    var requestBody = """
        {
          "type":"LEND",
          "partyName":"Error Party",
          "title":"Error Loan",
          "amount":3000.00,
          "interestRate":4.0
        }
        """;

    // When
    var response = Unirest.post(baseUrl + "/v1/loans").body(requestBody).asString();

    // Then
    assertEquals(500, response.getStatus());
    assertEquals("Failed to save loan", response.getBody());
  }

  @Test
  void givenBadRequest_WhenRequested_ThenShouldReturn400() {
    // Given
    var result = Result.<LoanResult>failure(Code.BAD_REQUEST, "Invalid loan type");
    when(usecase.process(any(CreateLoanInput.class))).thenReturn(result);

    var requestBody = """
        {
          "type":"INVALID",
          "partyName":"Party Name",
          "title":"Loan Title",
          "amount":1500.00,
          "interestRate":3.5
        }
        """;

    // When
    var response = Unirest.post(baseUrl + "/v1/loans").body(requestBody).asString();

    // Then
    assertEquals(400, response.getStatus());
    assertEquals("Invalid loan type", response.getBody());
  }

  @Test
  void givenNotFoundError_WhenRequested_ThenShouldReturn404() {
    // Given
    var result = Result.<LoanResult>failure(Code.NOT_FOUND, "Party not found");
    when(usecase.process(any(CreateLoanInput.class))).thenReturn(result);

    var requestBody = """
        {
          "type":"BORROW",
          "partyName":"Unknown Party",
          "title":"Loan Title",
          "amount":2500.00,
          "interestRate":5.0
        }
        """;

    // When
    var response = Unirest.post(baseUrl + "/v1/loans").body(requestBody).asString();

    // Then
    assertEquals(404, response.getStatus());
    assertEquals("Party not found", response.getBody());
  }

  @Test
  void givenUnauthorizedError_WhenRequested_ThenShouldReturn401() {
    // Given
    var result = Result.<LoanResult>failure(Code.UNAUTHORIZED, "User not authorized");
    when(usecase.process(any(CreateLoanInput.class))).thenReturn(result);

    var requestBody = """
        {
          "type":"LEND",
          "partyName":"Party Name",
          "title":"Unauthorized Loan",
          "amount":4000.00,
          "interestRate":6.0
        }
        """;

    // When
    var response = Unirest.post(baseUrl + "/v1/loans").body(requestBody).asString();

    // Then
    assertEquals(401, response.getStatus());
    assertEquals("User not authorized", response.getBody());
  }

  @Test
  void givenLoanRequestWithZeroInterestRate_WhenRequested_ThenShouldReturn200() {
    // Given
    var date = DateTimeConverter.epochMilli(LocalDate.now());
    var time = DateTimeConverter.minutesSinceMidnight(LocalTime.now());
    var createLoanResult = LoanResult.builder()
        .id("loan-zero")
        .user("user@email.com")
        .type("BORROW")
        .date(date)
        .time(time)
        .partyName("Interest Free Party")
        .title("No Interest Loan")
        .amount(new BigDecimal("3000.00"))
        .remainingAmount(new BigDecimal("3000.00"))
        .currency("IDR")
        .interestRate(0.0)
        .build();
    var result = Result.success(createLoanResult);
    when(usecase.process(any(CreateLoanInput.class))).thenReturn(result);

    var requestBody = """
        {
          "type":"BORROW",
          "partyName":"Interest Free Party",
          "title":"No Interest Loan",
          "amount":3000.00,
          "interestRate":0.0
        }
        """;

    // When
    var response = Unirest.post(baseUrl + "/v1/loans").body(requestBody).asString();

    // Then
    assertEquals(200, response.getStatus());
    var responseBody = response.getBody();
    assertNotNull(responseBody);
    assertTrue(responseBody.contains("\"interestRate\":0"));
  }

  @Test
  void givenLoanRequestWithDescription_WhenRequested_ThenShouldReturn200WithDescription() {
    // Given
    var date = DateTimeConverter.epochMilli(LocalDate.now());
    var time = DateTimeConverter.minutesSinceMidnight(LocalTime.now());
    var createLoanResult = LoanResult.builder()
        .id("loan-desc")
        .user("user@email.com")
        .type("LEND")
        .date(date)
        .time(time)
        .partyName("Described Party")
        .title("Described Loan")
        .description("This loan has a detailed description")
        .amount(new BigDecimal("7500.00"))
        .remainingAmount(new BigDecimal("7500.00"))
        .currency("USD")
        .interestRate(4.5)
        .build();
    var result = Result.success(createLoanResult);
    when(usecase.process(any(CreateLoanInput.class))).thenReturn(result);

    var requestBody = """
        {
          "type":"LEND",
          "partyName":"Described Party",
          "title":"Described Loan",
          "description":"This loan has a detailed description",
          "amount":7500.00,
          "currency":"USD",
          "interestRate":4.5
        }
        """;

    // When
    var response = Unirest.post(baseUrl + "/v1/loans").body(requestBody).asString();

    // Then
    assertEquals(200, response.getStatus());
    var responseBody = response.getBody();
    assertNotNull(responseBody);
    assertTrue(responseBody.contains("\"description\":\"This loan has a detailed description\""));
  }
}
