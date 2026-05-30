package io.dkakunsi.bitapp.loan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.dkakunsi.bitapp.AppLauncher;
import io.dkakunsi.bitapp.jwt.JWTAuthorizer;
import io.dkakunsi.bitapp.test.AppTestUtil;
import io.dkakunsi.bitapp.test.SecureTestUtil;
import kong.unirest.Unirest;

public class CreateLoanIT extends AppTestUtil {

  private static CreateLoanIT sut = new CreateLoanIT();

  private static String baseUrl;

  private static String token;

  @BeforeAll
  static void setup() throws Exception {
    var port = getPort();
    var appEnv = Map.of(APP_PORT, Integer.toString(port),
        JWTAuthorizer.JWT_PUBLIC_KEY, SecureTestUtil.PUBLIC_KEY);
    sut.create(appEnv);
    sut.startServer(new AppLauncher());

    baseUrl = "http://localhost:" + port;
    token = SecureTestUtil.generateToken(USER_ID);
  }

  @AfterAll
  static void tearDown() throws Exception {
    sut.destroy();
  }

  /**
   * <b>Given</b> a loan creation request without authorization header<br>
   * <b>When</b> the POST /loans endpoint is called<br>
   * <b>Then</b> the request should fail with a 401 status code
   */
  @Test
  public void createLoanWithoutAuthorizationHeaderShouldReturn401() throws Exception {
    var body = """
        {
          "type": "LEND",
          "partyName": "John Doe",
          "title": "Loan for Car",
          "description": "Lending money to buy a car",
          "amount": 500000000,
          "currency": "IDR",
          "interestRate": 5.5
        }
        """;

    var response = Unirest.post(baseUrl + "/v1/loans")
        .body(body)
        .asString();

    assertEquals(401, response.getStatus());
  }

  /**
   * <b>Given</b> a valid borrow loan creation request<br>
   * <b>When</b> the POST /loans endpoint is called<br>
   * <b>Then</b> a new loan is created and returned with a 200 status code
   */
  @Test
  public void createBorrowLoanShouldBeOk() throws Exception {
    var body = """
        {
          "type": "BORROW",
          "partyName": "John Doe",
          "date": "2024-06-15",
          "time": "14:30:00",
          "title": "Loan for Car",
          "description": "Borrowing money to buy a car",
          "amount": 500000000,
          "currency": "IDR",
          "interestRate": 5.5
        }
        """;

    var response = Unirest.post(baseUrl + "/v1/loans")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(200, response.getStatus());
    var responseBody = new JSONObject(response.getBody());
    assertNotNull(responseBody.getString("id"));
    assertEquals(USER_ID, responseBody.getString("user"));
    assertEquals("BORROW", responseBody.getString("type"));
    assertEquals("John Doe", responseBody.getString("partyName"));
    assertEquals("2024-06-15", responseBody.getString("date"));
    assertEquals("14:30", responseBody.getString("time"));
    assertEquals("Loan for Car", responseBody.getString("title"));
    assertEquals("Borrowing money to buy a car", responseBody.getString("description"));
    assertEquals(500000000, responseBody.getLong("amount"));
    assertEquals(500000000, responseBody.getLong("remainingAmount"));
    assertEquals("IDR", responseBody.getString("currency"));
    assertEquals(5.5, responseBody.getDouble("interestRate"));

  }

  /**
   * <b>Given</b> a valid borrow loan creation request without currency<br>
   * <b>When</b> the POST /loans endpoint is called<br>
   * <b>Then</b> a new loan is created with IDR as the default currency and
   * returned with a 200 status code
   */
  @Test
  public void shouldUseIdrAsDefaultCurrency() throws Exception {
    var body = """
        {
          "type": "BORROW",
          "partyName": "John Doe",
          "date": "2024-06-15",
          "time": "14:30:00",
          "title": "Loan for Car",
          "description": "Borrowing money to buy a car",
          "amount": 500000000,
          "interestRate": 5.5
        }
        """;

    var response = Unirest.post(baseUrl + "/v1/loans")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(200, response.getStatus());
    var responseBody = new JSONObject(response.getBody());
    assertNotNull(responseBody.getString("id"));
    assertEquals(USER_ID, responseBody.getString("user"));
    assertEquals("BORROW", responseBody.getString("type"));
    assertEquals("John Doe", responseBody.getString("partyName"));
    assertEquals("2024-06-15", responseBody.getString("date"));
    assertEquals("14:30", responseBody.getString("time"));
    assertEquals("Loan for Car", responseBody.getString("title"));
    assertEquals("Borrowing money to buy a car", responseBody.getString("description"));
    assertEquals(500000000, responseBody.getLong("amount"));
    assertEquals(500000000, responseBody.getLong("remainingAmount"));
    assertEquals("IDR", responseBody.getString("currency"));
    assertEquals(5.5, responseBody.getDouble("interestRate"));
  }

  /**
   * <b>Given</b> a valid borrow loan creation request without a date and time<br>
   * <b>When</b> the POST /loans endpoint is called<br>
   * <b>Then</b> a new loan is created with current date and time as the value and
   * returned with a 200 status code
   */
  @Test
  public void shouldUseNowAsDefaultDateAndTime() throws Exception {
    var body = """
        {
          "type": "BORROW",
          "partyName": "John Doe",
          "title": "Loan for Car",
          "description": "Borrowing money to buy a car",
          "amount": 500000000,
          "currency": "IDR",
          "interestRate": 5.5
        }
        """;

    var response = Unirest.post(baseUrl + "/v1/loans")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(200, response.getStatus());
    var responseBody = new JSONObject(response.getBody());
    assertNotNull(responseBody.getString("id"));
    assertEquals(USER_ID, responseBody.getString("user"));
    assertEquals("BORROW", responseBody.getString("type"));
    assertEquals("John Doe", responseBody.getString("partyName"));
    assertEquals("Loan for Car", responseBody.getString("title"));
    assertEquals("Borrowing money to buy a car", responseBody.getString("description"));
    assertEquals(500000000, responseBody.getLong("amount"));
    assertEquals(500000000, responseBody.getLong("remainingAmount"));
    assertEquals("IDR", responseBody.getString("currency"));
    assertEquals(5.5, responseBody.getDouble("interestRate"));
    assertNotNull(responseBody.getString("date"));
    assertNotNull(responseBody.getString("time"));

    // Verify date and time are close to current time (within a few seconds
    // tolerance)
    var expectedDate = java.time.LocalDate.now().toString();
    var expectedTime = java.time.LocalTime.now();
    var actualDate = responseBody.getString("date");
    var actualTime = java.time.LocalTime.parse(responseBody.getString("time"));

    assertEquals(expectedDate, actualDate);
    // Allow up to 60 seconds difference for time comparison
    var timeDiff = java.time.Duration.between(expectedTime, actualTime).abs().getSeconds();
    assertEquals(true, timeDiff <= 60, "Time difference should be within 60 seconds");
  }

  /**
   * <b>Given</b> a valid lend loan creation request<br>
   * <b>When</b> the POST /loans endpoint is called<br>
   * <b>Then</b> a new loan is created and returned with a 200 status code
   */
  @Test
  public void shouldCreateLendLoanSuccessfully() throws Exception {
    var body = """
        {
          "type": "LEND",
          "partyName": "John Doe",
          "title": "Loan for Car",
          "description": "Lending money to buy a car",
          "amount": 500000000,
          "currency": "IDR",
          "interestRate": 5.5,
          "date": "2025-12-31",
          "time": "23:59:59"
        }
        """;

    var response = Unirest.post(baseUrl + "/v1/loans")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(200, response.getStatus());
    var responseBody = new JSONObject(response.getBody());
    assertNotNull(responseBody.getString("id"));
    assertEquals(USER_ID, responseBody.getString("user"));
    assertEquals("LEND", responseBody.getString("type"));
    assertEquals("John Doe", responseBody.getString("partyName"));
    assertEquals("Loan for Car", responseBody.getString("title"));
    assertEquals("Lending money to buy a car", responseBody.getString("description"));
    assertEquals(500000000, responseBody.getLong("amount"));
    assertEquals(500000000, responseBody.getLong("remainingAmount"));
    assertEquals("IDR", responseBody.getString("currency"));
    assertEquals(5.5, responseBody.getDouble("interestRate"));
  }

  /**
   * <b>Given</b> a loan creation request with invalid type<br>
   * <b>When</b> the POST /loans endpoint is called<br>
   * <b>Then</b> the request should fail with a 400 status code
   */
  @Test
  public void shouldFailOnCreateLoanWithInvalidType() throws Exception {
    var body = """
        {
          "type": "INVALID_TYPE",
          "partyName": "John Doe",
          "title": "Loan for Car",
          "description": "Lending money to buy a car",
          "amount": 500000000,
          "currency": "IDR",
          "interestRate": 5.5,
          "date": "2025-12-31",
          "time": "23:59:59"
        }
        """;

    var response = Unirest.post(baseUrl + "/v1/loans")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(400, response.getStatus());
    assertEquals("type: invalid value", response.getBody());
  }

  /**
   * <b>Given</b> a loan creation request with missing type<br>
   * <b>When</b> the POST /loans endpoint is called<br>
   * <b>Then</b> the request should fail with a 400 status code
   */
  @Test
  public void shouldFailOnMissingType() throws Exception {
    var body = """
        {
          "partyName": "John Doe",
          "title": "Loan for Car",
          "description": "Lending money to buy a car",
          "amount": 500000000,
          "currency": "IDR",
          "interestRate": 5.5,
          "date": "2025-12-31",
          "time": "23:59:59"
        }
        """;

    var response = Unirest.post(baseUrl + "/v1/loans")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(400, response.getStatus());
    assertEquals("type: invalid value", response.getBody());
  }

  /**
   * <b>Given</b> a loan creation request with empty type<br>
   * <b>When</b> the POST /loans endpoint is called<br>
   * <b>Then</b> the request should fail with a 400 status code
   */
  @Test
  public void shouldFailOnEmptyType() throws Exception {
    var body = """
        {
          "type": "",
          "partyName": "John Doe",
          "title": "Loan for Car",
          "description": "Lending money to buy a car",
          "amount": 500000000,
          "currency": "IDR",
          "interestRate": 5.5,
          "date": "2025-12-31",
          "time": "23:59:59"
        }
        """;

    var response = Unirest.post(baseUrl + "/v1/loans")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(400, response.getStatus());
    assertEquals("type: invalid value", response.getBody());
  }

  /**
   * <b>Given</b> a loan creation request with blank type<br>
   * <b>When</b> the POST /loans endpoint is called<br>
   * <b>Then</b> the request should fail with a 400 status code
   */
  @Test
  public void shouldFailOnBlankType() throws Exception {
    var body = """
        {
          "type": "   ",
          "partyName": "John Doe",
          "title": "Loan for Car",
          "description": "Lending money to buy a car",
          "amount": 500000000,
          "currency": "IDR",
          "interestRate": 5.5,
          "date": "2025-12-31",
          "time": "23:59:59"
        }
        """;

    var response = Unirest.post(baseUrl + "/v1/loans")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(400, response.getStatus());
    assertEquals("type: invalid value", response.getBody());
  }

  /**
   * <b>Given</b> a loan creation request with null type<br>
   * <b>When</b> the POST /loans endpoint is called<br>
   * <b>Then</b> the request should fail with a 400 status code
   */
  @Test
  public void shouldFailOnNullType() throws Exception {
    var body = """
        {
          "type": null,
          "partyName": "John Doe",
          "title": "Loan for Car",
          "description": "Lending money to buy a car",
          "amount": 500000000,
          "currency": "IDR",
          "interestRate": 5.5,
          "date": "2025-12-31",
          "time": "23:59:59"
        }
        """;

    var response = Unirest.post(baseUrl + "/v1/loans")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(400, response.getStatus());
    assertEquals("type: invalid value", response.getBody());
  }

  /**
   * <b>Given</b> a loan creation request with missing title<br>
   * <b>When</b> the POST /loans endpoint is called<br>
   * <b>Then</b> the request should fail with a 400 status code
   */
  @Test
  public void shouldFailOnMissingTitle() throws Exception {
    var body = """
        {
          "type": "LEND",
          "partyName": "John Doe",
          "description": "Lending money to buy a car",
          "amount": 500000000,
          "currency": "IDR",
          "interestRate": 5.5,
          "date": "2025-12-31",
          "time": "23:59:59"
        }
        """;

    var response = Unirest.post(baseUrl + "/v1/loans")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(400, response.getStatus());
    assertEquals("title: invalid value", response.getBody());
  }

  /**
   * <b>Given</b> a loan creation request with null title<br>
   * <b>When</b> the POST /loans endpoint is called<br>
   * <b>Then</b> the request should fail with a 400 status code
   */
  @Test
  public void shouldFailOnNullTitle() throws Exception {
    var body = """
        {
          "type": "LEND",
          "partyName": "John Doe",
          "title": null,
          "description": "Lending money to buy a car",
          "amount": 500000000,
          "currency": "IDR",
          "interestRate": 5.5,
          "date": "2025-12-31",
          "time": "23:59:59"
        }
        """;

    var response = Unirest.post(baseUrl + "/v1/loans")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(400, response.getStatus());
    assertEquals("title: invalid value", response.getBody());
  }

  /**
   * <b>Given</b> a loan creation request with empty title<br>
   * <b>When</b> the POST /loans endpoint is called<br>
   * <b>Then</b> the request should fail with a 400 status code
   */
  @Test
  public void shouldFailOnEmptyTitle() throws Exception {
    var body = """
        {
          "type": "LEND",
          "partyName": "John Doe",
          "title": "",
          "description": "Lending money to buy a car",
          "amount": 500000000,
          "currency": "IDR",
          "interestRate": 5.5,
          "date": "2025-12-31",
          "time": "23:59:59"
        }
        """;

    var response = Unirest.post(baseUrl + "/v1/loans")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(400, response.getStatus());
    assertEquals("title: invalid value", response.getBody());
  }

  /**
   * <b>Given</b> a loan creation request with blank title<br>
   * <b>When</b> the POST /loans endpoint is called<br>
   * <b>Then</b> the request should fail with a 400 status code
   */
  @Test
  public void shouldFailOnBlankTitle() throws Exception {
    var body = """
        {
          "type": "LEND",
          "partyName": "John Doe",
          "title": "   ",
          "description": "Lending money to buy a car",
          "amount": 500000000,
          "currency": "IDR",
          "interestRate": 5.5,
          "date": "2025-12-31",
          "time": "23:59:59"
        }
        """;

    var response = Unirest.post(baseUrl + "/v1/loans")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(400, response.getStatus());
    assertEquals("title: invalid value", response.getBody());
  }

  /**
   * <b>Given</b> a loan creation request with negative amount<br>
   * <b>When</b> the POST /loans endpoint is called<br>
   * <b>Then</b> the request should fail with a 400 status code
   */
  @Test
  public void shouldFailOnNegativeAmount() throws Exception {
    var body = """
        {
          "type": "LEND",
          "partyName": "John Doe",
          "title": "Loan for Car",
          "description": "Lending money to buy a car",
          "amount": -500000000,
          "currency": "IDR",
          "interestRate": 5.5,
          "date": "2025-12-31",
          "time": "23:59:59"
        }
        """;

    var response = Unirest.post(baseUrl + "/v1/loans")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(400, response.getStatus());
    assertEquals("amount: invalid value", response.getBody());
  }

  /**
   * <b>Given</b> a loan creation request with zero amount<br>
   * <b>When</b> the POST /loans endpoint is called<br>
   * <b>Then</b> the request should fail with a 400 status code
   */
  @Test
  public void shouldFailOnZeroAmount() throws Exception {
    var body = """
        {
          "type": "LEND",
          "partyName": "John Doe",
          "title": "Loan for Car",
          "description": "Lending money to buy a car",
          "amount": 0,
          "currency": "IDR",
          "interestRate": 5.5,
          "date": "2025-12-31",
          "time": "23:59:59"
        }
        """;

    var response = Unirest.post(baseUrl + "/v1/loans")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(400, response.getStatus());
    assertEquals("amount: invalid value", response.getBody());
  }

  /**
   * <b>Given</b> a loan creation request with missing amount<br>
   * <b>When</b> the POST /loans endpoint is called<br>
   * <b>Then</b> the request should fail with a 400 status code
   */
  @Test
  public void shouldFailOnMissingAmount() throws Exception {
    var body = """
        {
          "type": "LEND",
          "partyName": "John Doe",
          "title": "Loan for Car",
          "description": "Lending money to buy a car",
          "currency": "IDR",
          "interestRate": 5.5,
          "date": "2025-12-31",
          "time": "23:59:59"
        }
        """;

    var response = Unirest.post(baseUrl + "/v1/loans")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(400, response.getStatus());
    assertEquals("amount: invalid value", response.getBody());
  }

  /**
   * <b>Given</b> a loan creation request with null amount<br>
   * <b>When</b> the POST /loans endpoint is called<br>
   * <b>Then</b> the request should fail with a 400 status code
   */
  @Test
  public void shouldFailOnNullAmount() throws Exception {
    var body = """
        {
          "type": "LEND",
          "partyName": "John Doe",
          "title": "Loan for Car",
          "description": "Lending money to buy a car",
          "amount": null,
          "currency": "IDR",
          "interestRate": 5.5,
          "date": "2025-12-31",
          "time": "23:59:59"
        }
        """;

    var response = Unirest.post(baseUrl + "/v1/loans")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(400, response.getStatus());
    assertEquals("amount: invalid value", response.getBody());
  }

  /**
   * <b>Given</b> a loan creation request with negative interest rate<br>
   * <b>When</b> the POST /loans endpoint is called<br>
   * <b>Then</b> the request should fail with a 400 status code
   */
  @Test
  public void shouldFailOnNegativeInterestRate() throws Exception {
    var body = """
        {
          "type": "LEND",
          "partyName": "John Doe",
          "title": "Loan for Car",
          "description": "Lending money to buy a car",
          "amount": 500000000,
          "currency": "IDR",
          "interestRate": -5.5,
          "date": "2025-12-31",
          "time": "23:59:59"
        }
        """;

    var response = Unirest.post(baseUrl + "/v1/loans")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(400, response.getStatus());
    assertEquals("interestRate: invalid value", response.getBody());
  }

  /**
   * <b>Given</b> a loan creation request with invalid date format<br>
   * <b>When</b> the POST /loans endpoint is called<br>
   * <b>Then</b> the request should fail with a 400 status code
   */
  @Test
  public void shouldFailOnInvalidDateFormat() throws Exception {
    var body = """
        {
          "type": "LEND",
          "partyName": "John Doe",
          "title": "Loan for Car",
          "description": "Lending money to buy a car",
          "amount": 500000000,
          "currency": "IDR",
          "interestRate": 5.5,
          "date": "2024-13-45",
          "time": "23:59:59"
        }
        """;

    var response = Unirest.post(baseUrl + "/v1/loans")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(400, response.getStatus());
    assertEquals("date: invalid value", response.getBody());
  }

  /**
   * <b>Given</b> a loan creation request with invalid date string<br>
   * <b>When</b> the POST /loans endpoint is called<br>
   * <b>Then</b> the request should fail with a 400 status code
   */
  @Test
  public void shouldFailOnInvalidDateString() throws Exception {
    var body = """
        {
          "type": "LEND",
          "partyName": "John Doe",
          "title": "Loan for Car",
          "description": "Lending money to buy a car",
          "amount": 500000000,
          "currency": "IDR",
          "interestRate": 5.5,
          "date": "invalid-date",
          "time": "23:59:59"
        }
        """;

    var response = Unirest.post(baseUrl + "/v1/loans")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(400, response.getStatus());
    assertEquals("date: invalid value", response.getBody());
  }

  /**
   * <b>Given</b> a loan creation request with invalid time format<br>
   * <b>When</b> the POST /loans endpoint is called<br>
   * <b>Then</b> the request should fail with a 400 status code
   */
  @Test
  public void shouldFailOnInvalidTimeFormat() throws Exception {
    var body = """
        {
          "type": "LEND",
          "partyName": "John Doe",
          "title": "Loan for Car",
          "description": "Lending money to buy a car",
          "amount": 500000000,
          "currency": "IDR",
          "interestRate": 5.5,
          "date": "2025-12-31",
          "time": "25:99:99"
        }
        """;

    var response = Unirest.post(baseUrl + "/v1/loans")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(400, response.getStatus());
    assertEquals("time: invalid value", response.getBody());
  }

  /**
   * <b>Given</b> a loan creation request with invalid time string<br>
   * <b>When</b> the POST /loans endpoint is called<br>
   * <b>Then</b> the request should fail with a 400 status code
   */
  @Test
  public void shouldFailOnInvalidTimeString() throws Exception {
    var body = """
        {
          "type": "LEND",
          "partyName": "John Doe",
          "title": "Loan for Car",
          "description": "Lending money to buy a car",
          "amount": 500000000,
          "currency": "IDR",
          "interestRate": 5.5,
          "date": "2025-12-31",
          "time": "invalid-time"
        }
        """;

    var response = Unirest.post(baseUrl + "/v1/loans")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(400, response.getStatus());
    assertEquals("time: invalid value", response.getBody());
  }

  /**
   * <b>Given</b> a loan creation request with both invalid date and time<br>
   * <b>When</b> the POST /loans endpoint is called<br>
   * <b>Then</b> the request should fail with a 400 status code
   */
  @Test
  public void shouldFailOnInvalidDateAndTime() throws Exception {
    var body = """
        {
          "type": "LEND",
          "partyName": "John Doe",
          "title": "Loan for Car",
          "description": "Lending money to buy a car",
          "amount": 500000000,
          "currency": "IDR",
          "interestRate": 5.5,
          "date": "not-a-date",
          "time": "not-a-time"
        }
        """;

    var response = Unirest.post(baseUrl + "/v1/loans")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(400, response.getStatus());
    assertTrue(response.getBody().contains("date: invalid value"));
    assertTrue(response.getBody().contains("time: invalid value"));
  }

  /**
   * <b>Given</b> a loan creation request without an account reference<br>
   * <b>When</b> the POST /loans endpoint is called<br>
   * <b>Then</b> the request should fail with a 400 status code
   */
  @Test
  public void createLoanWithoutAccountShouldFail() throws Exception {
    var body = """
        {
          "type": "BORROW",
          "partyName": "Bank XYZ",
          "title": "Personal Loan",
          "description": "Personal loan without account",
          "amount": 10000000,
          "currency": "IDR",
          "interestRate": 5.5,
          "account": ""
        }
        """;

    var response = Unirest.post(baseUrl + "/v1/loans")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(400, response.getStatus());
    assertEquals("account: invalid value", response.getBody());
  }

  /**
   * <b>Given</b> a loan creation request with a non-existent account<br>
   * <b>When</b> the POST /loans endpoint is called<br>
   * <b>Then</b> the request should fail with a 404 status code
   */
  @Test
  public void createLoanWithNonExistentAccountShouldFail() throws Exception {
    var beforeLoansResponse = Unirest.get(baseUrl + "/v1/users/" + USER_ID + "/loans")
        .header("Authorization", "Bearer " + token)
        .asString();

    assertEquals(200, beforeLoansResponse.getStatus());
    var beforeLoans = new JSONArray(beforeLoansResponse.getBody());

    var body = """
        {
          "type": "BORROW",
          "partyName": "Bank XYZ",
          "title": "Personal Loan",
          "description": "Loan with non-existent account",
          "amount": 10000000,
          "currency": "IDR",
          "interestRate": 5.5,
          "account": "non-existent-account-id"
        }
        """;

    var response = Unirest.post(baseUrl + "/v1/loans")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(404, response.getStatus());
    assertEquals("Account not found", response.getBody());

    var afterLoansResponse = Unirest.get(baseUrl + "/v1/users/" + USER_ID + "/loans")
        .header("Authorization", "Bearer " + token)
        .asString();

    assertEquals(200, afterLoansResponse.getStatus());
    var afterLoans = new JSONArray(afterLoansResponse.getBody());
    assertEquals(beforeLoans.length(), afterLoans.length());
  }

  /**
   * <b>Given</b> a valid loan creation request with an account<br>
   * <b>When</b> the POST /loans endpoint is called<br>
   * <b>Then</b> a disbursement CREDIT transaction should be automatically created
   */
  @Test
  public void createLoanShouldAutomaticallyCreateDisbursementTransaction() throws Exception {
    // First create an account
    var accountBody = """
        {
          "name": "Loan Disbursement Account",
          "type": "BANK",
          "themeColor": "#FF5733"
        }
        """;

    var accountResponse = Unirest.post(baseUrl + "/v1/accounts")
        .header("Authorization", "Bearer " + token)
        .body(accountBody)
        .asString();

    assertEquals(200, accountResponse.getStatus());
    var accountId = new JSONObject(accountResponse.getBody()).getString("id");

    // Create a loan linked to this account
    var loanBody = String.format("""
        {
          "type": "BORROW",
          "partyName": "Bank ABC",
          "title": "Car Loan",
          "description": "Loan for purchasing a car",
          "amount": 50000000,
          "currency": "IDR",
          "interestRate": 4.5,
          "account": "%s"
        }
        """, accountId);

    var loanResponse = Unirest.post(baseUrl + "/v1/loans")
        .header("Authorization", "Bearer " + token)
        .body(loanBody)
        .asString();

    assertEquals(200, loanResponse.getStatus());
    var loanResponseBody = new JSONObject(loanResponse.getBody());
    var loanId = loanResponseBody.getString("id");
    assertNotNull(loanId);

    // Verify the account balance increased (disbursement happened)
    var accountCheckResponse = Unirest.get(baseUrl + "/v1/accounts/" + accountId)
        .header("Authorization", "Bearer " + token)
        .asString();

    assertEquals(200, accountCheckResponse.getStatus());
    var accountData = new JSONObject(accountCheckResponse.getBody());
    assertEquals(0, accountData.getBigDecimal("balance").compareTo(new java.math.BigDecimal("50000000")));

    // Verify a CREDIT transaction was created for disbursement
    var transactionsResponse = Unirest.get(baseUrl + "/v1/users/" + USER_ID + "/transactions")
        .header("Authorization", "Bearer " + token)
        .asString();

    assertEquals(200, transactionsResponse.getStatus());
    var transactions = new org.json.JSONArray(transactionsResponse.getBody());

    boolean foundDisbursementTransaction = false;
    for (int i = 0; i < transactions.length(); i++) {
      var transaction = transactions.getJSONObject(i);
      if (transaction.has("loan") && loanId.equals(transaction.getString("loan"))) {
        foundDisbursementTransaction = true;
        assertEquals("CREDIT", transaction.getString("type"));
        assertEquals(accountId, transaction.getString("destination"));
        assertEquals(50000000, transaction.getLong("amount"));
        assertTrue(transaction.getString("title").toLowerCase().contains("disbursement")
            || transaction.getString("title").toLowerCase().contains("loan"));
        break;
      }
    }

    assertTrue(foundDisbursementTransaction, "A disbursement transaction should be automatically created");
  }

  /**
   * <b>Given</b> a LEND loan creation request with an account<br>
   * <b>When</b> the POST /loans endpoint is called<br>
   * <b>Then</b> a disbursement DEBIT transaction should be automatically created
   */
  @Test
  public void createLendLoanShouldAutomaticallyCreateDebitDisbursementTransaction() throws Exception {
    // First create an account with initial balance
    var accountBody = """
        {
          "name": "Lending Account",
          "type": "BANK",
          "themeColor": "#3357FF"
        }
        """;

    var accountResponse = Unirest.post(baseUrl + "/v1/accounts")
        .header("Authorization", "Bearer " + token)
        .body(accountBody)
        .asString();

    assertEquals(200, accountResponse.getStatus());
    var accountId = new JSONObject(accountResponse.getBody()).getString("id");

    // Add initial balance to the account
    var depositBody = String.format("""
        {
          "type": "CREDIT",
          "title": "Initial Balance",
          "description": "Initial balance setup",
          "destination": "%s",
          "amount": 100000000,
          "currency": "IDR"
        }
        """, accountId);

    var depositResponse = Unirest.post(baseUrl + "/v1/transactions")
        .header("Authorization", "Bearer " + token)
        .body(depositBody)
        .asString();

    assertEquals(200, depositResponse.getStatus());

    // Create a LEND loan linked to this account
    var loanBody = String.format("""
        {
          "type": "LEND",
          "partyName": "Friend John",
          "title": "Personal Lend",
          "description": "Lending money to a friend",
          "amount": 20000000,
          "currency": "IDR",
          "interestRate": 2.0,
          "account": "%s"
        }
        """, accountId);

    var loanResponse = Unirest.post(baseUrl + "/v1/loans")
        .header("Authorization", "Bearer " + token)
        .body(loanBody)
        .asString();

    assertEquals(200, loanResponse.getStatus());
    var loanResponseBody = new JSONObject(loanResponse.getBody());
    var loanId = loanResponseBody.getString("id");
    assertNotNull(loanId);

    // Verify the account balance decreased (disbursement happened)
    var accountCheckResponse = Unirest.get(baseUrl + "/v1/accounts/" + accountId)
        .header("Authorization", "Bearer " + token)
        .asString();

    assertEquals(200, accountCheckResponse.getStatus());
    var accountData = new JSONObject(accountCheckResponse.getBody());
    // Balance should be 100000000 - 20000000 = 80000000
    assertEquals(0, accountData.getBigDecimal("balance").compareTo(new java.math.BigDecimal("80000000")));

    // Verify a DEBIT transaction was created for disbursement
    var transactionsResponse = Unirest.get(baseUrl + "/v1/users/" + USER_ID + "/transactions")
        .header("Authorization", "Bearer " + token)
        .asString();

    assertEquals(200, transactionsResponse.getStatus());
    var transactions = new org.json.JSONArray(transactionsResponse.getBody());

    boolean foundDisbursementTransaction = false;
    for (int i = 0; i < transactions.length(); i++) {
      var transaction = transactions.getJSONObject(i);
      if (transaction.has("loan") && loanId.equals(transaction.getString("loan"))) {
        foundDisbursementTransaction = true;
        assertEquals("DEBIT", transaction.getString("type"));
        assertEquals(accountId, transaction.getString("source"));
        assertEquals(20000000, transaction.getLong("amount"));
        assertTrue(transaction.getString("title").toLowerCase().contains("disbursement")
            || transaction.getString("title").toLowerCase().contains("lend"));
        break;
      }
    }

    assertTrue(foundDisbursementTransaction, "A disbursement transaction should be automatically created for LEND");
  }

  /**
   * <b>Given</b> a loan creation request with an account from another user<br>
   * <b>When</b> the POST /loans endpoint is called<br>
   * <b>Then</b> the request should fail with a 403 or 404 status code
   */
  @Test
  public void createLoanWithAnotherUserAccountShouldFail() throws Exception {
    // Create account with different user
    var otherUserToken = SecureTestUtil.generateToken("otheruser@email.com");

    var accountBody = """
        {
          "name": "Other User Account",
          "type": "BANK",
          "themeColor": "#FF5733"
        }
        """;

    var accountResponse = Unirest.post(baseUrl + "/v1/accounts")
        .header("Authorization", "Bearer " + otherUserToken)
        .body(accountBody)
        .asString();

    assertEquals(200, accountResponse.getStatus());
    var accountId = new JSONObject(accountResponse.getBody()).getString("id");

    // Try to create loan with this account using original user
    var loanBody = String.format("""
        {
          "type": "BORROW",
          "partyName": "Bank ABC",
          "title": "Car Loan",
          "description": "Loan for purchasing a car",
          "amount": 50000000,
          "currency": "IDR",
          "interestRate": 4.5,
          "account": "%s"
        }
        """, accountId);

    var loanResponse = Unirest.post(baseUrl + "/v1/loans")
        .header("Authorization", "Bearer " + token)
        .body(loanBody)
        .asString();

    // Should fail with 403 (Forbidden) or 404 (Not Found)
    assertTrue(loanResponse.getStatus() == 403 || loanResponse.getStatus() == 404);
  }

  /**
   * <b>Given</b> multiple loans are created with the same account<br>
   * <b>When</b> checking the account balance<br>
   * <b>Then</b> all disbursements should be reflected in the balance
   */
  @Test
  public void multipleLoansToSameAccountShouldAccumulateDisbursements() throws Exception {
    // Create an account
    var accountBody = """
        {
          "name": "Multiple Loans Account",
          "type": "BANK",
          "themeColor": "#FF5733"
        }
        """;

    var accountResponse = Unirest.post(baseUrl + "/v1/accounts")
        .header("Authorization", "Bearer " + token)
        .body(accountBody)
        .asString();

    assertEquals(200, accountResponse.getStatus());
    var accountId = new JSONObject(accountResponse.getBody()).getString("id");

    // Create first loan
    var loan1Body = String.format("""
        {
          "type": "BORROW",
          "partyName": "Bank A",
          "title": "Loan 1",
          "description": "First loan",
          "amount": 10000000,
          "currency": "IDR",
          "interestRate": 4.0,
          "account": "%s"
        }
        """, accountId);

    var loan1Response = Unirest.post(baseUrl + "/v1/loans")
        .header("Authorization", "Bearer " + token)
        .body(loan1Body)
        .asString();

    assertEquals(200, loan1Response.getStatus());

    // Create second loan
    var loan2Body = String.format("""
        {
          "type": "BORROW",
          "partyName": "Bank B",
          "title": "Loan 2",
          "description": "Second loan",
          "amount": 15000000,
          "currency": "IDR",
          "interestRate": 5.0,
          "account": "%s"
        }
        """, accountId);

    var loan2Response = Unirest.post(baseUrl + "/v1/loans")
        .header("Authorization", "Bearer " + token)
        .body(loan2Body)
        .asString();

    assertEquals(200, loan2Response.getStatus());

    // Verify the account balance reflects both disbursements
    var accountCheckResponse = Unirest.get(baseUrl + "/v1/accounts/" + accountId)
        .header("Authorization", "Bearer " + token)
        .asString();

    assertEquals(200, accountCheckResponse.getStatus());
    var accountData = new JSONObject(accountCheckResponse.getBody());
    // Balance should be 10000000 + 15000000 = 25000000
    assertEquals(0, accountData.getBigDecimal("balance").compareTo(new java.math.BigDecimal("25000000")));
  }
}
