package io.dkakunsi.bitapp.money.loan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.dkakunsi.bitapp.jwt.JWTAuthorizer;
import io.dkakunsi.bitapp.money.AppLauncher;
import io.dkakunsi.bitapp.test.AppTestUtil;
import io.dkakunsi.bitapp.test.SecureTestUtil;
import kong.unirest.Unirest;

public class RemoveLoanIT extends AppTestUtil {

  private static final int port = 20014;

  private static RemoveLoanIT sut = new RemoveLoanIT();

  private static String baseUrl;

  private static String token;

  private String sourceAccountId;
  private String destinationAccountId;

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
  void setupTestData() {
    sourceAccountId = createAccount("Source Account", "BANK", 1000000);
    destinationAccountId = createAccount("Destination Account", "BANK", 500000);
  }

  private String createAccount(String name, String type, long initialBalance) {
    var body = String.format("""
        {
          "name": "%s",
          "type": "%s",
          "themeColor": "#0000FF"
        }
        """, name, type);

    var response = Unirest.post(baseUrl + "/accounts")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(200, response.getStatus());
    var accountId = new JSONObject(response.getBody()).getString("id");

    if (initialBalance > 0) {
      var depositBody = String.format("""
          {
            "type": "CREDIT",
            "title": "Initial Balance",
            "description": "Initial balance setup",
            "destination": "%s",
            "amount": %d,
            "currency": "IDR"
          }
          """, accountId, initialBalance);

      var depositResponse = Unirest.post(baseUrl + "/transactions")
          .header("Authorization", "Bearer " + token)
          .body(depositBody)
          .asString();

      assertEquals(200, depositResponse.getStatus());
    }

    return accountId;
  }

  private String createLoan(String type, String partyName, String title, long amount, double interestRate) {
    var body = String.format("""
        {
          "type": "%s",
          "partyName": "%s",
          "title": "%s",
          "description": "Test loan",
          "amount": %d,
          "currency": "IDR",
          "interestRate": %.1f
        }
        """, type, partyName, title, amount, interestRate);

    var response = Unirest.post(baseUrl + "/loans")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(200, response.getStatus());
    return new JSONObject(response.getBody()).getString("id");
  }

  private String createTransactionWithLoan(String type, String title, String source, String destination,
      String loanId, long amount) {
    var bodyBuilder = new StringBuilder("{");
    bodyBuilder.append(String.format("\"type\":\"%s\",", type));
    bodyBuilder.append(String.format("\"title\":\"%s\",", title));
    bodyBuilder.append("\"description\":\"Transaction with loan\",");

    if (source != null) {
      bodyBuilder.append(String.format("\"source\":\"%s\",", source));
    }
    if (destination != null) {
      bodyBuilder.append(String.format("\"destination\":\"%s\",", destination));
    }

    bodyBuilder.append(String.format("\"loan\":\"%s\",", loanId));
    bodyBuilder.append(String.format("\"amount\":%d,", amount));
    bodyBuilder.append("\"currency\":\"IDR\",");
    bodyBuilder.append("\"category\":\"LOAN\"");
    bodyBuilder.append("}");

    var response = Unirest.post(baseUrl + "/transactions")
        .header("Authorization", "Bearer " + token)
        .body(bodyBuilder.toString())
        .asString();

    assertEquals(200, response.getStatus());
    return new JSONObject(response.getBody()).getString("id");
  }

  /**
   * <b>Given</b> a loan exists without any transactions<br>
   * <b>When</b> the DELETE /loans/{id} endpoint is called<br>
   * <b>Then</b> the loan should be deleted from the database
   */
  @Test
  public void removeLoanWithoutTransactionsShouldBeDeleted() {
    var loanId = createLoan("BORROW", "John Doe", "Simple Loan", 1000000, 5.0);

    // Verify loan exists
    var getResponse = Unirest.get(baseUrl + "/loans/" + loanId)
        .header("Authorization", "Bearer " + token)
        .asString();
    assertEquals(200, getResponse.getStatus());

    // Remove the loan
    var deleteResponse = Unirest.delete(baseUrl + "/loans/" + loanId)
        .header("Authorization", "Bearer " + token)
        .asString();
    assertEquals(200, deleteResponse.getStatus());

    // Verify loan is deleted from database
    var getAfterDelete = Unirest.get(baseUrl + "/loans/" + loanId)
        .header("Authorization", "Bearer " + token)
        .asString();
    assertEquals(404, getAfterDelete.getStatus());
  }

  /**
   * <b>Given</b> a loan with a single repayment transaction<br>
   * <b>When</b> the DELETE /loans/{id} endpoint is called<br>
   * <b>Then</b> the loan should be deleted and the transaction's loan reference
   * should be removed
   */
  @Test
  public void removeLoanWithRepaymentTransactionShouldUpdateTransaction() {
    var loanId = createLoan("BORROW", "Jane Smith", "Loan with Payment", 1000000, 5.0);

    // Create a repayment transaction
    var transactionId = createTransactionWithLoan("DEBIT", "Loan Repayment", sourceAccountId, null, loanId,
        100000);

    // Verify transaction has loan reference
    var getTransactionBefore = Unirest.get(baseUrl + "/transactions/" + transactionId)
        .header("Authorization", "Bearer " + token)
        .asString();
    assertEquals(200, getTransactionBefore.getStatus());
    var transactionBefore = new JSONObject(getTransactionBefore.getBody());
    assertEquals(loanId, transactionBefore.getString("loan"));

    // Remove the loan
    var deleteResponse = Unirest.delete(baseUrl + "/loans/" + loanId)
        .header("Authorization", "Bearer " + token)
        .asString();
    assertEquals(200, deleteResponse.getStatus());

    // Verify loan is deleted
    assertEquals(404, Unirest.get(baseUrl + "/loans/" + loanId)
        .header("Authorization", "Bearer " + token).asString().getStatus());

    // Verify transaction still exists but loan reference is removed
    var getTransactionAfter = Unirest.get(baseUrl + "/transactions/" + transactionId)
        .header("Authorization", "Bearer " + token)
        .asString();
    assertEquals(200, getTransactionAfter.getStatus());
    var transactionAfter = new JSONObject(getTransactionAfter.getBody());
    // Loan field should be null or empty
    assertTrue(!transactionAfter.has("loan") || transactionAfter.isNull("loan")
        || transactionAfter.getString("loan").isEmpty());
  }

  /**
   * <b>Given</b> a loan with multiple transactions<br>
   * <b>When</b> the DELETE /loans/{id} endpoint is called<br>
   * <b>Then</b> the loan should be deleted and all related transactions should
   * have their loan references removed
   */
  @Test
  public void removeLoanWithMultipleTransactionsShouldUpdateAllTransactions() {
    var loanId = createLoan("BORROW", "Bob Johnson", "Loan with Multiple Payments", 1000000, 5.0);

    // Create multiple transactions with this loan
    var transaction1Id = createTransactionWithLoan("DEBIT", "Payment 1", sourceAccountId, null, loanId, 100000);
    var transaction2Id = createTransactionWithLoan("DEBIT", "Payment 2", sourceAccountId, null, loanId, 150000);
    var transaction3Id = createTransactionWithLoan("DEBIT", "Payment 3", sourceAccountId, null, loanId, 200000);

    // Remove the loan
    var deleteResponse = Unirest.delete(baseUrl + "/loans/" + loanId)
        .header("Authorization", "Bearer " + token)
        .asString();
    assertEquals(200, deleteResponse.getStatus());

    // Verify loan is deleted
    assertEquals(404, Unirest.get(baseUrl + "/loans/" + loanId)
        .header("Authorization", "Bearer " + token).asString().getStatus());

    // Verify all transactions still exist but loan references are removed
    checkTransactionLoanReferenceRemoved(transaction1Id);
    checkTransactionLoanReferenceRemoved(transaction2Id);
    checkTransactionLoanReferenceRemoved(transaction3Id);
  }

  private void checkTransactionLoanReferenceRemoved(String transactionId) {
    var getResponse = Unirest.get(baseUrl + "/transactions/" + transactionId)
        .header("Authorization", "Bearer " + token)
        .asString();
    assertEquals(200, getResponse.getStatus());
    var transaction = new JSONObject(getResponse.getBody());
    assertTrue(!transaction.has("loan") || transaction.isNull("loan")
        || transaction.getString("loan").isEmpty(),
        "Transaction should not have loan reference after loan deletion");
  }

  /**
   * <b>Given</b> a loan with disbursement transaction<br>
   * <b>When</b> the DELETE /loans/{id} endpoint is called<br>
   * <b>Then</b> the loan should be deleted and the disbursement transaction's
   * loan reference should be removed
   */
  @Test
  public void removeLoanWithDisbursementTransactionShouldUpdateTransaction() {
    var loanId = createLoan("BORROW", "Alice Brown", "Loan with Disbursement", 1000000, 5.0);

    // Create a disbursement transaction
    var transactionId = createTransactionWithLoan("CREDIT", "Loan Disbursement", null, destinationAccountId,
        loanId, 500000);

    // Remove the loan
    var deleteResponse = Unirest.delete(baseUrl + "/loans/" + loanId)
        .header("Authorization", "Bearer " + token)
        .asString();
    assertEquals(200, deleteResponse.getStatus());

    // Verify loan is deleted
    assertEquals(404, Unirest.get(baseUrl + "/loans/" + loanId)
        .header("Authorization", "Bearer " + token).asString().getStatus());

    // Verify transaction still exists but loan reference is removed
    checkTransactionLoanReferenceRemoved(transactionId);
  }

  /**
   * <b>Given</b> a loan that does not exist<br>
   * <b>When</b> the DELETE /loans/{id} endpoint is called<br>
   * <b>Then</b> the request should fail with status 404
   */
  @Test
  public void removeNonExistentLoanShouldFail() {
    var nonExistentId = "non-existent-loan-id";

    var response = Unirest.delete(baseUrl + "/loans/" + nonExistentId)
        .header("Authorization", "Bearer " + token)
        .asString();

    assertEquals(404, response.getStatus());
  }

  /**
   * <b>Given</b> a request without authorization header<br>
   * <b>When</b> the DELETE /loans/{id} endpoint is called<br>
   * <b>Then</b> the request should fail with status 401
   */
  @Test
  public void removeLoanWithoutAuthorizationShouldFail() {
    var loanId = createLoan("BORROW", "Test User", "Test Loan", 1000000, 5.0);

    var response = Unirest.delete(baseUrl + "/loans/" + loanId)
        .asString();

    assertEquals(401, response.getStatus());

    // Verify the loan still exists
    var getResponse = Unirest.get(baseUrl + "/loans/" + loanId)
        .header("Authorization", "Bearer " + token)
        .asString();
    assertEquals(200, getResponse.getStatus());
  }

  /**
   * <b>Given</b> a request with invalid token<br>
   * <b>When</b> the DELETE /loans/{id} endpoint is called<br>
   * <b>Then</b> the request should fail with status 401
   */
  @Test
  public void removeLoanWithInvalidTokenShouldFail() {
    var loanId = createLoan("BORROW", "Test User", "Test Loan", 1000000, 5.0);

    var response = Unirest.delete(baseUrl + "/loans/" + loanId)
        .header("Authorization", "Bearer invalid-token")
        .asString();

    assertEquals(401, response.getStatus());

    // Verify the loan still exists
    var getResponse = Unirest.get(baseUrl + "/loans/" + loanId)
        .header("Authorization", "Bearer " + token)
        .asString();
    assertEquals(200, getResponse.getStatus());
  }

  /**
   * <b>Given</b> a user attempting to delete another user's loan<br>
   * <b>When</b> the DELETE /loans/{id} endpoint is called<br>
   * <b>Then</b> the request should fail with status 403 or 404
   */
  @Test
  public void removeAnotherUserLoanShouldFail() {
    var loanId = createLoan("BORROW", "Test User", "Test Loan", 1000000, 5.0);

    // Try to delete with a different user's token
    var otherUserToken = SecureTestUtil.generateToken("otheruser@email.com");

    var response = Unirest.delete(baseUrl + "/loans/" + loanId)
        .header("Authorization", "Bearer " + otherUserToken)
        .asString();

    // Should fail with 403 (Forbidden) or 404 (Not Found)
    assertTrue(response.getStatus() == 403 || response.getStatus() == 404);

    // Verify the loan still exists for the original user
    var getResponse = Unirest.get(baseUrl + "/loans/" + loanId)
        .header("Authorization", "Bearer " + token)
        .asString();
    assertEquals(200, getResponse.getStatus());
  }

  /**
   * <b>Given</b> a loan is successfully removed<br>
   * <b>When</b> the loan details are requested<br>
   * <b>Then</b> the loan should not be found in database
   */
  @Test
  public void removedLoanShouldNotBeRetrievable() {
    var loanId = createLoan("BORROW", "Test User", "Test Loan", 1000000, 5.0);

    // Remove the loan
    var deleteResponse = Unirest.delete(baseUrl + "/loans/" + loanId)
        .header("Authorization", "Bearer " + token)
        .asString();
    assertEquals(200, deleteResponse.getStatus());

    // Try to retrieve the removed loan - should return 404 (not found in database)
    var getResponse = Unirest.get(baseUrl + "/loans/" + loanId)
        .header("Authorization", "Bearer " + token)
        .asString();
    assertEquals(404, getResponse.getStatus());
  }

  /**
   * <b>Given</b> a loan is successfully removed<br>
   * <b>When</b> the user's loans are listed<br>
   * <b>Then</b> the removed loan should not appear in the list
   */
  @Test
  public void removedLoanShouldNotAppearInUserLoansList() {
    // Create multiple loans
    var loan1Id = createLoan("BORROW", "Bank A", "Car Loan", 5000000, 4.5);
    var loan2Id = createLoan("BORROW", "Bank B", "House Loan", 10000000, 3.5);
    var loan3Id = createLoan("LEND", "Friend", "Personal Lend", 500000, 0.0);

    // Remove the second loan
    var deleteResponse = Unirest.delete(baseUrl + "/loans/" + loan2Id)
        .header("Authorization", "Bearer " + token)
        .asString();
    assertEquals(200, deleteResponse.getStatus());

    // Get user's loans list
    var listResponse = Unirest.get(baseUrl + "/users/" + USER_ID + "/loans")
        .header("Authorization", "Bearer " + token)
        .asString();
    assertEquals(200, listResponse.getStatus());

    var loans = new JSONArray(listResponse.getBody());

    // Verify the deleted loan is not in the list
    boolean foundDeletedLoan = false;
    boolean foundLoan1 = false;
    boolean foundLoan3 = false;

    for (int i = 0; i < loans.length(); i++) {
      var loan = loans.getJSONObject(i);
      var id = loan.getString("id");

      if (id.equals(loan2Id)) {
        foundDeletedLoan = true;
      }
      if (id.equals(loan1Id)) {
        foundLoan1 = true;
      }
      if (id.equals(loan3Id)) {
        foundLoan3 = true;
      }
    }

    assertFalse(foundDeletedLoan, "Deleted loan should not appear in user's loan list");
    assertTrue(foundLoan1, "Non-deleted loan 1 should still exist");
    assertTrue(foundLoan3, "Non-deleted loan 3 should still exist");
  }

  /**
   * <b>Given</b> a loan is successfully removed<br>
   * <b>When</b> checking the database<br>
   * <b>Then</b> the loan should be completely deleted (not soft-deleted)
   */
  @Test
  public void removedLoanShouldBeCompletelyDeletedFromDatabase() {
    var loanId = createLoan("BORROW", "Test Bank", "Test Loan", 1000000, 5.0);

    // Verify loan exists before deletion
    var getBeforeDelete = Unirest.get(baseUrl + "/loans/" + loanId)
        .header("Authorization", "Bearer " + token)
        .asString();
    assertEquals(200, getBeforeDelete.getStatus());

    // Remove the loan
    var deleteResponse = Unirest.delete(baseUrl + "/loans/" + loanId)
        .header("Authorization", "Bearer " + token)
        .asString();
    assertEquals(200, deleteResponse.getStatus());

    // Verify loan no longer exists - should return 404 (hard delete)
    var getAfterDelete = Unirest.get(baseUrl + "/loans/" + loanId)
        .header("Authorization", "Bearer " + token)
        .asString();
    assertEquals(404, getAfterDelete.getStatus());

    // Verify it's not in the user's loan list either
    var listResponse = Unirest.get(baseUrl + "/users/" + USER_ID + "/loans")
        .header("Authorization", "Bearer " + token)
        .asString();
    assertEquals(200, listResponse.getStatus());

    var loans = new JSONArray(listResponse.getBody());
    for (int i = 0; i < loans.length(); i++) {
      var loan = loans.getJSONObject(i);
      if (loan.getString("id").equals(loanId)) {
        throw new AssertionError("Loan should be completely deleted from database, not just hidden");
      }
    }
  }

  /**
   * <b>Given</b> multiple loans are removed<br>
   * <b>When</b> checking the database<br>
   * <b>Then</b> all removed loans should be deleted from the database
   */
  @Test
  public void multipleRemovedLoansShouldAllBeDeletedFromDatabase() {
    // Create multiple loans
    var loan1Id = createLoan("BORROW", "Bank 1", "Loan 1", 1000000, 4.0);
    var loan2Id = createLoan("BORROW", "Bank 2", "Loan 2", 2000000, 5.0);
    var loan3Id = createLoan("LEND", "Person 1", "Loan 3", 500000, 0.0);
    var keepLoanId = createLoan("BORROW", "Bank Keep", "Keep This", 3000000, 6.0);

    // Remove three loans
    assertEquals(200, Unirest.delete(baseUrl + "/loans/" + loan1Id)
        .header("Authorization", "Bearer " + token).asString().getStatus());
    assertEquals(200, Unirest.delete(baseUrl + "/loans/" + loan2Id)
        .header("Authorization", "Bearer " + token).asString().getStatus());
    assertEquals(200, Unirest.delete(baseUrl + "/loans/" + loan3Id)
        .header("Authorization", "Bearer " + token).asString().getStatus());

    // Verify none of the deleted loans can be retrieved
    assertEquals(404, Unirest.get(baseUrl + "/loans/" + loan1Id)
        .header("Authorization", "Bearer " + token).asString().getStatus());
    assertEquals(404, Unirest.get(baseUrl + "/loans/" + loan2Id)
        .header("Authorization", "Bearer " + token).asString().getStatus());
    assertEquals(404, Unirest.get(baseUrl + "/loans/" + loan3Id)
        .header("Authorization", "Bearer " + token).asString().getStatus());

    // Verify the kept loan still exists
    assertEquals(200, Unirest.get(baseUrl + "/loans/" + keepLoanId)
        .header("Authorization", "Bearer " + token).asString().getStatus());

    // Verify none of the deleted loans appear in the user's list
    var listResponse = Unirest.get(baseUrl + "/users/" + USER_ID + "/loans")
        .header("Authorization", "Bearer " + token)
        .asString();
    assertEquals(200, listResponse.getStatus());

    var loans = new JSONArray(listResponse.getBody());
    for (int i = 0; i < loans.length(); i++) {
      var loan = loans.getJSONObject(i);
      var id = loan.getString("id");
      assertFalse(id.equals(loan1Id) || id.equals(loan2Id) || id.equals(loan3Id),
          "Deleted loans should not appear in database");
    }
  }

  /**
   * <b>Given</b> a successful loan removal<br>
   * <b>When</b> the DELETE endpoint returns<br>
   * <b>Then</b> it should return the removed loan details
   */
  @Test
  public void removeLoanShouldReturnLoanDetails() {
    var loanId = createLoan("BORROW", "Test Bank", "Test Loan", 1000000, 5.5);

    var response = Unirest.delete(baseUrl + "/loans/" + loanId)
        .header("Authorization", "Bearer " + token)
        .asString();

    assertEquals(200, response.getStatus());

    // Verify response contains loan details
    var responseBody = new JSONObject(response.getBody());
    assertNotNull(responseBody.getString("id"));
    assertEquals(loanId, responseBody.getString("id"));
    assertEquals("Test Loan", responseBody.getString("title"));
    assertEquals("BORROW", responseBody.getString("type"));
    assertEquals("Test Bank", responseBody.getString("partyName"));
  }

  /**
   * <b>Given</b> a loan with both repayment and disbursement transactions<br>
   * <b>When</b> the DELETE /loans/{id} endpoint is called<br>
   * <b>Then</b> all related transactions should have loan references removed
   */
  @Test
  public void removeLoanWithMixedTransactionsShouldUpdateAllTransactions() {
    var loanId = createLoan("BORROW", "Test Bank", "Complex Loan", 2000000, 5.0);

    // Create disbursement
    var disbursementId = createTransactionWithLoan("CREDIT", "Loan Disbursement", null, destinationAccountId,
        loanId, 2000000);

    // Create multiple repayments
    var repayment1Id = createTransactionWithLoan("DEBIT", "Repayment 1", sourceAccountId, null, loanId, 200000);
    var repayment2Id = createTransactionWithLoan("DEBIT", "Repayment 2", sourceAccountId, null, loanId, 250000);

    // Remove the loan
    var deleteResponse = Unirest.delete(baseUrl + "/loans/" + loanId)
        .header("Authorization", "Bearer " + token)
        .asString();
    assertEquals(200, deleteResponse.getStatus());

    // Verify loan is deleted
    assertEquals(404, Unirest.get(baseUrl + "/loans/" + loanId)
        .header("Authorization", "Bearer " + token).asString().getStatus());

    // Verify all transactions still exist but loan references are removed
    checkTransactionLoanReferenceRemoved(disbursementId);
    checkTransactionLoanReferenceRemoved(repayment1Id);
    checkTransactionLoanReferenceRemoved(repayment2Id);
  }

  /**
   * <b>Given</b> a fully repaid loan (remaining amount is zero)<br>
   * <b>When</b> the DELETE /loans/{id} endpoint is called<br>
   * <b>Then</b> the loan should be deleted and transactions updated
   */
  @Test
  public void removeFullyRepaidLoanShouldSucceed() {
    var loanId = createLoan("BORROW", "Test Bank", "Fully Paid Loan", 500000, 5.0);

    // Create a transaction that fully repays the loan
    var transactionId = createTransactionWithLoan("DEBIT", "Full Repayment", sourceAccountId, null, loanId,
        500000);

    // Verify loan is fully repaid
    var getLoan = Unirest.get(baseUrl + "/loans/" + loanId)
        .header("Authorization", "Bearer " + token)
        .asString();
    assertEquals(200, getLoan.getStatus());

    // Remove the loan
    var deleteResponse = Unirest.delete(baseUrl + "/loans/" + loanId)
        .header("Authorization", "Bearer " + token)
        .asString();
    assertEquals(200, deleteResponse.getStatus());

    // Verify loan is deleted
    assertEquals(404, Unirest.get(baseUrl + "/loans/" + loanId)
        .header("Authorization", "Bearer " + token).asString().getStatus());

    // Verify transaction still exists but loan reference is removed
    checkTransactionLoanReferenceRemoved(transactionId);
  }

  /**
   * <b>Given</b> a LEND type loan with transactions<br>
   * <b>When</b> the DELETE /loans/{id} endpoint is called<br>
   * <b>Then</b> the loan should be deleted and transactions updated
   */
  @Test
  public void removeLendLoanWithTransactionsShouldUpdateTransactions() {
    var loanId = createLoan("LEND", "Borrower Name", "Money Lent", 1000000, 3.5);

    // Create disbursement (we're lending money)
    var disbursementId = createTransactionWithLoan("DEBIT", "Lend Money", sourceAccountId, null, loanId, 1000000);

    // Create repayment (receiving money back)
    var repaymentId = createTransactionWithLoan("CREDIT", "Receive Payment", null, destinationAccountId, loanId,
        150000);

    // Remove the loan
    var deleteResponse = Unirest.delete(baseUrl + "/loans/" + loanId)
        .header("Authorization", "Bearer " + token)
        .asString();
    assertEquals(200, deleteResponse.getStatus());

    // Verify loan is deleted
    assertEquals(404, Unirest.get(baseUrl + "/loans/" + loanId)
        .header("Authorization", "Bearer " + token).asString().getStatus());

    // Verify both transactions still exist but loan references are removed
    checkTransactionLoanReferenceRemoved(disbursementId);
    checkTransactionLoanReferenceRemoved(repaymentId);
  }
}
