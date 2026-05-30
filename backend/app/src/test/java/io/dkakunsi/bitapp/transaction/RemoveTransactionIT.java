package io.dkakunsi.bitapp.transaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
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

public class RemoveTransactionIT extends AppTestUtil {

  private static RemoveTransactionIT sut = new RemoveTransactionIT();

  private static String baseUrl;

  private static String token;

  private String sourceAccountId;

  private String destinationAccountId;

  private String loanId;

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

  @BeforeEach
  void setupTestData() {
    sourceAccountId = createAccount("Source Account", "BANK", 1000000);
    destinationAccountId = createAccount("Destination Account", "BANK", 500000);
    loanId = createLoan("BORROW", "John Doe", "Personal Loan", 2000000, 5.0);
  }

  private String createAccount(String name, String type, long initialBalance) {
    var body = String.format("""
        {
          "name": "%s",
          "type": "%s",
          "themeColor": "#0000FF"
        }
        """, name, type);

    var response = Unirest.post(baseUrl + "/v1/accounts")
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

      var depositResponse = Unirest.post(baseUrl + "/v1/transactions")
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

    var response = Unirest.post(baseUrl + "/v1/loans")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(200, response.getStatus());
    return new JSONObject(response.getBody()).getString("id");
  }

  private String createTransaction(String type, String title, String description, String source, String destination,
      String loan, long amount, String category) {
    var bodyBuilder = new StringBuilder("{");
    bodyBuilder.append(String.format("\"type\":\"%s\",", type));
    bodyBuilder.append(String.format("\"title\":\"%s\",", title));
    bodyBuilder.append(String.format("\"description\":\"%s\",", description));

    if (source != null) {
      bodyBuilder.append(String.format("\"source\":\"%s\",", source));
    }
    if (destination != null) {
      bodyBuilder.append(String.format("\"destination\":\"%s\",", destination));
    }
    if (loan != null) {
      bodyBuilder.append(String.format("\"loan\":\"%s\",", loan));
    }

    bodyBuilder.append(String.format("\"amount\":%d,", amount));
    bodyBuilder.append("\"currency\":\"IDR\"");

    if (category != null) {
      bodyBuilder.append(String.format(",\"category\":\"%s\"", category));
    }

    bodyBuilder.append("}");

    var response = Unirest.post(baseUrl + "/v1/transactions")
        .header("Authorization", "Bearer " + token)
        .body(bodyBuilder.toString())
        .asString();

    assertEquals(200, response.getStatus());
    return new JSONObject(response.getBody()).getString("id");
  }

  private BigDecimal getAccountBalance(String accountId) {
    var response = Unirest.get(baseUrl + "/v1/accounts/" + accountId)
        .header("Authorization", "Bearer " + token)
        .asString();

    assertEquals(200, response.getStatus());
    return new JSONObject(response.getBody()).getBigDecimal("balance");
  }

  private BigDecimal getLoanRemainingAmount(String loanId) {
    var response = Unirest.get(baseUrl + "/v1/loans/" + loanId)
        .header("Authorization", "Bearer " + token)
        .asString();

    assertEquals(200, response.getStatus());
    return new JSONObject(response.getBody()).getBigDecimal("remainingAmount");
  }

  /**
   * <b>Given</b> a DEBIT transaction exists<br>
   * <b>When</b> the DELETE /transactions/{id} endpoint is called<br>
   * <b>Then</b> the transaction should be deleted and the source account balance
   * should be restored
   */
  @Test
  public void removeDebitTransactionShouldRestoreSourceAccountBalance() {
    // Create a debit transaction
    var transactionId = createTransaction("DEBIT", "Shopping", "Grocery shopping", sourceAccountId, null, null,
        50000, "FOOD");

    // Get account balance after transaction
    var balanceAfterDebit = getAccountBalance(sourceAccountId);
    assertEquals(0, new BigDecimal("950000").compareTo(balanceAfterDebit));

    // Remove the transaction
    var response = Unirest.delete(baseUrl + "/v1/transactions/" + transactionId)
        .header("Authorization", "Bearer " + token)
        .asString();

    assertEquals(200, response.getStatus());

    // Verify the account balance is restored
    var balanceAfterRemoval = getAccountBalance(sourceAccountId);
    assertEquals(0, new BigDecimal("1000000").compareTo(balanceAfterRemoval));
  }

  /**
   * <b>Given</b> a CREDIT transaction exists<br>
   * <b>When</b> the DELETE /transactions/{id} endpoint is called<br>
   * <b>Then</b> the transaction should be deleted and the destination account
   * balance should be reduced
   */
  @Test
  public void removeCreditTransactionShouldReduceDestinationAccountBalance() {
    // Create a credit transaction
    var transactionId = createTransaction("CREDIT", "Income", "Freelance payment", null, destinationAccountId, null,
        100000, "INCOME");

    // Get account balance after transaction
    var balanceAfterCredit = getAccountBalance(destinationAccountId);
    assertEquals(0, new BigDecimal("600000").compareTo(balanceAfterCredit));

    // Remove the transaction
    var response = Unirest.delete(baseUrl + "/v1/transactions/" + transactionId)
        .header("Authorization", "Bearer " + token)
        .asString();

    assertEquals(200, response.getStatus());

    // Verify the account balance is reduced back
    var balanceAfterRemoval = getAccountBalance(destinationAccountId);
    assertEquals(0, new BigDecimal("500000").compareTo(balanceAfterRemoval));
  }

  /**
   * <b>Given</b> a TRANSFER transaction exists<br>
   * <b>When</b> the DELETE /transactions/{id} endpoint is called<br>
   * <b>Then</b> the transaction should be deleted and both account balances
   * should be reverted
   */
  @Test
  public void removeTransferTransactionShouldRevertBothAccountBalances() {
    // Create a transfer transaction
    var transactionId = createTransaction("TRANSFER", "Transfer", "Move money", sourceAccountId,
        destinationAccountId, null, 200000, "OTHER");

    // Get account balances after transaction
    var sourceBalanceAfterTransfer = getAccountBalance(sourceAccountId);
    var destBalanceAfterTransfer = getAccountBalance(destinationAccountId);
    assertEquals(0, new BigDecimal("800000").compareTo(sourceBalanceAfterTransfer));
    assertEquals(0, new BigDecimal("700000").compareTo(destBalanceAfterTransfer));

    // Remove the transaction
    var response = Unirest.delete(baseUrl + "/v1/transactions/" + transactionId)
        .header("Authorization", "Bearer " + token)
        .asString();

    assertEquals(200, response.getStatus());

    // Verify both account balances are reverted
    var sourceBalanceAfterRemoval = getAccountBalance(sourceAccountId);
    var destBalanceAfterRemoval = getAccountBalance(destinationAccountId);
    assertEquals(0, new BigDecimal("1000000").compareTo(sourceBalanceAfterRemoval));
    assertEquals(0, new BigDecimal("500000").compareTo(destBalanceAfterRemoval));
  }

  /**
   * <b>Given</b> a transaction with a loan reference exists<br>
   * <b>When</b> the DELETE /transactions/{id} endpoint is called<br>
   * <b>Then</b> the transaction should be deleted and the loan remaining amount
   * should be reverted
   */
  @Test
  public void removeTransactionWithLoanShouldRevertLoanRemainingAmount() {
    // Create a debit transaction with loan (repayment)
    var transactionId = createTransaction("DEBIT", "Loan Repayment", "Monthly payment", sourceAccountId, null,
        loanId, 100000, "LOAN");

    // Get loan remaining amount after transaction
    var remainingAfterPayment = getLoanRemainingAmount(loanId);
    assertEquals(0, new BigDecimal("1900000").compareTo(remainingAfterPayment));

    // Get account balance after transaction
    var accountBalanceAfterPayment = getAccountBalance(sourceAccountId);
    assertEquals(0, new BigDecimal("900000").compareTo(accountBalanceAfterPayment));

    // Remove the transaction
    var response = Unirest.delete(baseUrl + "/v1/transactions/" + transactionId)
        .header("Authorization", "Bearer " + token)
        .asString();

    assertEquals(200, response.getStatus());

    // Verify the loan remaining amount is restored
    var remainingAfterRemoval = getLoanRemainingAmount(loanId);
    assertEquals(0, new BigDecimal("2000000").compareTo(remainingAfterRemoval));

    // Verify the account balance is also restored
    var accountBalanceAfterRemoval = getAccountBalance(sourceAccountId);
    assertEquals(0, new BigDecimal("1000000").compareTo(accountBalanceAfterRemoval));
  }

  /**
   * <b>Given</b> a loan disbursement transaction (CREDIT with loan) exists<br>
   * <b>When</b> the DELETE /transactions/{id} endpoint is called<br>
   * <b>Then</b> the transaction should be deleted, the account balance reduced,
   * and the loan remaining amount should increase
   */
  @Test
  public void removeLoanDisbursementShouldRevertAccountAndLoanBalances() {
    // Create a credit transaction with loan (disbursement)
    var transactionId = createTransaction("CREDIT", "Loan Disbursement", "Loan received", null,
        destinationAccountId, loanId, 500000, "LOAN");

    // Get balances after transaction
    var accountBalanceAfterDisbursement = getAccountBalance(destinationAccountId);
    var loanRemainingAfterDisbursement = getLoanRemainingAmount(loanId);
    assertEquals(0, new BigDecimal("1000000").compareTo(accountBalanceAfterDisbursement));
    assertEquals(0, new BigDecimal("1500000").compareTo(loanRemainingAfterDisbursement));

    // Remove the transaction
    var response = Unirest.delete(baseUrl + "/v1/transactions/" + transactionId)
        .header("Authorization", "Bearer " + token)
        .asString();

    assertEquals(200, response.getStatus());

    // Verify the balances are reverted
    var accountBalanceAfterRemoval = getAccountBalance(destinationAccountId);
    var loanRemainingAfterRemoval = getLoanRemainingAmount(loanId);
    assertEquals(0, new BigDecimal("500000").compareTo(accountBalanceAfterRemoval));
    assertEquals(0, new BigDecimal("2000000").compareTo(loanRemainingAfterRemoval));
  }

  /**
   * <b>Given</b> a transaction that does not exist<br>
   * <b>When</b> the DELETE /transactions/{id} endpoint is called<br>
   * <b>Then</b> the request should fail with status 404
   */
  @Test
  public void removeNonExistentTransactionShouldFail() {
    var nonExistentId = "non-existent-id";

    var response = Unirest.delete(baseUrl + "/v1/transactions/" + nonExistentId)
        .header("Authorization", "Bearer " + token)
        .asString();

    assertEquals(404, response.getStatus());
  }

  /**
   * <b>Given</b> a request without authorization header<br>
   * <b>When</b> the DELETE /transactions/{id} endpoint is called<br>
   * <b>Then</b> the request should fail with status 401
   */
  @Test
  public void removeTransactionWithoutAuthorizationShouldFail() {
    var transactionId = createTransaction("DEBIT", "Shopping", "Grocery", sourceAccountId, null, null, 50000,
        "FOOD");

    var response = Unirest.delete(baseUrl + "/v1/transactions/" + transactionId)
        .asString();

    assertEquals(401, response.getStatus());

    // Verify the transaction still exists and account balance unchanged
    var balanceAfterFailedRemoval = getAccountBalance(sourceAccountId);
    assertEquals(0, new BigDecimal("950000").compareTo(balanceAfterFailedRemoval));
  }

  /**
   * <b>Given</b> a request with invalid token<br>
   * <b>When</b> the DELETE /transactions/{id} endpoint is called<br>
   * <b>Then</b> the request should fail with status 401
   */
  @Test
  public void removeTransactionWithInvalidTokenShouldFail() {
    var transactionId = createTransaction("DEBIT", "Shopping", "Grocery", sourceAccountId, null, null, 50000,
        "FOOD");

    var response = Unirest.delete(baseUrl + "/v1/transactions/" + transactionId)
        .header("Authorization", "Bearer invalid-token")
        .asString();

    assertEquals(401, response.getStatus());

    // Verify the transaction still exists and account balance unchanged
    var balanceAfterFailedRemoval = getAccountBalance(sourceAccountId);
    assertEquals(0, new BigDecimal("950000").compareTo(balanceAfterFailedRemoval));
  }

  /**
   * <b>Given</b> a user attempting to delete another user's transaction<br>
   * <b>When</b> the DELETE /transactions/{id} endpoint is called<br>
   * <b>Then</b> the request should fail with status 403 or 404
   */
  @Test
  public void removeAnotherUserTransactionShouldFail() {
    // Create transaction with the current user
    var transactionId = createTransaction("DEBIT", "Shopping", "Grocery", sourceAccountId, null, null, 50000,
        "FOOD");

    // Try to delete with a different user's token
    var otherUserToken = SecureTestUtil.generateToken("otheruser@email.com");

    var response = Unirest.delete(baseUrl + "/v1/transactions/" + transactionId)
        .header("Authorization", "Bearer " + otherUserToken)
        .asString();

    // Should fail with 403 (Forbidden) or 404 (Not Found)
    assertEquals(true, response.getStatus() == 403 || response.getStatus() == 404);

    // Verify the transaction still exists and account balance unchanged
    var balanceAfterFailedRemoval = getAccountBalance(sourceAccountId);
    assertEquals(0, new BigDecimal("950000").compareTo(balanceAfterFailedRemoval));
  }

  /**
   * <b>Given</b> multiple transactions on an account<br>
   * <b>When</b> one transaction is removed<br>
   * <b>Then</b> only that transaction's impact should be reverted
   */
  @Test
  public void removeOneOfMultipleTransactionsShouldOnlyRevertThatTransaction() {
    // Create multiple transactions
    createTransaction("DEBIT", "Shopping 1", "First shopping", sourceAccountId, null, null,
        50000, "FOOD");
    createTransaction("DEBIT", "Shopping 3", "Third shopping", sourceAccountId, null, null,
        20000, "FOOD");
    var transaction2Id = createTransaction("DEBIT", "Shopping 2", "Second shopping", sourceAccountId, null, null,
        30000, "FOOD");

    // Balance should be 1000000 - 50000 - 30000 - 20000 = 900000
    var balanceAfterTransactions = getAccountBalance(sourceAccountId);
    assertEquals(0, new BigDecimal("900000").compareTo(balanceAfterTransactions));

    // Remove only the second transaction
    var response = Unirest.delete(baseUrl + "/v1/transactions/" + transaction2Id)
        .header("Authorization", "Bearer " + token)
        .asString();

    assertEquals(200, response.getStatus());

    // Balance should be 900000 + 30000 = 930000
    var balanceAfterRemoval = getAccountBalance(sourceAccountId);
    assertEquals(0, new BigDecimal("930000").compareTo(balanceAfterRemoval));
  }

  /**
   * <b>Given</b> a large amount transaction exists<br>
   * <b>When</b> the DELETE /transactions/{id} endpoint is called<br>
   * <b>Then</b> the large amount should be correctly reverted
   */
  @Test
  public void removeLargeAmountTransactionShouldRevertCorrectly() {
    // Create account with large balance
    var largeAccountId = createAccount("Large Account", "BANK", 100000000);

    // Create a large transaction
    var transactionId = createTransaction("DEBIT", "Large Purchase", "Big spending", largeAccountId, null, null,
        50000000, "OTHER");

    // Balance should be 100000000 - 50000000 = 50000000
    var balanceAfterTransaction = getAccountBalance(largeAccountId);
    assertEquals(0, new BigDecimal("50000000").compareTo(balanceAfterTransaction));

    // Remove the transaction
    var response = Unirest.delete(baseUrl + "/v1/transactions/" + transactionId)
        .header("Authorization", "Bearer " + token)
        .asString();

    assertEquals(200, response.getStatus());

    // Balance should be restored to 100000000
    var balanceAfterRemoval = getAccountBalance(largeAccountId);
    assertEquals(0, new BigDecimal("100000000").compareTo(balanceAfterRemoval));
  }

  /**
   * <b>Given</b> a transaction is successfully removed<br>
   * <b>When</b> the transaction details are requested<br>
   * <b>Then</b> the transaction should not be found in database
   */
  @Test
  public void removedTransactionShouldNotBeRetrievable() {
    var transactionId = createTransaction("DEBIT", "Shopping", "Grocery", sourceAccountId, null, null, 50000,
        "FOOD");

    // Remove the transaction
    var deleteResponse = Unirest.delete(baseUrl + "/v1/transactions/" + transactionId)
        .header("Authorization", "Bearer " + token)
        .asString();

    assertEquals(200, deleteResponse.getStatus());

    // Try to retrieve the removed transaction - should return 404 (not found in
    // database)
    var getResponse = Unirest.get(baseUrl + "/v1/transactions/" + transactionId)
        .header("Authorization", "Bearer " + token)
        .asString();

    assertEquals(404, getResponse.getStatus());
  }

  /**
   * <b>Given</b> a DEBIT transaction with zero remaining loan balance<br>
   * <b>When</b> the DELETE /transactions/{id} endpoint is called<br>
   * <b>Then</b> the loan remaining amount should be restored from zero
   */
  @Test
  public void removeTransactionShouldRestoreLoanFromZeroBalance() {
    // Create a small loan
    var smallLoanId = createLoan("BORROW", "Jane Doe", "Small Loan", 100000, 5.0);

    // Create a repayment transaction that pays off the entire loan
    var transactionId = createTransaction("DEBIT", "Full Repayment", "Pay off loan", sourceAccountId, null,
        smallLoanId, 100000, "LOAN");

    // Verify loan is fully paid
    var remainingAfterPayment = getLoanRemainingAmount(smallLoanId);
    assertEquals(0, new BigDecimal("0").compareTo(remainingAfterPayment));

    // Remove the transaction
    var response = Unirest.delete(baseUrl + "/v1/transactions/" + transactionId)
        .header("Authorization", "Bearer " + token)
        .asString();

    assertEquals(200, response.getStatus());

    // Verify the loan remaining amount is restored
    var remainingAfterRemoval = getLoanRemainingAmount(smallLoanId);
    assertEquals(0, new BigDecimal("100000").compareTo(remainingAfterRemoval));
  }

  /**
   * <b>Given</b> a successful transaction removal<br>
   * <b>When</b> the DELETE endpoint returns<br>
   * <b>Then</b> it should return the removed transaction details
   */
  @Test
  public void removeTransactionShouldReturnTransactionDetails() {
    var transactionId = createTransaction("DEBIT", "Shopping", "Grocery shopping", sourceAccountId, null, null,
        50000, "FOOD");

    var response = Unirest.delete(baseUrl + "/v1/transactions/" + transactionId)
        .header("Authorization", "Bearer " + token)
        .asString();

    assertEquals(200, response.getStatus());

    // Verify response contains transaction details
    var responseBody = new JSONObject(response.getBody());
    assertNotNull(responseBody.getString("id"));
    assertEquals(transactionId, responseBody.getString("id"));
    assertEquals("Shopping", responseBody.getString("title"));
    assertEquals("DEBIT", responseBody.getString("type"));
    assertEquals(50000, responseBody.getLong("amount"));
  }

  /**
   * <b>Given</b> a transfer transaction with both accounts having exact
   * balances<br>
   * <b>When</b> the DELETE /transactions/{id} endpoint is called<br>
   * <b>Then</b> both accounts should have their exact original balances
   */
  @Test
  public void removeTransferShouldMaintainPrecisionForBothAccounts() {
    // Create accounts with specific balances
    var account1Id = createAccount("Precision Account 1", "BANK", 1234567);
    var account2Id = createAccount("Precision Account 2", "BANK", 7654321);

    // Create a transfer
    var transactionId = createTransaction("TRANSFER", "Precision Transfer", "Testing precision", account1Id,
        account2Id, null, 123456, "OTHER");

    // Verify balances after transfer
    var balance1AfterTransfer = getAccountBalance(account1Id);
    var balance2AfterTransfer = getAccountBalance(account2Id);
    assertEquals(0, new BigDecimal("1111111").compareTo(balance1AfterTransfer));
    assertEquals(0, new BigDecimal("7777777").compareTo(balance2AfterTransfer));

    // Remove the transaction
    var response = Unirest.delete(baseUrl + "/v1/transactions/" + transactionId)
        .header("Authorization", "Bearer " + token)
        .asString();

    assertEquals(200, response.getStatus());

    // Verify exact original balances are restored
    var balance1AfterRemoval = getAccountBalance(account1Id);
    var balance2AfterRemoval = getAccountBalance(account2Id);
    assertEquals(0, new BigDecimal("1234567").compareTo(balance1AfterRemoval));
    assertEquals(0, new BigDecimal("7654321").compareTo(balance2AfterRemoval));
  }

  /**
   * <b>Given</b> a transaction is successfully removed<br>
   * <b>When</b> the user's transactions are listed<br>
   * <b>Then</b> the removed transaction should not appear in the list
   */
  @Test
  public void removedTransactionShouldNotAppearInUserTransactionsList() {
    // Create multiple transactions to ensure list isn't empty
    var transaction1Id = createTransaction("DEBIT", "Shopping 1", "First shopping", sourceAccountId, null, null,
        50000, "FOOD");
    var transaction2Id = createTransaction("DEBIT", "Shopping 2", "Second shopping", sourceAccountId, null, null,
        30000, "FOOD");
    var transaction3Id = createTransaction("CREDIT", "Income", "Payment received", null, destinationAccountId,
        null, 100000, "INCOME");

    // Remove the second transaction
    var deleteResponse = Unirest.delete(baseUrl + "/v1/transactions/" + transaction2Id)
        .header("Authorization", "Bearer " + token)
        .asString();

    assertEquals(200, deleteResponse.getStatus());

    // Get user's transactions list
    var listResponse = Unirest.get(baseUrl + "/v1/users/" + USER_ID + "/transactions")
        .header("Authorization", "Bearer " + token)
        .asString();

    assertEquals(200, listResponse.getStatus());
    var transactions = new org.json.JSONArray(listResponse.getBody());

    // Verify the deleted transaction is not in the list
    boolean foundDeletedTransaction = false;
    boolean foundTransaction1 = false;
    boolean foundTransaction3 = false;

    for (int i = 0; i < transactions.length(); i++) {
      var transaction = transactions.getJSONObject(i);
      var id = transaction.getString("id");

      if (id.equals(transaction2Id)) {
        foundDeletedTransaction = true;
      }
      if (id.equals(transaction1Id)) {
        foundTransaction1 = true;
      }
      if (id.equals(transaction3Id)) {
        foundTransaction3 = true;
      }
    }

    assertEquals(false, foundDeletedTransaction,
        "Deleted transaction should not appear in user's transaction list");
    assertEquals(true, foundTransaction1, "Non-deleted transaction 1 should still exist");
    assertEquals(true, foundTransaction3, "Non-deleted transaction 3 should still exist");
  }

  /**
   * <b>Given</b> a transaction is successfully removed<br>
   * <b>When</b> checking the database<br>
   * <b>Then</b> the transaction should be completely deleted (not soft-deleted)
   */
  @Test
  public void removedTransactionShouldBeCompletelyDeletedFromDatabase() {
    var transactionId = createTransaction("DEBIT", "Shopping", "Grocery shopping", sourceAccountId, null, null,
        50000, "FOOD");

    // Verify transaction exists before deletion
    var getBeforeDelete = Unirest.get(baseUrl + "/v1/transactions/" + transactionId)
        .header("Authorization", "Bearer " + token)
        .asString();
    assertEquals(200, getBeforeDelete.getStatus());

    // Remove the transaction
    var deleteResponse = Unirest.delete(baseUrl + "/v1/transactions/" + transactionId)
        .header("Authorization", "Bearer " + token)
        .asString();
    assertEquals(200, deleteResponse.getStatus());

    // Verify transaction no longer exists - should return 404 (hard delete)
    var getAfterDelete = Unirest.get(baseUrl + "/v1/transactions/" + transactionId)
        .header("Authorization", "Bearer " + token)
        .asString();
    assertEquals(404, getAfterDelete.getStatus());

    // Verify it's not in the user's transaction list either
    var listResponse = Unirest.get(baseUrl + "/v1/users/" + USER_ID + "/transactions")
        .header("Authorization", "Bearer " + token)
        .asString();
    assertEquals(200, listResponse.getStatus());

    var transactions = new org.json.JSONArray(listResponse.getBody());
    for (int i = 0; i < transactions.length(); i++) {
      var transaction = transactions.getJSONObject(i);
      if (transaction.getString("id").equals(transactionId)) {
        throw new AssertionError("Transaction should be completely deleted from database, not just hidden");
      }
    }
  }

  /**
   * <b>Given</b> multiple transactions are removed<br>
   * <b>When</b> checking the database<br>
   * <b>Then</b> all removed transactions should be deleted from the database
   */
  @Test
  public void multipleRemovedTransactionsShouldAllBeDeletedFromDatabase() {
    // Create multiple transactions
    var transaction1Id = createTransaction("DEBIT", "Shopping 1", "First", sourceAccountId, null, null, 10000,
        "FOOD");
    var transaction2Id = createTransaction("DEBIT", "Shopping 2", "Second", sourceAccountId, null, null, 20000,
        "FOOD");
    var transaction3Id = createTransaction("DEBIT", "Shopping 3", "Third", sourceAccountId, null, null, 30000,
        "FOOD");
    var keepTransactionId = createTransaction("CREDIT", "Income", "Keep this", null, destinationAccountId, null,
        100000, "INCOME");

    // Remove three transactions
    var delete1 = Unirest.delete(baseUrl + "/v1/transactions/" + transaction1Id)
        .header("Authorization", "Bearer " + token)
        .asString();
    assertEquals(200, delete1.getStatus());

    var delete2 = Unirest.delete(baseUrl + "/v1/transactions/" + transaction2Id)
        .header("Authorization", "Bearer " + token)
        .asString();
    assertEquals(200, delete2.getStatus());

    var delete3 = Unirest.delete(baseUrl + "/v1/transactions/" + transaction3Id)
        .header("Authorization", "Bearer " + token)
        .asString();
    assertEquals(200, delete3.getStatus());

    // Verify none of the deleted transactions can be retrieved
    assertEquals(404, Unirest.get(baseUrl + "/v1/transactions/" + transaction1Id)
        .header("Authorization", "Bearer " + token).asString().getStatus());
    assertEquals(404, Unirest.get(baseUrl + "/v1/transactions/" + transaction2Id)
        .header("Authorization", "Bearer " + token).asString().getStatus());
    assertEquals(404, Unirest.get(baseUrl + "/v1/transactions/" + transaction3Id)
        .header("Authorization", "Bearer " + token).asString().getStatus());

    // Verify the kept transaction still exists
    assertEquals(200, Unirest.get(baseUrl + "/v1/transactions/" + keepTransactionId)
        .header("Authorization", "Bearer " + token).asString().getStatus());

    // Verify none of the deleted transactions appear in the user's list
    var listResponse = Unirest.get(baseUrl + "/v1/users/" + USER_ID + "/transactions")
        .header("Authorization", "Bearer " + token)
        .asString();
    assertEquals(200, listResponse.getStatus());

    var transactions = new org.json.JSONArray(listResponse.getBody());
    for (int i = 0; i < transactions.length(); i++) {
      var transaction = transactions.getJSONObject(i);
      var id = transaction.getString("id");
      // None of the deleted transaction IDs should appear
      assertEquals(false, id.equals(transaction1Id) || id.equals(transaction2Id) || id.equals(transaction3Id),
          "Deleted transactions should not appear in database");
    }
  }
}
