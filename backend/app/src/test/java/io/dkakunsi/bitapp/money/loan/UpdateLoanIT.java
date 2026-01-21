package io.dkakunsi.bitapp.money.loan;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

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

public class UpdateLoanIT extends AppTestUtil {

  private static final int port = 20009;

  private static UpdateLoanIT sut = new UpdateLoanIT();

  private static String baseUrl;

  private static String token;

  private static String testLoanId;

  @BeforeAll
  static void setup() throws Exception {
    var appEnv = Map.of(APP_PORT, Integer.toString(port),
        JWTAuthorizer.JWT_PUBLIC_KEY, SecureTestUtil.PUBLIC_KEY);
    sut.create(appEnv);
    sut.startServer(new AppLauncher());

    baseUrl = "http://localhost:" + port;
    token = SecureTestUtil.generateToken(USER_ID);

    // Create a test loan to update
    testLoanId = createTestLoan();
  }

  @AfterAll
  static void tearDown() throws Exception {
    sut.destroy();
  }

  /**
   * Helper method to create a test loan for update operations
   */
  private static String createTestLoan() throws Exception {
    var body = """
        {
          "type": "BORROW",
          "partyName": "John Doe",
          "date": "2024-06-15",
          "time": "14:30:00",
          "title": "Original Loan",
          "description": "Original description",
          "amount": 500000000,
          "currency": "IDR",
          "interestRate": 5.5
        }
        """;

    var response = Unirest.post(baseUrl + "/loans")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    var responseBody = new JSONObject(response.getBody());
    return responseBody.getString("id");
  }

  /**
   * <b>Given</b> a loan update request without authorization header<br>
   * <b>When</b> the PUT /loans/{id} endpoint is called<br>
   * <b>Then</b> the request should fail with a 401 status code
   */
  @Test
  public void updateLoanWithoutAuthorizationHeaderShouldReturn401() throws Exception {
    var body = """
        {
          "partyName": "Jane Smith",
          "title": "Updated Loan",
          "description": "Updated description"
        }
        """;

    var response = Unirest.put(baseUrl + "/loans/" + testLoanId)
        .body(body)
        .asString();

    assertEquals(401, response.getStatus());
  }

  /**
   * <b>Given</b> a valid loan update request with new party name<br>
   * <b>When</b> the PUT /loans/{id} endpoint is called<br>
   * <b>Then</b> the loan is updated and returned with a 200 status code
   */
  @Test
  public void updateLoanPartyNameShouldBeOk() throws Exception {
    var loanId = createTestLoan();
    var body = """
        {
          "partyName": "Jane Smith"
        }
        """;

    var response = Unirest.put(baseUrl + "/loans/" + loanId)
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(200, response.getStatus());
    var responseBody = new JSONObject(response.getBody());
    assertEquals(loanId, responseBody.getString("id"));
    assertEquals(USER_ID, responseBody.getString("user"));
    assertEquals("Jane Smith", responseBody.getString("partyName"));
    assertEquals(500000000, responseBody.getLong("remainingAmount"));
  }

  /**
   * <b>Given</b> a valid loan update request with new title<br>
   * <b>When</b> the PUT /loans/{id} endpoint is called<br>
   * <b>Then</b> the loan title is updated successfully
   */
  @Test
  public void updateLoanTitleShouldBeOk() throws Exception {
    var loanId = createTestLoan();
    var body = """
        {
          "title": "New Loan Title"
        }
        """;

    var response = Unirest.put(baseUrl + "/loans/" + loanId)
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(200, response.getStatus());
    var responseBody = new JSONObject(response.getBody());
    assertEquals(loanId, responseBody.getString("id"));
    assertEquals("New Loan Title", responseBody.getString("title"));
  }

  /**
   * <b>Given</b> a valid loan update request with new description<br>
   * <b>When</b> the PUT /loans/{id} endpoint is called<br>
   * <b>Then</b> the loan description is updated successfully
   */
  @Test
  public void updateLoanDescriptionShouldBeOk() throws Exception {
    var loanId = createTestLoan();
    var body = """
        {
          "description": "This is a completely new description"
        }
        """;

    var response = Unirest.put(baseUrl + "/loans/" + loanId)
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(200, response.getStatus());
    var responseBody = new JSONObject(response.getBody());
    assertEquals(loanId, responseBody.getString("id"));
    assertEquals("This is a completely new description", responseBody.getString("description"));
  }

  /**
   * <b>Given</b> a valid loan update request with new amount<br>
   * <b>When</b> the PUT /loans/{id} endpoint is called<br>
   * <b>Then</b> the loan amount is updated successfully
   */
  @Test
  public void updateLoanAmountShouldBeOk() throws Exception {
    var loanId = createTestLoan();
    var body = """
        {
          "amount": 750000000
        }
        """;

    var response = Unirest.put(baseUrl + "/loans/" + loanId)
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(200, response.getStatus());
    var responseBody = new JSONObject(response.getBody());
    assertEquals(loanId, responseBody.getString("id"));
    assertEquals(750000000, responseBody.getLong("amount"));
    assertEquals(750000000, responseBody.getLong("remainingAmount"));
  }

  /**
   * <b>Given</b> a valid loan update request with new interest rate<br>
   * <b>When</b> the PUT /loans/{id} endpoint is called<br>
   * <b>Then</b> the loan interest rate is updated successfully
   */
  @Test
  public void updateLoanInterestRateShouldBeOk() throws Exception {
    var loanId = createTestLoan();
    var body = """
        {
          "interestRate": 7.5
        }
        """;

    var response = Unirest.put(baseUrl + "/loans/" + loanId)
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(200, response.getStatus());
    var responseBody = new JSONObject(response.getBody());
    assertEquals(loanId, responseBody.getString("id"));
    assertEquals(7.5, responseBody.getDouble("interestRate"));
  }

  /**
   * <b>Given</b> a valid loan update request with new currency<br>
   * <b>When</b> the PUT /loans/{id} endpoint is called<br>
   * <b>Then</b> the loan currency is updated successfully
   */
  @Test
  public void updateLoanCurrencyShouldBeOk() throws Exception {
    var loanId = createTestLoan();
    var body = """
        {
          "currency": "USD"
        }
        """;

    var response = Unirest.put(baseUrl + "/loans/" + loanId)
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(200, response.getStatus());
    var responseBody = new JSONObject(response.getBody());
    assertEquals(loanId, responseBody.getString("id"));
    assertEquals("USD", responseBody.getString("currency"));
  }

  /**
   * <b>Given</b> a valid loan update request with new date and time<br>
   * <b>When</b> the PUT /loans/{id} endpoint is called<br>
   * <b>Then</b> the loan date and time are updated successfully
   */
  @Test
  public void updateLoanDateAndTimeShouldBeOk() throws Exception {
    var loanId = createTestLoan();
    var body = """
        {
          "date": "2025-12-31",
          "time": "23:59:59"
        }
        """;

    var response = Unirest.put(baseUrl + "/loans/" + loanId)
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(200, response.getStatus());
    var responseBody = new JSONObject(response.getBody());
    assertEquals(loanId, responseBody.getString("id"));
    assertEquals("2025-12-31", responseBody.getString("date"));
    assertEquals("23:59", responseBody.getString("time"));
  }

  /**
   * <b>Given</b> a loan update request with multiple fields<br>
   * <b>When</b> the PUT /loans/{id} endpoint is called<br>
   * <b>Then</b> all fields are updated successfully
   */
  @Test
  public void updateMultipleLoanFieldsShouldBeOk() throws Exception {
    var loanId = createTestLoan();
    var body = """
        {
          "partyName": "Jane Smith",
          "title": "Completely Updated Loan",
          "description": "All new description for updated loan",
          "amount": 1000000000,
          "currency": "USD",
          "interestRate": 3.5,
          "date": "2026-01-15",
          "time": "10:00:00"
        }
        """;

    var response = Unirest.put(baseUrl + "/loans/" + loanId)
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(200, response.getStatus());
    var responseBody = new JSONObject(response.getBody());
    assertEquals(loanId, responseBody.getString("id"));
    assertEquals(USER_ID, responseBody.getString("user"));
    assertEquals("Jane Smith", responseBody.getString("partyName"));
    assertEquals("Completely Updated Loan", responseBody.getString("title"));
    assertEquals("All new description for updated loan", responseBody.getString("description"));
    assertEquals(1000000000, responseBody.getLong("amount"));
    assertEquals("USD", responseBody.getString("currency"));
    assertEquals(3.5, responseBody.getDouble("interestRate"));
    assertEquals("2026-01-15", responseBody.getString("date"));
    assertEquals("10:00", responseBody.getString("time"));
  }

  /**
   * <b>Given</b> a loan update request with empty title<br>
   * <b>When</b> the PUT /loans/{id} endpoint is called<br>
   * <b>Then</b> the request should fail with a 400 status code
   */
  @Test
  public void shouldFailOnEmptyTitle() throws Exception {
    var loanId = createTestLoan();
    var body = """
        {
          "partyName": "John Doe",
          "title": "",
          "description": "Original description"
        }
        """;

    var response = Unirest.put(baseUrl + "/loans/" + loanId)
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(400, response.getStatus());
    assertEquals("title: invalid value", response.getBody());
  }

  /**
   * <b>Given</b> a loan update request with blank title<br>
   * <b>When</b> the PUT /loans/{id} endpoint is called<br>
   * <b>Then</b> the request should fail with a 400 status code
   */
  @Test
  public void shouldFailOnBlankTitle() throws Exception {
    var loanId = createTestLoan();
    var body = """
        {
          "partyName": "John Doe",
          "title": "   ",
          "description": "Original description"
        }
        """;

    var response = Unirest.put(baseUrl + "/loans/" + loanId)
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(400, response.getStatus());
    assertEquals("title: invalid value", response.getBody());
  }

  /**
   * <b>Given</b> a loan update request with missing title<br>
   * <b>When</b> the PUT /loans/{id} endpoint is called<br>
   * <b>Then</b> the request should succeed with a 200 status code (partial update allowed)
   */
  @Test
  public void shouldSucceedOnMissingTitle() throws Exception {
    var loanId = createTestLoan();
    var body = """
        {
          "partyName": "John Doe Updated",
          "description": "Updated description"
        }
        """;

    var response = Unirest.put(baseUrl + "/loans/" + loanId)
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(200, response.getStatus());
    var responseBody = new JSONObject(response.getBody());
    assertEquals("John Doe Updated", responseBody.getString("partyName"));
    assertEquals("Updated description", responseBody.getString("description"));
    assertEquals("Original Loan", responseBody.getString("title")); // title should remain unchanged
  }

  /**
   * <b>Given</b> a loan update request with negative amount<br>
   * <b>When</b> the PUT /loans/{id} endpoint is called<br>
   * <b>Then</b> the request should fail with a 400 status code
   */
  @Test
  public void shouldFailOnNegativeAmount() throws Exception {
    var loanId = createTestLoan();
    var body = """
        {
          "amount": -100000
        }
        """;

    var response = Unirest.put(baseUrl + "/loans/" + loanId)
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(400, response.getStatus());
    assertEquals("amount: invalid value", response.getBody());
  }

  /**
   * <b>Given</b> a loan update request with zero amount<br>
   * <b>When</b> the PUT /loans/{id} endpoint is called<br>
   * <b>Then</b> the request should fail with a 400 status code
   */
  @Test
  public void shouldFailOnZeroAmount() throws Exception {
    var loanId = createTestLoan();
    var body = """
        {
          "amount": 0
        }
        """;

    var response = Unirest.put(baseUrl + "/loans/" + loanId)
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(400, response.getStatus());
    assertEquals("amount: invalid value", response.getBody());
  }

  /**
   * <b>Given</b> a loan update request with negative interest rate<br>
   * <b>When</b> the PUT /loans/{id} endpoint is called<br>
   * <b>Then</b> the request should fail with a 400 status code
   */
  @Test
  public void shouldFailOnNegativeInterestRate() throws Exception {
    var loanId = createTestLoan();
    var body = """
        {
          "interestRate": -5.5
        }
        """;

    var response = Unirest.put(baseUrl + "/loans/" + loanId)
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(400, response.getStatus());
    assertEquals("interestRate: invalid value", response.getBody());
  }

  /**
   * <b>Given</b> a loan update request with invalid date format<br>
   * <b>When</b> the PUT /loans/{id} endpoint is called<br>
   * <b>Then</b> the request should fail with a 400 status code
   */
  @Test
  public void shouldFailOnInvalidDateFormat() throws Exception {
    var loanId = createTestLoan();
    var body = """
        {
          "date": "2024-13-45"
        }
        """;

    var response = Unirest.put(baseUrl + "/loans/" + loanId)
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(400, response.getStatus());
    assertEquals("date: invalid value", response.getBody());
  }

  /**
   * <b>Given</b> a loan update request with invalid date string<br>
   * <b>When</b> the PUT /loans/{id} endpoint is called<br>
   * <b>Then</b> the request should fail with a 400 status code
   */
  @Test
  public void shouldFailOnInvalidDateString() throws Exception {
    var loanId = createTestLoan();
    var body = """
        {
          "date": "invalid-date"
        }
        """;

    var response = Unirest.put(baseUrl + "/loans/" + loanId)
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(400, response.getStatus());
    assertEquals("date: invalid value", response.getBody());
  }

  /**
   * <b>Given</b> a loan update request with invalid time format<br>
   * <b>When</b> the PUT /loans/{id} endpoint is called<br>
   * <b>Then</b> the request should fail with a 400 status code
   */
  @Test
  public void shouldFailOnInvalidTimeFormat() throws Exception {
    var loanId = createTestLoan();
    var body = """
        {
          "time": "25:99:99"
        }
        """;

    var response = Unirest.put(baseUrl + "/loans/" + loanId)
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(400, response.getStatus());
    assertEquals("time: invalid value", response.getBody());
  }

  /**
   * <b>Given</b> a loan update request with invalid time string<br>
   * <b>When</b> the PUT /loans/{id} endpoint is called<br>
   * <b>Then</b> the request should fail with a 400 status code
   */
  @Test
  public void shouldFailOnInvalidTimeString() throws Exception {
    var loanId = createTestLoan();
    var body = """
        {
          "time": "invalid-time"
        }
        """;

    var response = Unirest.put(baseUrl + "/loans/" + loanId)
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(400, response.getStatus());
    assertEquals("time: invalid value", response.getBody());
  }

  /**
   * <b>Given</b> a loan update request with both invalid date and time<br>
   * <b>When</b> the PUT /loans/{id} endpoint is called<br>
   * <b>Then</b> the request should fail with a 400 status code
   */
  @Test
  public void shouldFailOnInvalidDateAndTime() throws Exception {
    var loanId = createTestLoan();
    var body = """
        {
          "date": "not-a-date",
          "time": "not-a-time"
        }
        """;

    var response = Unirest.put(baseUrl + "/loans/" + loanId)
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(400, response.getStatus());
    assertTrue(response.getBody().contains("date: invalid value"));
    assertTrue(response.getBody().contains("time: invalid value"));
  }

  /**
   * <b>Given</b> a loan update request with non-existent loan ID<br>
   * <b>When</b> the PUT /loans/{id} endpoint is called<br>
   * <b>Then</b> the request should fail with a 404 status code
   */
  @Test
  public void shouldFailOnNonExistentLoanId() throws Exception {
    var body = """
        {
          "partyName": "John Doe",
          "title": "Original Loan",
          "description": "Original description"
        }
        """;

    var response = Unirest.put(baseUrl + "/loans/non-existent-id")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(404, response.getStatus());
  }

  /**
   * <b>Given</b> a loan owned by another user<br>
   * <b>When</b> the PUT /loans/{id} endpoint is called by a different user<br>
   * <b>Then</b> the request should fail with a 403 status code
   */
  @Test
  public void shouldFailOnUpdateLoanOwnedByAnotherUser() throws Exception {
    var loanId = createTestLoan();
    var anotherUserToken = SecureTestUtil.generateToken("another-user@test.com");

    var body = """
        {
          "partyName": "John Doe",
          "title": "Trying to update someone else's loan",
          "description": "This should fail"
        }
        """;

    var response = Unirest.put(baseUrl + "/loans/" + loanId)
        .header("Authorization", "Bearer " + anotherUserToken)
        .body(body)
        .asString();

    assertEquals(403, response.getStatus());
  }

  // TODO: when updating amount on a partially repaid loan, should adjust
  // remainingAmount

  // TODO: when updating amount to a value less than (original amount - repaid
  // amount), should set remainingAmount to negative value
}
