package io.dkakunsi.bitapp.transaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

public class GetLoanTransactionsIT extends AppTestUtil {

  private static GetLoanTransactionsIT sut = new GetLoanTransactionsIT();

  private static String baseUrl;

  private static String token;

  private String loanId;

  private String accountId;

  private String destinationAccountId;

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
    loanId = createLoan("BORROW", "John Doe", "Personal Loan", 2000000, 5.0);
    accountId = createAccount("Main Account", "BANK", 1000000);
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

    var response = Unirest.post(baseUrl + "/v1/accounts")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(200, response.getStatus());
    var newAccountId = new JSONObject(response.getBody()).getString("id");

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
          """, newAccountId, initialBalance);

      var depositResponse = Unirest.post(baseUrl + "/v1/transactions")
          .header("Authorization", "Bearer " + token)
          .body(depositBody)
          .asString();

      assertEquals(200, depositResponse.getStatus());
    }

    return newAccountId;
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

  /**
   * <b>Given</b> a loan with multiple transactions<br>
   * <b>When</b> the GET /loans/{loanId}/transactions endpoint is called<br>
   * <b>Then</b> all loan's transactions should be returned with status 200
   */
  @Test
  public void getLoanTransactionsShouldReturnAllTransactions() {
    // Create several transactions related to the loan
    createTransaction("CREDIT", "Loan Disbursement", "Initial loan disbursement", null, accountId, loanId, 2000000,
        "OTHER");
    createTransaction("DEBIT", "Loan Repayment", "Monthly loan repayment", accountId, null, loanId, 100000, "OTHER");
    createTransaction("DEBIT", "Loan Interest Payment", "Monthly interest payment", accountId, null, loanId, 50000,
        "OTHER");

    var response = Unirest.get(baseUrl + "/v1/loans/" + loanId + "/transactions")
        .header("Authorization", "Bearer " + token)
        .asString();

    assertEquals(200, response.getStatus());
    var transactions = new JSONArray(response.getBody());

    // Should have at least 3 transactions
    assertTrue(transactions.length() >= 3, "Expected at least 3 transactions");

    // Verify all transactions are related to the loan
    for (int i = 0; i < transactions.length(); i++) {
      var transaction = transactions.getJSONObject(i);
      assertNotNull(transaction.getString("id"));
      assertNotNull(transaction.getString("type"));
      assertNotNull(transaction.getString("title"));
      assertEquals(loanId, transaction.getString("loan"));
    }
  }

  /**
   * <b>Given</b> a loan with no transactions<br>
   * <b>When</b> the GET /loans/{loanId}/transactions endpoint is called<br>
   * <b>Then</b> an empty list should be returned with status 200
   */
  @Test
  public void getLoanTransactionsShouldReturnEmptyListWhenNoTransactions() {
    var emptyLoanId = createLoan("BORROW", "Jane Doe", "New Loan", 1000000, 4.5);

    var response = Unirest.get(baseUrl + "/v1/loans/" + emptyLoanId + "/transactions")
        .header("Authorization", "Bearer " + token)
        .asString();

    assertEquals(200, response.getStatus());
    var transactions = new JSONArray(response.getBody());
    assertEquals(0, transactions.length());
  }

  /**
   * <b>Given</b> a loan with various transaction types<br>
   * <b>When</b> the GET /loans/{loanId}/transactions endpoint is called<br>
   * <b>Then</b> all transaction types should be returned correctly
   */
  @Test
  public void getLoanTransactionsShouldReturnAllTransactionTypes() {
    var disbursementId = createTransaction("CREDIT", "Loan Disbursement", "Initial disbursement", null, accountId,
        loanId, 2000000, "OTHER");
    var repaymentId = createTransaction("DEBIT", "Loan Repayment", "Full repayment", accountId, null, loanId, 2000000,
        "OTHER");
    var interestId = createTransaction("DEBIT", "Interest Payment", "Interest payment", accountId, null, loanId, 100000,
        "OTHER");

    var response = Unirest.get(baseUrl + "/v1/loans/" + loanId + "/transactions")
        .header("Authorization", "Bearer " + token)
        .asString();

    assertEquals(200, response.getStatus());
    var transactions = new JSONArray(response.getBody());

    boolean foundDisbursement = false;
    boolean foundRepayment = false;
    boolean foundInterest = false;

    for (int i = 0; i < transactions.length(); i++) {
      var transaction = transactions.getJSONObject(i);
      var id = transaction.getString("id");

      if (id.equals(disbursementId)) {
        foundDisbursement = true;
        assertEquals("CREDIT", transaction.getString("type"));
        assertEquals("Loan Disbursement", transaction.getString("title"));
      } else if (id.equals(repaymentId)) {
        foundRepayment = true;
        assertEquals("DEBIT", transaction.getString("type"));
        assertEquals("Loan Repayment", transaction.getString("title"));
      } else if (id.equals(interestId)) {
        foundInterest = true;
        assertEquals("DEBIT", transaction.getString("type"));
        assertEquals("Interest Payment", transaction.getString("title"));
      }
    }

    assertTrue(foundDisbursement, "Should find loan disbursement transaction");
    assertTrue(foundRepayment, "Should find loan repayment transaction");
    assertTrue(foundInterest, "Should find loan interest payment transaction");
  }

  /**
   * <b>Given</b> a loan with transactions from different accounts<br>
   * <b>When</b> the GET /loans/{loanId}/transactions endpoint is called<br>
   * <b>Then</b> all transactions should be returned regardless of
   * source/destination account
   */
  @Test
  public void getLoanTransactionsShouldReturnTransactionsFromMultipleAccounts() {
    var account2Id = createAccount("Second Account", "BANK", 500000);

    createTransaction("CREDIT", "Disbursement to Account 1", "Loan disbursement", null, accountId, loanId, 1000000,
        "OTHER");
    createTransaction("DEBIT", "Repayment from Account 1", "Monthly repayment", accountId, null, loanId, 100000,
        "OTHER");
    createTransaction("DEBIT", "Repayment from Account 2", "Month 2 repayment", account2Id, null, loanId, 100000,
        "OTHER");

    var response = Unirest.get(baseUrl + "/v1/loans/" + loanId + "/transactions")
        .header("Authorization", "Bearer " + token)
        .asString();

    assertEquals(200, response.getStatus());
    var transactions = new JSONArray(response.getBody());

    assertTrue(transactions.length() >= 3, "Should have at least 3 transactions");
  }

  /**
   * <b>Given</b> a request without authorization header<br>
   * <b>When</b> the GET /loans/{loanId}/transactions endpoint is called<br>
   * <b>Then</b> the request should fail with status 401
   */
  @Test
  public void getLoanTransactionsWithoutAuthorizationShouldFail() {
    var response = Unirest.get(baseUrl + "/v1/loans/" + loanId + "/transactions")
        .asString();

    assertEquals(401, response.getStatus());
  }

  /**
   * <b>Given</b> a request with invalid token<br>
   * <b>When</b> the GET /loans/{loanId}/transactions endpoint is called<br>
   * <b>Then</b> the request should fail with status 401
   */
  @Test
  public void getLoanTransactionsWithInvalidTokenShouldFail() {
    var response = Unirest.get(baseUrl + "/v1/loans/" + loanId + "/transactions")
        .header("Authorization", "Bearer invalid-token")
        .asString();

    assertEquals(401, response.getStatus());
  }

  /**
   * <b>Given</b> transactions with dates and times for a loan<br>
   * <b>When</b> the GET /loans/{loanId}/transactions endpoint is called<br>
   * <b>Then</b> date and time information should be included
   */
  @Test
  public void getLoanTransactionsShouldIncludeDateAndTime() {
    createTransaction("CREDIT", "Loan Disbursement", "Initial disbursement", null, accountId, loanId, 2000000, "OTHER");

    var response = Unirest.get(baseUrl + "/v1/loans/" + loanId + "/transactions")
        .header("Authorization", "Bearer " + token)
        .asString();

    assertEquals(200, response.getStatus());
    var transactions = new JSONArray(response.getBody());

    assertTrue(transactions.length() > 0, "Should have at least one transaction");

    // Verify transactions have date/time fields
    for (int i = 0; i < transactions.length(); i++) {
      var transaction = transactions.getJSONObject(i);
      assertNotNull(transaction.getString("date"));
      assertNotNull(transaction.getString("time"));
      assertNotNull(transaction.getString("id"));
      assertTrue(transaction.has("type"));
      assertTrue(transaction.has("title"));
    }
  }

  /**
   * <b>Given</b> multiple transactions with amounts for a loan<br>
   * <b>When</b> the GET /loans/{loanId}/transactions endpoint is called<br>
   * <b>Then</b> all amounts should be correct and in the proper currency
   */
  @Test
  public void getLoanTransactionsShouldReturnCorrectAmounts() {
    var disbursementAmount = 2000000L;
    var repayment1Amount = 100000L;
    var repayment2Amount = 50000L;

    var disbursementId = createTransaction("CREDIT", "Loan Disbursement", "Initial disbursement", null, accountId,
        loanId, disbursementAmount, "OTHER");
    var repayment1Id = createTransaction("DEBIT", "Repayment 1", "First repayment", accountId, null, loanId,
        repayment1Amount, "OTHER");
    var repayment2Id = createTransaction("DEBIT", "Repayment 2", "Second repayment", accountId, null, loanId,
        repayment2Amount, "OTHER");

    var response = Unirest.get(baseUrl + "/v1/loans/" + loanId + "/transactions")
        .header("Authorization", "Bearer " + token)
        .asString();

    assertEquals(200, response.getStatus());
    var transactions = new JSONArray(response.getBody());

    boolean foundDisbursement = false;
    boolean foundRepayment1 = false;
    boolean foundRepayment2 = false;

    for (int i = 0; i < transactions.length(); i++) {
      var transaction = transactions.getJSONObject(i);
      var id = transaction.getString("id");

      if (id.equals(disbursementId)) {
        foundDisbursement = true;
        assertEquals(disbursementAmount, transaction.getLong("amount"));
        assertEquals("IDR", transaction.getString("currency"));
      } else if (id.equals(repayment1Id)) {
        foundRepayment1 = true;
        assertEquals(repayment1Amount, transaction.getLong("amount"));
        assertEquals("IDR", transaction.getString("currency"));
      } else if (id.equals(repayment2Id)) {
        foundRepayment2 = true;
        assertEquals(repayment2Amount, transaction.getLong("amount"));
        assertEquals("IDR", transaction.getString("currency"));
      }
    }

    assertTrue(foundDisbursement, "Should find loan disbursement transaction");
    assertTrue(foundRepayment1, "Should find first repayment transaction");
    assertTrue(foundRepayment2, "Should find second repayment transaction");
  }

  /**
   * <b>Given</b> a loan with transactions tracking account changes<br>
   * <b>When</b> the GET /loans/{loanId}/transactions endpoint is called<br>
   * <b>Then</b> all transactions should properly reflect loan association
   */
  @Test
  public void getLoanTransactionsShouldTrackLoanAssociation() {
    createTransaction("CREDIT", "Loan Disbursement", "Initial disbursement", null, accountId, loanId, 2000000, "OTHER");
    createTransaction("DEBIT", "Monthly Repayment", "Loan repayment", accountId, null, loanId, 100000, "OTHER");

    var response = Unirest.get(baseUrl + "/v1/loans/" + loanId + "/transactions")
        .header("Authorization", "Bearer " + token)
        .asString();

    assertEquals(200, response.getStatus());
    var transactions = new JSONArray(response.getBody());

    // All transactions for this loan should have the loan ID
    for (int i = 0; i < transactions.length(); i++) {
      var transaction = transactions.getJSONObject(i);
      assertEquals(loanId, transaction.getString("loan"),
          "All transactions should be associated with the correct loan");
    }
  }
}
