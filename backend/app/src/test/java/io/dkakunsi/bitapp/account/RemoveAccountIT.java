package io.dkakunsi.bitapp.account;

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

import io.dkakunsi.bitapp.AppLauncher;
import io.dkakunsi.bitapp.jwt.JWTAuthorizer;
import io.dkakunsi.bitapp.test.AppTestUtil;
import io.dkakunsi.bitapp.test.SecureTestUtil;
import kong.unirest.Unirest;

public class RemoveAccountIT extends AppTestUtil {

  private static RemoveAccountIT sut = new RemoveAccountIT();

  private static String baseUrl;

  private static String token;

  private String account1Id;
  private String account2Id;
  private String account3Id;

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
    account1Id = createAccount("Account 1", "BANK");
    account2Id = createAccount("Account 2", "CASH");
    account3Id = createAccount("Account 3", "BANK");
  }

  private String createAccount(String name, String type) {
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
    return new JSONObject(response.getBody()).getString("id");
  }

  private String createLoanWithAccount(String type, String partyName, String title, long amount, String accountId) {
    var body = String.format("""
        {
          "type": "%s",
          "partyName": "%s",
          "title": "%s",
          "description": "Test loan",
          "amount": %d,
          "currency": "IDR",
          "interestRate": 5.0,
          "account": "%s"
        }
        """, type, partyName, title, amount, accountId);

    var response = Unirest.post(baseUrl + "/v1/loans")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(200, response.getStatus());
    return new JSONObject(response.getBody()).getString("id");
  }

  private String createTransaction(String type, String title, String source, String destination, long amount) {
    var bodyBuilder = new StringBuilder("{");
    bodyBuilder.append(String.format("\"type\":\"%s\",", type));
    bodyBuilder.append(String.format("\"title\":\"%s\",", title));
    bodyBuilder.append("\"description\":\"Test transaction\",");

    if (source != null) {
      bodyBuilder.append(String.format("\"source\":\"%s\",", source));
    }
    if (destination != null) {
      bodyBuilder.append(String.format("\"destination\":\"%s\",", destination));
    }

    bodyBuilder.append(String.format("\"amount\":%d,", amount));
    bodyBuilder.append("\"currency\":\"IDR\",");
    bodyBuilder.append("\"category\":\"OTHER\"");
    bodyBuilder.append("}");

    var response = Unirest.post(baseUrl + "/v1/transactions")
        .header("Authorization", "Bearer " + token)
        .body(bodyBuilder.toString())
        .asString();

    assertEquals(200, response.getStatus());
    return new JSONObject(response.getBody()).getString("id");
  }

  /**
   * <b>Given</b> an account exists without any related data<br>
   * <b>When</b> the DELETE /accounts/{id} endpoint is called<br>
   * <b>Then</b> the account should be deleted from the database
   */
  @Test
  public void removeAccountWithoutRelatedDataShouldBeDeleted() {
    var accountId = createAccount("Empty Account", "BANK");

    // Verify account exists
    var getResponse = Unirest.get(baseUrl + "/v1/accounts/" + accountId)
        .header("Authorization", "Bearer " + token)
        .asString();
    assertEquals(200, getResponse.getStatus());

    // Remove the account
    var deleteResponse = Unirest.delete(baseUrl + "/v1/accounts/" + accountId)
        .header("Authorization", "Bearer " + token)
        .asString();
    assertEquals(200, deleteResponse.getStatus());

    // Verify account is deleted from database
    var getAfterDelete = Unirest.get(baseUrl + "/v1/accounts/" + accountId)
        .header("Authorization", "Bearer " + token)
        .asString();
    assertEquals(404, getAfterDelete.getStatus());
  }

  /**
   * <b>Given</b> an account has a related loan<br>
   * <b>When</b> the DELETE /accounts/{id} endpoint is called<br>
   * <b>Then</b> the account and the related loan should be deleted
   */
  @Test
  public void removeAccountWithLoanShouldDeleteBoth() {
    var accountId = createAccount("Loan Account", "BANK");
    var loanId = createLoanWithAccount("BORROW", "Bank ABC", "Car Loan", 5000000, accountId);

    // Verify both exist
    assertEquals(200, Unirest.get(baseUrl + "/v1/accounts/" + accountId)
        .header("Authorization", "Bearer " + token).asString().getStatus());
    assertEquals(200, Unirest.get(baseUrl + "/v1/loans/" + loanId)
        .header("Authorization", "Bearer " + token).asString().getStatus());

    // Remove the account
    var deleteResponse = Unirest.delete(baseUrl + "/v1/accounts/" + accountId)
        .header("Authorization", "Bearer " + token)
        .asString();
    assertEquals(200, deleteResponse.getStatus());

    // Verify both account and loan are deleted
    assertEquals(404, Unirest.get(baseUrl + "/v1/accounts/" + accountId)
        .header("Authorization", "Bearer " + token).asString().getStatus());
    assertEquals(404, Unirest.get(baseUrl + "/v1/loans/" + loanId)
        .header("Authorization", "Bearer " + token).asString().getStatus());
  }

  /**
   * <b>Given</b> an account has multiple related loans<br>
   * <b>When</b> the DELETE /accounts/{id} endpoint is called<br>
   * <b>Then</b> the account and all related loans should be deleted
   */
  @Test
  public void removeAccountWithMultipleLoansShouldDeleteAll() {
    var accountId = createAccount("Multiple Loans Account", "BANK");
    var loan1Id = createLoanWithAccount("BORROW", "Bank A", "Loan 1", 1000000, accountId);
    var loan2Id = createLoanWithAccount("BORROW", "Bank B", "Loan 2", 2000000, accountId);
    var loan3Id = createLoanWithAccount("LEND", "Friend", "Loan 3", 500000, accountId);

    // Remove the account
    var deleteResponse = Unirest.delete(baseUrl + "/v1/accounts/" + accountId)
        .header("Authorization", "Bearer " + token)
        .asString();
    assertEquals(200, deleteResponse.getStatus());

    // Verify account is deleted
    assertEquals(404, Unirest.get(baseUrl + "/v1/accounts/" + accountId)
        .header("Authorization", "Bearer " + token).asString().getStatus());

    // Verify all loans are deleted
    assertEquals(404, Unirest.get(baseUrl + "/v1/loans/" + loan1Id)
        .header("Authorization", "Bearer " + token).asString().getStatus());
    assertEquals(404, Unirest.get(baseUrl + "/v1/loans/" + loan2Id)
        .header("Authorization", "Bearer " + token).asString().getStatus());
    assertEquals(404, Unirest.get(baseUrl + "/v1/loans/" + loan3Id)
        .header("Authorization", "Bearer " + token).asString().getStatus());
  }

  /**
   * <b>Given</b> an account has a DEBIT transaction (source only)<br>
   * <b>When</b> the DELETE /accounts/{id} endpoint is called<br>
   * <b>Then</b> the account and transaction should be deleted
   */
  @Test
  public void removeAccountWithDebitTransactionShouldDeleteBoth() {
    var transactionId = createTransaction("DEBIT", "Shopping", account1Id, null, 50000);

    // Remove the account
    var deleteResponse = Unirest.delete(baseUrl + "/v1/accounts/" + account1Id)
        .header("Authorization", "Bearer " + token)
        .asString();
    assertEquals(200, deleteResponse.getStatus());

    // Verify account is deleted
    assertEquals(404, Unirest.get(baseUrl + "/v1/accounts/" + account1Id)
        .header("Authorization", "Bearer " + token).asString().getStatus());

    // Verify transaction is deleted (no other account relation)
    assertEquals(404, Unirest.get(baseUrl + "/v1/transactions/" + transactionId)
        .header("Authorization", "Bearer " + token).asString().getStatus());
  }

  /**
   * <b>Given</b> an account has a CREDIT transaction (destination only)<br>
   * <b>When</b> the DELETE /accounts/{id} endpoint is called<br>
   * <b>Then</b> the account and transaction should be deleted
   */
  @Test
  public void removeAccountWithCreditTransactionShouldDeleteBoth() {
    var transactionId = createTransaction("CREDIT", "Income", null, account1Id, 100000);

    // Remove the account
    var deleteResponse = Unirest.delete(baseUrl + "/v1/accounts/" + account1Id)
        .header("Authorization", "Bearer " + token)
        .asString();
    assertEquals(200, deleteResponse.getStatus());

    // Verify account is deleted
    assertEquals(404, Unirest.get(baseUrl + "/v1/accounts/" + account1Id)
        .header("Authorization", "Bearer " + token).asString().getStatus());

    // Verify transaction is deleted (no other account relation)
    assertEquals(404, Unirest.get(baseUrl + "/v1/transactions/" + transactionId)
        .header("Authorization", "Bearer " + token).asString().getStatus());
  }

  /**
   * <b>Given</b> an account is the source of a TRANSFER transaction<br>
   * <b>When</b> the DELETE /accounts/{id} endpoint is called<br>
   * <b>Then</b> the account is deleted and the transaction becomes CREDIT
   */
  @Test
  public void removeSourceAccountOfTransferShouldConvertToCredit() {
    var transactionId = createTransaction("TRANSFER", "Transfer Money", account1Id, account2Id, 75000);

    // Verify transaction is TRANSFER before deletion
    var getTransactionBefore = Unirest.get(baseUrl + "/v1/transactions/" + transactionId)
        .header("Authorization", "Bearer " + token)
        .asString();
    assertEquals(200, getTransactionBefore.getStatus());
    var transactionBefore = new JSONObject(getTransactionBefore.getBody());
    assertEquals("TRANSFER", transactionBefore.getString("type"));
    assertEquals(account1Id, transactionBefore.getString("source"));
    assertEquals(account2Id, transactionBefore.getString("destination"));

    // Remove the source account
    var deleteResponse = Unirest.delete(baseUrl + "/v1/accounts/" + account1Id)
        .header("Authorization", "Bearer " + token)
        .asString();
    assertEquals(200, deleteResponse.getStatus());

    // Verify account is deleted
    assertEquals(404, Unirest.get(baseUrl + "/v1/accounts/" + account1Id)
        .header("Authorization", "Bearer " + token).asString().getStatus());

    // Verify transaction still exists but is now CREDIT
    var getTransactionAfter = Unirest.get(baseUrl + "/v1/transactions/" + transactionId)
        .header("Authorization", "Bearer " + token)
        .asString();
    assertEquals(200, getTransactionAfter.getStatus());
    var transactionAfter = new JSONObject(getTransactionAfter.getBody());
    assertEquals("CREDIT", transactionAfter.getString("type"));
    assertTrue(!transactionAfter.has("source") || transactionAfter.isNull("source")
        || transactionAfter.getString("source").isEmpty());
    assertEquals(account2Id, transactionAfter.getString("destination"));
  }

  /**
   * <b>Given</b> an account is the destination of a TRANSFER transaction<br>
   * <b>When</b> the DELETE /accounts/{id} endpoint is called<br>
   * <b>Then</b> the account is deleted and the transaction becomes DEBIT
   */
  @Test
  public void removeDestinationAccountOfTransferShouldConvertToDebit() {
    var transactionId = createTransaction("TRANSFER", "Transfer Money", account1Id, account2Id, 80000);

    // Verify transaction is TRANSFER before deletion
    var getTransactionBefore = Unirest.get(baseUrl + "/v1/transactions/" + transactionId)
        .header("Authorization", "Bearer " + token)
        .asString();
    assertEquals(200, getTransactionBefore.getStatus());
    var transactionBefore = new JSONObject(getTransactionBefore.getBody());
    assertEquals("TRANSFER", transactionBefore.getString("type"));
    assertEquals(account1Id, transactionBefore.getString("source"));
    assertEquals(account2Id, transactionBefore.getString("destination"));

    // Remove the destination account
    var deleteResponse = Unirest.delete(baseUrl + "/v1/accounts/" + account2Id)
        .header("Authorization", "Bearer " + token)
        .asString();
    assertEquals(200, deleteResponse.getStatus());

    // Verify account is deleted
    assertEquals(404, Unirest.get(baseUrl + "/v1/accounts/" + account2Id)
        .header("Authorization", "Bearer " + token).asString().getStatus());

    // Verify transaction still exists but is now DEBIT
    var getTransactionAfter = Unirest.get(baseUrl + "/v1/transactions/" + transactionId)
        .header("Authorization", "Bearer " + token)
        .asString();
    assertEquals(200, getTransactionAfter.getStatus());
    var transactionAfter = new JSONObject(getTransactionAfter.getBody());
    assertEquals("DEBIT", transactionAfter.getString("type"));
    assertEquals(account1Id, transactionAfter.getString("source"));
    assertTrue(!transactionAfter.has("destination") || transactionAfter.isNull("destination")
        || transactionAfter.getString("destination").isEmpty());
  }

  /**
   * <b>Given</b> an account has multiple TRANSFER transactions<br>
   * <b>When</b> the DELETE /accounts/{id} endpoint is called<br>
   * <b>Then</b> all transfers should be converted appropriately
   */
  @Test
  public void removeAccountWithMultipleTransfersShouldConvertAll() {
    // Create transfers where account1 is source
    var transfer1Id = createTransaction("TRANSFER", "Transfer 1", account1Id, account2Id, 10000);
    var transfer2Id = createTransaction("TRANSFER", "Transfer 2", account1Id, account3Id, 20000);

    // Create transfers where account1 is destination
    var transfer3Id = createTransaction("TRANSFER", "Transfer 3", account2Id, account1Id, 15000);
    var transfer4Id = createTransaction("TRANSFER", "Transfer 4", account3Id, account1Id, 25000);

    // Remove account1
    var deleteResponse = Unirest.delete(baseUrl + "/v1/accounts/" + account1Id)
        .header("Authorization", "Bearer " + token)
        .asString();
    assertEquals(200, deleteResponse.getStatus());

    // Verify account is deleted
    assertEquals(404, Unirest.get(baseUrl + "/v1/accounts/" + account1Id)
        .header("Authorization", "Bearer " + token).asString().getStatus());

    // Transfers where account1 was source should become CREDIT
    var transaction1 = new JSONObject(Unirest.get(baseUrl + "/v1/transactions/" + transfer1Id)
        .header("Authorization", "Bearer " + token).asString().getBody());
    assertEquals("CREDIT", transaction1.getString("type"));
    assertEquals(account2Id, transaction1.getString("destination"));

    var transaction2 = new JSONObject(Unirest.get(baseUrl + "/v1/transactions/" + transfer2Id)
        .header("Authorization", "Bearer " + token).asString().getBody());
    assertEquals("CREDIT", transaction2.getString("type"));
    assertEquals(account3Id, transaction2.getString("destination"));

    // Transfers where account1 was destination should become DEBIT
    var transaction3 = new JSONObject(Unirest.get(baseUrl + "/v1/transactions/" + transfer3Id)
        .header("Authorization", "Bearer " + token).asString().getBody());
    assertEquals("DEBIT", transaction3.getString("type"));
    assertEquals(account2Id, transaction3.getString("source"));

    var transaction4 = new JSONObject(Unirest.get(baseUrl + "/v1/transactions/" + transfer4Id)
        .header("Authorization", "Bearer " + token).asString().getBody());
    assertEquals("DEBIT", transaction4.getString("type"));
    assertEquals(account3Id, transaction4.getString("source"));
  }

  /**
   * <b>Given</b> an account has both loans and transactions<br>
   * <b>When</b> the DELETE /accounts/{id} endpoint is called<br>
   * <b>Then</b> all related data should be properly handled
   */
  @Test
  public void removeAccountWithLoansAndTransactionsShouldHandleAll() {
    var accountId = createAccount("Complex Account", "BANK");

    // Create loans
    var loan1Id = createLoanWithAccount("BORROW", "Bank", "Loan 1", 1000000, accountId);
    var loan2Id = createLoanWithAccount("LEND", "Friend", "Loan 2", 500000, accountId);

    // Create transactions - these will be created by loan disbursements and we can
    // add more
    var debitId = createTransaction("DEBIT", "Shopping", accountId, null, 50000);
    var transferId = createTransaction("TRANSFER", "Transfer", accountId, account2Id, 30000);

    // Remove the account
    var deleteResponse = Unirest.delete(baseUrl + "/v1/accounts/" + accountId)
        .header("Authorization", "Bearer " + token)
        .asString();
    assertEquals(200, deleteResponse.getStatus());

    // Verify account is deleted
    assertEquals(404, Unirest.get(baseUrl + "/v1/accounts/" + accountId)
        .header("Authorization", "Bearer " + token).asString().getStatus());

    // Verify loans are deleted
    assertEquals(404, Unirest.get(baseUrl + "/v1/loans/" + loan1Id)
        .header("Authorization", "Bearer " + token).asString().getStatus());
    assertEquals(404, Unirest.get(baseUrl + "/v1/loans/" + loan2Id)
        .header("Authorization", "Bearer " + token).asString().getStatus());

    // Verify debit transaction is deleted (no other account)
    assertEquals(404, Unirest.get(baseUrl + "/v1/transactions/" + debitId)
        .header("Authorization", "Bearer " + token).asString().getStatus());

    // Verify transfer becomes CREDIT (account was source)
    var transfer = new JSONObject(Unirest.get(baseUrl + "/v1/transactions/" + transferId)
        .header("Authorization", "Bearer " + token).asString().getBody());
    assertEquals("CREDIT", transfer.getString("type"));
  }

  /**
   * <b>Given</b> an account that does not exist<br>
   * <b>When</b> the DELETE /accounts/{id} endpoint is called<br>
   * <b>Then</b> the request should fail with status 404
   */
  @Test
  public void removeNonExistentAccountShouldFail() {
    var nonExistentId = "non-existent-account-id";

    var response = Unirest.delete(baseUrl + "/v1/accounts/" + nonExistentId)
        .header("Authorization", "Bearer " + token)
        .asString();

    assertEquals(404, response.getStatus());
  }

  /**
   * <b>Given</b> a request without authorization header<br>
   * <b>When</b> the DELETE /accounts/{id} endpoint is called<br>
   * <b>Then</b> the request should fail with status 401
   */
  @Test
  public void removeAccountWithoutAuthorizationShouldFail() {
    var response = Unirest.delete(baseUrl + "/v1/accounts/" + account1Id)
        .asString();

    assertEquals(401, response.getStatus());

    // Verify the account still exists
    assertEquals(200, Unirest.get(baseUrl + "/v1/accounts/" + account1Id)
        .header("Authorization", "Bearer " + token).asString().getStatus());
  }

  /**
   * <b>Given</b> a request with invalid token<br>
   * <b>When</b> the DELETE /accounts/{id} endpoint is called<br>
   * <b>Then</b> the request should fail with status 401
   */
  @Test
  public void removeAccountWithInvalidTokenShouldFail() {
    var response = Unirest.delete(baseUrl + "/v1/accounts/" + account1Id)
        .header("Authorization", "Bearer invalid-token")
        .asString();

    assertEquals(401, response.getStatus());

    // Verify the account still exists
    assertEquals(200, Unirest.get(baseUrl + "/v1/accounts/" + account1Id)
        .header("Authorization", "Bearer " + token).asString().getStatus());
  }

  /**
   * <b>Given</b> a user attempting to delete another user's account<br>
   * <b>When</b> the DELETE /accounts/{id} endpoint is called<br>
   * <b>Then</b> the request should fail with status 403 or 404
   */
  @Test
  public void removeAnotherUserAccountShouldFail() {
    var otherUserToken = SecureTestUtil.generateToken("otheruser@email.com");

    var response = Unirest.delete(baseUrl + "/v1/accounts/" + account1Id)
        .header("Authorization", "Bearer " + otherUserToken)
        .asString();

    // Should fail with 403 (Forbidden) or 404 (Not Found)
    assertTrue(response.getStatus() == 403 || response.getStatus() == 404);

    // Verify the account still exists
    assertEquals(200, Unirest.get(baseUrl + "/v1/accounts/" + account1Id)
        .header("Authorization", "Bearer " + token).asString().getStatus());
  }

  /**
   * <b>Given</b> an account is successfully removed<br>
   * <b>When</b> the account details are requested<br>
   * <b>Then</b> the account should not be found in database
   */
  @Test
  public void removedAccountShouldNotBeRetrievable() {
    var accountId = createAccount("Test Account", "BANK");

    // Remove the account
    var deleteResponse = Unirest.delete(baseUrl + "/v1/accounts/" + accountId)
        .header("Authorization", "Bearer " + token)
        .asString();
    assertEquals(200, deleteResponse.getStatus());

    // Try to retrieve the removed account - should return 404 (not found in
    // database)
    var getResponse = Unirest.get(baseUrl + "/v1/accounts/" + accountId)
        .header("Authorization", "Bearer " + token)
        .asString();
    assertEquals(404, getResponse.getStatus());
  }

  /**
   * <b>Given</b> an account is successfully removed<br>
   * <b>When</b> the user's accounts are listed<br>
   * <b>Then</b> the removed account should not appear in the list
   */
  @Test
  public void removedAccountShouldNotAppearInUserAccountsList() {
    var accountToRemove = createAccount("Account to Remove", "BANK");

    // Remove the account
    var deleteResponse = Unirest.delete(baseUrl + "/v1/accounts/" + accountToRemove)
        .header("Authorization", "Bearer " + token)
        .asString();
    assertEquals(200, deleteResponse.getStatus());

    // Get user's accounts list
    var listResponse = Unirest.get(baseUrl + "/v1/users/" + USER_ID + "/accounts")
        .header("Authorization", "Bearer " + token)
        .asString();
    assertEquals(200, listResponse.getStatus());

    var accounts = new JSONArray(listResponse.getBody());

    // Verify the deleted account is not in the list
    for (int i = 0; i < accounts.length(); i++) {
      var account = accounts.getJSONObject(i);
      assertFalse(account.getString("id").equals(accountToRemove),
          "Deleted account should not appear in user's account list");
    }

    // Verify other accounts still exist
    boolean foundAccount1 = false;
    boolean foundAccount2 = false;
    boolean foundAccount3 = false;
    for (int i = 0; i < accounts.length(); i++) {
      var account = accounts.getJSONObject(i);
      var id = account.getString("id");
      if (id.equals(account1Id))
        foundAccount1 = true;
      if (id.equals(account2Id))
        foundAccount2 = true;
      if (id.equals(account3Id))
        foundAccount3 = true;
    }
    assertTrue(foundAccount1, "Non-deleted account 1 should still exist");
    assertTrue(foundAccount2, "Non-deleted account 2 should still exist");
    assertTrue(foundAccount3, "Non-deleted account 3 should still exist");
  }

  /**
   * <b>Given</b> an account is successfully removed<br>
   * <b>When</b> checking the database<br>
   * <b>Then</b> the account should be completely deleted (not soft-deleted)
   */
  @Test
  public void removedAccountShouldBeCompletelyDeletedFromDatabase() {
    var accountId = createAccount("Test Account", "BANK");

    // Verify account exists before deletion
    var getBeforeDelete = Unirest.get(baseUrl + "/v1/accounts/" + accountId)
        .header("Authorization", "Bearer " + token)
        .asString();
    assertEquals(200, getBeforeDelete.getStatus());

    // Remove the account
    var deleteResponse = Unirest.delete(baseUrl + "/v1/accounts/" + accountId)
        .header("Authorization", "Bearer " + token)
        .asString();
    assertEquals(200, deleteResponse.getStatus());

    // Verify account no longer exists - should return 404 (hard delete)
    var getAfterDelete = Unirest.get(baseUrl + "/v1/accounts/" + accountId)
        .header("Authorization", "Bearer " + token)
        .asString();
    assertEquals(404, getAfterDelete.getStatus());

    // Verify it's not in the user's account list either
    var listResponse = Unirest.get(baseUrl + "/v1/users/" + USER_ID + "/accounts")
        .header("Authorization", "Bearer " + token)
        .asString();
    assertEquals(200, listResponse.getStatus());

    var accounts = new JSONArray(listResponse.getBody());
    for (int i = 0; i < accounts.length(); i++) {
      var account = accounts.getJSONObject(i);
      if (account.getString("id").equals(accountId)) {
        throw new AssertionError("Account should be completely deleted from database, not just hidden");
      }
    }
  }

  /**
   * <b>Given</b> multiple accounts are removed<br>
   * <b>When</b> checking the database<br>
   * <b>Then</b> all removed accounts should be deleted from the database
   */
  @Test
  public void multipleRemovedAccountsShouldAllBeDeletedFromDatabase() {
    var acc1 = createAccount("Account A", "BANK");
    var acc2 = createAccount("Account B", "CASH");
    var acc3 = createAccount("Account C", "BANK");

    // Remove three accounts
    assertEquals(200, Unirest.delete(baseUrl + "/v1/accounts/" + acc1)
        .header("Authorization", "Bearer " + token).asString().getStatus());
    assertEquals(200, Unirest.delete(baseUrl + "/v1/accounts/" + acc2)
        .header("Authorization", "Bearer " + token).asString().getStatus());
    assertEquals(200, Unirest.delete(baseUrl + "/v1/accounts/" + acc3)
        .header("Authorization", "Bearer " + token).asString().getStatus());

    // Verify none of the deleted accounts can be retrieved
    assertEquals(404, Unirest.get(baseUrl + "/v1/accounts/" + acc1)
        .header("Authorization", "Bearer " + token).asString().getStatus());
    assertEquals(404, Unirest.get(baseUrl + "/v1/accounts/" + acc2)
        .header("Authorization", "Bearer " + token).asString().getStatus());
    assertEquals(404, Unirest.get(baseUrl + "/v1/accounts/" + acc3)
        .header("Authorization", "Bearer " + token).asString().getStatus());

    // Verify none of the deleted accounts appear in the user's list
    var listResponse = Unirest.get(baseUrl + "/v1/users/" + USER_ID + "/accounts")
        .header("Authorization", "Bearer " + token)
        .asString();
    assertEquals(200, listResponse.getStatus());

    var accounts = new JSONArray(listResponse.getBody());
    for (int i = 0; i < accounts.length(); i++) {
      var account = accounts.getJSONObject(i);
      var id = account.getString("id");
      assertFalse(id.equals(acc1) || id.equals(acc2) || id.equals(acc3),
          "Deleted accounts should not appear in database");
    }
  }

  /**
   * <b>Given</b> a successful account removal<br>
   * <b>When</b> the DELETE endpoint returns<br>
   * <b>Then</b> it should return the removed account details
   */
  @Test
  public void removeAccountShouldReturnAccountDetails() {
    var accountId = createAccount("Test Account", "CASH");

    var response = Unirest.delete(baseUrl + "/v1/accounts/" + accountId)
        .header("Authorization", "Bearer " + token)
        .asString();

    assertEquals(200, response.getStatus());

    // Verify response contains account details
    var responseBody = new JSONObject(response.getBody());
    assertNotNull(responseBody.getString("id"));
    assertEquals(accountId, responseBody.getString("id"));
    assertEquals("Test Account", responseBody.getString("name"));
    assertEquals("CASH", responseBody.getString("type"));
  }

  /**
   * <b>Given</b> an account with a mix of transaction types<br>
   * <b>When</b> the DELETE /accounts/{id} endpoint is called<br>
   * <b>Then</b> transactions should be handled correctly based on type
   */
  @Test
  public void removeAccountWithMixedTransactionsShouldHandleCorrectly() {
    var accountId = createAccount("Mixed Transactions Account", "BANK");

    // DEBIT - should be deleted
    var debitId = createTransaction("DEBIT", "Expense", accountId, null, 10000);

    // CREDIT - should be deleted
    var creditId = createTransaction("CREDIT", "Income", null, accountId, 20000);

    // TRANSFER as source - should become CREDIT
    var transferSource = createTransaction("TRANSFER", "Transfer Out", accountId, account2Id, 30000);

    // TRANSFER as destination - should become DEBIT
    var transferDest = createTransaction("TRANSFER", "Transfer In", account3Id, accountId, 40000);

    // Remove the account
    var deleteResponse = Unirest.delete(baseUrl + "/v1/accounts/" + accountId)
        .header("Authorization", "Bearer " + token)
        .asString();
    assertEquals(200, deleteResponse.getStatus());

    // Verify DEBIT and CREDIT are deleted
    assertEquals(404, Unirest.get(baseUrl + "/v1/transactions/" + debitId)
        .header("Authorization", "Bearer " + token).asString().getStatus());
    assertEquals(404, Unirest.get(baseUrl + "/v1/transactions/" + creditId)
        .header("Authorization", "Bearer " + token).asString().getStatus());

    // Verify TRANSFER as source became CREDIT
    var transferSourceAfter = new JSONObject(Unirest.get(baseUrl + "/v1/transactions/" + transferSource)
        .header("Authorization", "Bearer " + token).asString().getBody());
    assertEquals("CREDIT", transferSourceAfter.getString("type"));
    assertEquals(account2Id, transferSourceAfter.getString("destination"));

    // Verify TRANSFER as destination became DEBIT
    var transferDestAfter = new JSONObject(Unirest.get(baseUrl + "/v1/transactions/" + transferDest)
        .header("Authorization", "Bearer " + token).asString().getBody());
    assertEquals("DEBIT", transferDestAfter.getString("type"));
    assertEquals(account3Id, transferDestAfter.getString("source"));
  }
}
