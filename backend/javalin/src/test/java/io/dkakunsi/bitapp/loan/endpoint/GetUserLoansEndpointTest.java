package io.dkakunsi.bitapp.loan.endpoint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import io.dkakunsi.bitapp.loan.dto.LoanResult;
import io.dkakunsi.bitapp.loan.usecase.GetUserLoans;
import io.dkakunsi.bitapp.common.DateTimeConverter;
import kong.unirest.Unirest;

class GetUserLoansEndpointTest {

  private static final int PORT = 20009;

  private static String baseUrl;

  private static final String USER_ID = "user123";

  private static GetUserLoans usecase;

  private static JavalinServer server;

  @BeforeAll
  static void setup() throws Exception {
    baseUrl = "http://localhost:" + PORT;
    usecase = mock(GetUserLoans.class);
    var endpoint = new GetUserLoansEndpoint(usecase);
    server = JavalinServer.of(PORT);
    server.addEndpoint(endpoint);
    server.start();
  }

  @AfterAll
  static void destroy() {
    server.stop();
  }

  @AfterEach
  void resetMocks() {
    reset(usecase);
  }

  @Test
  void givenValidUserIdWithMultipleLoans_WhenRequested_ThenShouldReturn200AndLoansList() {
    // Given
    var loanItem1 = LoanResult.builder()
        .id("loan1")
        .user(USER_ID)
        .type("BORROW")
        .date(DateTimeConverter.epochMilli(LocalDate.of(2024, 6, 15)))
        .time(DateTimeConverter.minutesSinceMidnight(LocalTime.of(14, 30)))
        .partyName("Bank ABC")
        .title("Car Loan")
        .description("Loan for purchasing a car")
        .amount(BigDecimal.valueOf(500000000))
        .remainingAmount(BigDecimal.valueOf(500000000))
        .currency("IDR")
        .interestRate(5.5)
        .build();
    var loanItem2 = LoanResult.builder()
        .id("loan2")
        .user(USER_ID)
        .type("LEND")
        .date(DateTimeConverter.epochMilli(LocalDate.of(2024, 6, 20)))
        .time(DateTimeConverter.minutesSinceMidnight(LocalTime.of(10, 0)))
        .partyName("John Doe")
        .title("Personal Loan")
        .description("Money lent to friend")
        .amount(BigDecimal.valueOf(10000000))
        .remainingAmount(BigDecimal.valueOf(10000000))
        .currency("IDR")
        .interestRate(2.0)
        .build();
    var getResult = List.of(loanItem1, loanItem2);
    var result = Result.success(getResult);
    when(usecase.process(any(String.class))).thenReturn(result);

    // When
    var response = Unirest.get(baseUrl + "/v1/users/{userId}/loans")
        .routeParam("userId", USER_ID)
        .asString();

    // Then
    assertEquals(200, response.getStatus());

    var responseBody = response.getBody();
    assertNotNull(responseBody);
    var resultBody = new JSONArray(responseBody);
    assertEquals(2, resultBody.length());
    var loan1 = resultBody.getJSONObject(0);
    assertEquals("loan1", loan1.getString("id"));
    assertEquals(USER_ID, loan1.getString("user"));
    assertEquals("BORROW", loan1.getString("type"));
    assertEquals("Bank ABC", loan1.getString("partyName"));
    assertEquals("Car Loan", loan1.getString("title"));
    assertEquals(500000000, loan1.getDouble("amount"));
    assertEquals("IDR", loan1.getString("currency"));
    assertEquals(5.5, loan1.getDouble("interestRate"));
    var loan2 = resultBody.getJSONObject(1);
    assertEquals("loan2", loan2.getString("id"));
    assertEquals("LEND", loan2.getString("type"));
    assertEquals("John Doe", loan2.getString("partyName"));
    assertEquals("Personal Loan", loan2.getString("title"));
  }

  @Test
  void givenValidUserIdWithNoLoans_WhenRequested_ThenShouldReturn200AndEmptyList() {
    // Given
    var result = Result.<List<LoanResult>>success(List.of());
    when(usecase.process(any(String.class))).thenReturn(result);

    // When
    var response = Unirest.get(baseUrl + "/v1/users/{userId}/loans")
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
  void givenValidUserIdWithSingleLoan_WhenRequested_ThenShouldReturn200AndSingleLoanList() {
    // Given
    var loanItem = LoanResult.builder()
        .id("loan1")
        .user(USER_ID)
        .type("BORROW")
        .date(DateTimeConverter.epochMilli(LocalDate.of(2024, 6, 15)))
        .time(DateTimeConverter.minutesSinceMidnight(LocalTime.of(14, 30)))
        .partyName("Credit Union")
        .title("Home Renovation")
        .description("Loan for home improvement")
        .amount(BigDecimal.valueOf(200000000))
        .remainingAmount(BigDecimal.valueOf(200000000))
        .currency("IDR")
        .interestRate(4.5)
        .build();
    var result = Result.success(List.of(loanItem));
    when(usecase.process(any(String.class))).thenReturn(result);

    // When
    var response = Unirest.get(baseUrl + "/v1/users/{userId}/loans")
        .routeParam("userId", USER_ID)
        .asString();

    // Then
    assertEquals(200, response.getStatus());

    var responseBody = response.getBody();
    assertNotNull(responseBody);
    assertTrue(responseBody.contains("\"id\":\"loan1\""));
    assertTrue(responseBody.contains("\"title\":\"Home Renovation\""));
    assertTrue(responseBody.contains("\"type\":\"BORROW\""));
    assertTrue(responseBody.contains("\"amount\":200000000"));
  }

  @Test
  void givenValidRequest_WhenUseCaseReturnsEmpty_ThenShouldReturn200() {
    // Given
    var result = Result.<List<LoanResult>>success(List.of());
    when(usecase.process(any(String.class))).thenReturn(result);

    // When
    var response = Unirest.get(baseUrl + "/v1/users/{userId}/loans")
        .routeParam("userId", USER_ID)
        .asString();

    // Then
    assertEquals(200, response.getStatus());
  }

  @Test
  void givenValidRequest_WhenUseCaseFails_ThenShouldReturn500() {
    // Given
    var result = Result.<List<LoanResult>>failure(Code.SERVER_ERROR, "Database error");
    when(usecase.process(any(String.class))).thenReturn(result);

    // When
    var response = Unirest.get(baseUrl + "/v1/users/{userId}/loans")
        .routeParam("userId", USER_ID)
        .asString();

    // Then
    assertEquals(500, response.getStatus());
    assertEquals("Database error", response.getBody());
  }
}
