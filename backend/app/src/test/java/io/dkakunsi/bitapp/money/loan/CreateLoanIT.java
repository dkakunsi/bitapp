package io.dkakunsi.bitapp.money.loan;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;

import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.dkakunsi.bitapp.jwt.JWTAuthorizer;
import io.dkakunsi.bitapp.money.AppLauncher;
import io.dkakunsi.bitapp.test.AppTestUtil;
import io.dkakunsi.bitapp.test.SecureTestUtil;
import kong.unirest.Unirest;

public class CreateLoanIT extends AppTestUtil {

  private static final int port = 20007;

  private static CreateLoanIT sut = new CreateLoanIT();

  private static String baseUrl;

  private static String token;

  @BeforeAll
  static void setup() throws Exception {
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
  public void shouldFailOnMissingAuthorization() throws Exception {
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

    var response = Unirest.post(baseUrl + "/loans")
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
  public void shouldCreateBorrowLoanSuccessfully() throws Exception {
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

    var response = Unirest.post(baseUrl + "/loans")
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
    assertEquals("14:30:00", responseBody.getString("time"));
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

    var response = Unirest.post(baseUrl + "/loans")
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
    assertEquals("14:30:00", responseBody.getString("time"));
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

    var response = Unirest.post(baseUrl + "/loans")
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
          "interestRate": 5.5
        }
        """;

    var response = Unirest.post(baseUrl + "/loans")
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
          "interestRate": 5.5
        }
        """;

    var response = Unirest.post(baseUrl + "/loans")
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
          "type": null,
          "partyName": "John Doe",
          "title": "Loan for Car",
          "description": "Lending money to buy a car",
          "amount": 500000000,
          "currency": "IDR",
          "interestRate": 5.5
        }
        """;

    var response = Unirest.post(baseUrl + "/loans")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(400, response.getStatus());
    assertEquals("type: must not be blank", response.getBody());
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
          "interestRate": 5.5
        }
        """;

    var response = Unirest.post(baseUrl + "/loans")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(400, response.getStatus());
    assertEquals("type: must not be blank", response.getBody());
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
          "interestRate": 5.5
        }
        """;

    var response = Unirest.post(baseUrl + "/loans")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(400, response.getStatus());
    assertEquals("type: must not be blank", response.getBody());
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
          "partyName": "John Doe",
          "title": "Loan for Car",
          "description": "Lending money to buy a car",
          "amount": 500000000,
          "currency": "IDR",
          "interestRate": 5.5
        }
        """;

    var response = Unirest.post(baseUrl + "/loans")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(400, response.getStatus());
    assertEquals("type: must not be blank", response.getBody());
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
          "interestRate": 5.5
        }
        """;

    var response = Unirest.post(baseUrl + "/loans")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(400, response.getStatus());
    assertEquals("title: must not be blank", response.getBody());
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
          "interestRate": 5.5
        }
        """;

    var response = Unirest.post(baseUrl + "/loans")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(400, response.getStatus());
    assertEquals("title: must not be blank", response.getBody());
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
          "interestRate": 5.5
        }
        """;

    var response = Unirest.post(baseUrl + "/loans")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(400, response.getStatus());
    assertEquals("title: must not be blank", response.getBody());
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
          "interestRate": 5.5
        }
        """;

    var response = Unirest.post(baseUrl + "/loans")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(400, response.getStatus());
    assertEquals("title: must not be blank", response.getBody());
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
          "interestRate": 5.5
        }
        """;

    var response = Unirest.post(baseUrl + "/loans")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(400, response.getStatus());
    assertEquals("amount: must be greater than 0", response.getBody());
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
          "interestRate": 5.5
        }
        """;

    var response = Unirest.post(baseUrl + "/loans")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(400, response.getStatus());
    assertEquals("amount: must be greater than 0", response.getBody());
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
          "interestRate": 5.5
        }
        """;

    var response = Unirest.post(baseUrl + "/loans")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(400, response.getStatus());
    assertEquals("amount: must not be null", response.getBody());
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
          "interestRate": 5.5
        }
        """;

    var response = Unirest.post(baseUrl + "/loans")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(400, response.getStatus());
    assertEquals("amount: must not be null", response.getBody());
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
          "interestRate": -5.5
        }
        """;

    var response = Unirest.post(baseUrl + "/loans")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(400, response.getStatus());
    assertEquals("interestRate: must be greater than or equal to 0", response.getBody());
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
          "date": "2024-13-45"
        }
        """;

    var response = Unirest.post(baseUrl + "/loans")
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
          "date": "invalid-date"
        }
        """;

    var response = Unirest.post(baseUrl + "/loans")
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
          "time": "25:99:99"
        }
        """;

    var response = Unirest.post(baseUrl + "/loans")
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
          "time": "invalid-time"
        }
        """;

    var response = Unirest.post(baseUrl + "/loans")
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

    var response = Unirest.post(baseUrl + "/loans")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(400, response.getStatus());
    assertTrue(response.getBody().contains("date: invalid value"));
    assertTrue(response.getBody().contains("time: invalid value"));
  }
}
