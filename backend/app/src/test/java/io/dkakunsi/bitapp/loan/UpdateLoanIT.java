package io.dkakunsi.bitapp.loan;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.dkakunsi.bitapp.AppLauncher;
import io.dkakunsi.bitapp.jwt.JWTAuthorizer;
import io.dkakunsi.bitapp.test.AppTestUtil;
import io.dkakunsi.bitapp.test.SecureTestUtil;
import kong.unirest.Unirest;

public class UpdateLoanIT extends AppTestUtil {
  private static final int port = 20009;

  private static UpdateLoanIT sut = new UpdateLoanIT();

  private static String baseUrl;

  private static String token;

  private String loanId;

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

  @BeforeEach
  public void setupEach() throws Exception {
    var testAccountId = createTestAccount();
    loanId = createTestLoan(testAccountId);
  }

  private static String createTestAccount() throws Exception {
    // Create source account for transaction
    var accountBody = """
        {
          "name": "Repayment Account",
          "type": "BANK",
          "themeColor": "#0000FF"
        }
        """;

    var accountResponse = Unirest.post(baseUrl + "/v1/accounts")
        .header("Authorization", "Bearer " + token)
        .body(accountBody)
        .asString();

    assertEquals(200, accountResponse.getStatus());
    var accountResponseBody = new JSONObject(accountResponse.getBody());
    var sourceAccountId = accountResponseBody.getString("id");
    return sourceAccountId;
  }

  private static String createTestLoan(String accountId) throws Exception {
    var body = """
        {
          "type": "BORROW",
          "partyName": "John Doe",
          "account": "%s",
          "date": "2024-06-15",
          "time": "14:30:00",
          "title": "Original Loan",
          "description": "Original description",
          "amount": 500000000,
          "currency": "IDR",
          "interestRate": 5.5
        }
        """.formatted(accountId);

    var response = Unirest.post(baseUrl + "/v1/loans")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(200, response.getStatus());
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

    var response = Unirest.put(baseUrl + "/v1/loans/" + loanId)
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
    var body = """
        {
          "partyName": "Jane Smith"
        }
        """;

    var response = Unirest.put(baseUrl + "/v1/loans/" + loanId)
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
    var body = """
        {
          "title": "New Loan Title"
        }
        """;

    var response = Unirest.put(baseUrl + "/v1/loans/" + loanId)
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
    var body = """
        {
          "description": "This is a completely new description"
        }
        """;

    var response = Unirest.put(baseUrl + "/v1/loans/" + loanId)
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
    var body = """
        {
          "amount": 750000000
        }
        """;

    var response = Unirest.put(baseUrl + "/v1/loans/" + loanId)
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
    var body = """
        {
          "interestRate": 7.5
        }
        """;

    var response = Unirest.put(baseUrl + "/v1/loans/" + loanId)
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
    var body = """
        {
          "currency": "USD"
        }
        """;

    var response = Unirest.put(baseUrl + "/v1/loans/" + loanId)
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
    var body = """
        {
          "date": "2025-12-31",
          "time": "23:59:59"
        }
        """;

    var response = Unirest.put(baseUrl + "/v1/loans/" + loanId)
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

    var response = Unirest.put(baseUrl + "/v1/loans/" + loanId)
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
    var body = """
        {
          "partyName": "John Doe",
          "title": "",
          "description": "Original description"
        }
        """;

    var response = Unirest.put(baseUrl + "/v1/loans/" + loanId)
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
    var body = """
        {
          "partyName": "John Doe",
          "title": "   ",
          "description": "Original description"
        }
        """;

    var response = Unirest.put(baseUrl + "/v1/loans/" + loanId)
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(400, response.getStatus());
    assertEquals("title: invalid value", response.getBody());
  }

  /**
   * <b>Given</b> a loan update request with missing title<br>
   * <b>When</b> the PUT /loans/{id} endpoint is called<br>
   * <b>Then</b> the request should succeed with a 200 status code (partial update
   * allowed)
   */
  @Test
  public void shouldSucceedOnMissingTitle() throws Exception {
    var body = """
        {
          "partyName": "John Doe Updated",
          "description": "Updated description"
        }
        """;

    var response = Unirest.put(baseUrl + "/v1/loans/" + loanId)
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
    var body = """
        {
          "amount": -100000
        }
        """;

    var response = Unirest.put(baseUrl + "/v1/loans/" + loanId)
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
    var body = """
        {
          "amount": 0
        }
        """;

    var response = Unirest.put(baseUrl + "/v1/loans/" + loanId)
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
    var body = """
        {
          "interestRate": -5.5
        }
        """;

    var response = Unirest.put(baseUrl + "/v1/loans/" + loanId)
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
    var body = """
        {
          "date": "2024-13-45"
        }
        """;

    var response = Unirest.put(baseUrl + "/v1/loans/" + loanId)
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
    var body = """
        {
          "date": "invalid-date"
        }
        """;

    var response = Unirest.put(baseUrl + "/v1/loans/" + loanId)
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
    var body = """
        {
          "time": "25:99:99"
        }
        """;

    var response = Unirest.put(baseUrl + "/v1/loans/" + loanId)
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
    var body = """
        {
          "time": "invalid-time"
        }
        """;

    var response = Unirest.put(baseUrl + "/v1/loans/" + loanId)
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
    var body = """
        {
          "date": "not-a-date",
          "time": "not-a-time"
        }
        """;

    var response = Unirest.put(baseUrl + "/v1/loans/" + loanId)
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

    var response = Unirest.put(baseUrl + "/v1/loans/non-existent-id")
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
    var anotherUserToken = SecureTestUtil.generateToken("another-user@test.com");

    var body = """
        {
          "partyName": "John Doe",
          "title": "Trying to update someone else's loan",
          "description": "This should fail"
        }
        """;

    var response = Unirest.put(baseUrl + "/v1/loans/" + loanId)
        .header("Authorization", "Bearer " + anotherUserToken)
        .body(body)
        .asString();

    assertEquals(403, response.getStatus());
  }

  /**
   * <b>Given</b> a partially repaid loan with original amount 500,000,000 and
   * repaid 200,000,000 (remaining 300,000,000)<br>
   * <b>When</b> the amount is updated to 800,000,000<br>
   * <b>Then</b> the remainingAmount should be adjusted to 600,000,000 (new amount
   * - repaid amount)
   */
  @Test
  public void updateAmountOnPartiallyRepaidLoanShouldAdjustRemainingAmount() throws Exception {
    // Create source account for transaction
    var accountBody = """
        {
          "name": "Repayment Account",
          "type": "BANK",
          "themeColor": "#0000FF"
        }
        """;

    var accountResponse = Unirest.post(baseUrl + "/v1/accounts")
        .header("Authorization", "Bearer " + token)
        .body(accountBody)
        .asString();

    assertEquals(200, accountResponse.getStatus());
    var accountResponseBody = new JSONObject(accountResponse.getBody());
    var sourceAccountId = accountResponseBody.getString("id");

    // Add initial balance to account via CREDIT transaction
    var creditBody = """
        {
          "type": "CREDIT",
          "title": "Initial Balance",
          "description": "Initial balance setup",
          "destination": "%s",
          "amount": 500000000,
          "currency": "IDR"
        }
        """.formatted(sourceAccountId);

    var creditResponse = Unirest.post(baseUrl + "/v1/transactions")
        .header("Authorization", "Bearer " + token)
        .body(creditBody)
        .asString();

    assertEquals(200, creditResponse.getStatus());

    // Make a partial payment of 200,000,000 (40% of 500,000,000) via transaction
    var transactionBody = """
        {
          "type": "DEBIT",
          "date": "2024-07-01",
          "time": "10:00:00",
          "title": "Partial Repayment",
          "description": "Partial loan repayment",
          "amount": 200000000,
          "currency": "IDR",
          "source": "%s",
          "loan": "%s",
          "category": "LOAN"
        }
        """.formatted(sourceAccountId, loanId);

    var transactionResponse = Unirest.post(baseUrl + "/v1/transactions")
        .header("Authorization", "Bearer " + token)
        .body(transactionBody)
        .asString();

    assertEquals(200, transactionResponse.getStatus());

    // Verify the loan has remaining amount of 300,000,000
    var getLoanResponse = Unirest.get(baseUrl + "/v1/loans/" + loanId)
        .header("Authorization", "Bearer " + token)
        .asString();

    var loanBeforeUpdate = new JSONObject(getLoanResponse.getBody());
    assertEquals(500000000, loanBeforeUpdate.getLong("amount"));
    assertEquals(300000000, loanBeforeUpdate.getLong("remainingAmount"));

    // Update the loan amount to 800,000,000
    var updateBody = """
        {
          "amount": 800000000
        }
        """;

    var updateResponse = Unirest.put(baseUrl + "/v1/loans/" + loanId)
        .header("Authorization", "Bearer " + token)
        .body(updateBody)
        .asString();

    assertEquals(200, updateResponse.getStatus());
    var updatedLoan = new JSONObject(updateResponse.getBody());
    assertEquals(800000000, updatedLoan.getLong("amount"));
    // remainingAmount should be: new amount - repaid amount = 800,000,000 -
    // 200,000,000 = 600,000,000
    assertEquals(600000000, updatedLoan.getLong("remainingAmount"));
  }

  /**
   * <b>Given</b> a partially repaid loan with original amount 500,000,000 and
   * repaid 400,000,000 (remaining 100,000,000)<br>
   * <b>When</b> the amount is updated to 300,000,000 (less than the repaid
   * amount)<br>
   * <b>Then</b> the remainingAmount should be set to -100,000,000 (indicating
   * overpayment)
   */
  @Test
  public void updateAmountBelowRepaidAmountShouldSetNegativeRemainingAmount() throws Exception {
    // Create source account for transaction
    var accountBody = """
        {
          "name": "Repayment Account",
          "type": "BANK",
          "themeColor": "#0000FF"
        }
        """;

    var accountResponse = Unirest.post(baseUrl + "/v1/accounts")
        .header("Authorization", "Bearer " + token)
        .body(accountBody)
        .asString();

    assertEquals(200, accountResponse.getStatus());
    var accountResponseBody = new JSONObject(accountResponse.getBody());
    var sourceAccountId = accountResponseBody.getString("id");

    // Add initial balance to account via CREDIT transaction
    var creditBody = """
        {
          "type": "CREDIT",
          "title": "Initial Balance",
          "description": "Initial balance setup",
          "destination": "%s",
          "amount": 500000000,
          "currency": "IDR"
        }
        """.formatted(sourceAccountId);

    var creditResponse = Unirest.post(baseUrl + "/v1/transactions")
        .header("Authorization", "Bearer " + token)
        .body(creditBody)
        .asString();

    assertEquals(200, creditResponse.getStatus());

    // Make a large partial payment of 400,000,000 (80% of 500,000,000) via
    // transaction
    var transactionBody = """
        {
          "type": "DEBIT",
          "date": "2024-07-01",
          "time": "10:00:00",
          "title": "Large Partial Repayment",
          "description": "Large partial loan repayment",
          "amount": 400000000,
          "currency": "IDR",
          "source": "%s",
          "loan": "%s",
          "category": "LOAN"
        }
        """.formatted(sourceAccountId, loanId);

    var transactionResponse = Unirest.post(baseUrl + "/v1/transactions")
        .header("Authorization", "Bearer " + token)
        .body(transactionBody)
        .asString();

    assertEquals(200, transactionResponse.getStatus());

    // Verify the loan has remaining amount of 100,000,000
    var getLoanResponse = Unirest.get(baseUrl + "/v1/loans/" + loanId)
        .header("Authorization", "Bearer " + token)
        .asString();

    var loanBeforeUpdate = new JSONObject(getLoanResponse.getBody());
    assertEquals(500000000, loanBeforeUpdate.getLong("amount"));
    assertEquals(100000000, loanBeforeUpdate.getLong("remainingAmount"));

    // Update the loan amount to 300,000,000 (less than the repaid 400,000,000)
    var updateBody = """
        {
          "amount": 300000000
        }
        """;

    var updateResponse = Unirest.put(baseUrl + "/v1/loans/" + loanId)
        .header("Authorization", "Bearer " + token)
        .body(updateBody)
        .asString();

    assertEquals(200, updateResponse.getStatus());
    var updatedLoan = new JSONObject(updateResponse.getBody());
    assertEquals(300000000, updatedLoan.getLong("amount"));
    // remainingAmount should be: new amount - repaid amount = 300,000,000 -
    // 400,000,000 = -100,000,000
    assertEquals(-100000000, updatedLoan.getLong("remainingAmount"));
  }
}
