package io.dkakunsi.bitapp.transaction.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.langchain4j.model.chat.ChatModel;
import io.dkakunsi.bitapp.AppLauncher;
import io.dkakunsi.bitapp.jwt.JWTAuthorizer;
import io.dkakunsi.bitapp.test.AppTestUtil;
import io.dkakunsi.bitapp.test.SecureTestUtil;
import kong.unirest.Unirest;

public class GetUserTransactionsIT extends AppTestUtil {

  private static GetUserTransactionsIT sut = new GetUserTransactionsIT();

  private static String baseUrl;

  private static String token;

  private String sourceAccountId;

  private String destinationAccountId;

  private String loanId;

  @BeforeAll
  static void setup() throws Exception {
    AppTestUtil.setTestDependency(ChatModel.class, mock(ChatModel.class));
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

  /**
   * <b>Given</b> a user with multiple transactions<br>
   * <b>When</b> the GET /users/{userId}/transactions endpoint is called<br>
   * <b>Then</b> all user's transactions should be returned with status 200
   */
  @Test
  public void getUserTransactionsShouldReturnAllTransactions() {
    // Create several transactions
    createTransaction("DEBIT", "Grocery Shopping", "Weekly groceries", sourceAccountId, null, null, 50000, "FOOD");
    createTransaction("CREDIT", "Salary", "Monthly salary", null, destinationAccountId, null, 5000000, "SALARY");
    createTransaction("TRANSFER", "Transfer to Savings", "Monthly savings", sourceAccountId, destinationAccountId,
        null, 100000, "OTHER");

    var response = Unirest.get(baseUrl + "/v1/users/" + USER_ID + "/transactions")
        .header("Authorization", "Bearer " + token)
        .asString();

    assertEquals(200, response.getStatus());
    var transactions = new JSONArray(response.getBody());

    // Should have 3 transactions plus 2 initial balance transactions = 5 total
    assertTrue(transactions.length() >= 5, "Expected at least 5 transactions");

    // Verify transactions have required fields
    for (int i = 0; i < transactions.length(); i++) {
      var transaction = transactions.getJSONObject(i);
      assertNotNull(transaction.getString("id"));
      assertNotNull(transaction.getString("type"));
      assertNotNull(transaction.getString("title"));
      assertEquals(USER_ID, transaction.getString("user"));
    }
  }

  /**
   * <b>Given</b> a user with no transactions<br>
   * <b>When</b> the GET /users/{userId}/transactions endpoint is called<br>
   * <b>Then</b> an empty list should be returned with status 200
   */
  @Test
  public void getUserTransactionsShouldReturnEmptyListWhenNoTransactions() {
    // Create a new user token
    var newUserEmail = "newuser@email.com";
    var newUserToken = SecureTestUtil.generateToken(newUserEmail);

    var response = Unirest.get(baseUrl + "/v1/users/" + newUserEmail + "/transactions")
        .header("Authorization", "Bearer " + newUserToken)
        .asString();

    assertEquals(200, response.getStatus());
    var transactions = new JSONArray(response.getBody());
    assertEquals(0, transactions.length());
  }

  /**
   * <b>Given</b> a user with various transaction types<br>
   * <b>When</b> the GET /users/{userId}/transactions endpoint is called<br>
   * <b>Then</b> all transaction types should be returned correctly
   */
  @Test
  public void getUserTransactionsShouldReturnAllTransactionTypes() {
    var debitId = createTransaction("DEBIT", "Shopping", "Grocery shopping", sourceAccountId, null, null, 50000,
        "FOOD");
    var creditId = createTransaction("CREDIT", "Income", "Freelance payment", null, destinationAccountId, null,
        1000000, "SALARY");
    var transferId = createTransaction("TRANSFER", "Transfer", "Move money", sourceAccountId, destinationAccountId,
        null, 200000, "OTHER");

    var response = Unirest.get(baseUrl + "/v1/users/" + USER_ID + "/transactions")
        .header("Authorization", "Bearer " + token)
        .asString();

    assertEquals(200, response.getStatus());
    var transactions = new JSONArray(response.getBody());

    boolean foundDebit = false;
    boolean foundCredit = false;
    boolean foundTransfer = false;

    for (int i = 0; i < transactions.length(); i++) {
      var transaction = transactions.getJSONObject(i);
      var id = transaction.getString("id");

      if (id.equals(debitId)) {
        foundDebit = true;
        assertEquals("DEBIT", transaction.getString("type"));
        assertEquals(sourceAccountId, transaction.getString("source"));
      } else if (id.equals(creditId)) {
        foundCredit = true;
        assertEquals("CREDIT", transaction.getString("type"));
        assertEquals(destinationAccountId, transaction.getString("destination"));
      } else if (id.equals(transferId)) {
        foundTransfer = true;
        assertEquals("TRANSFER", transaction.getString("type"));
        assertEquals(sourceAccountId, transaction.getString("source"));
        assertEquals(destinationAccountId, transaction.getString("destination"));
      }
    }

    assertTrue(foundDebit, "Should find debit transaction");
    assertTrue(foundCredit, "Should find credit transaction");
    assertTrue(foundTransfer, "Should find transfer transaction");
  }

  /**
   * <b>Given</b> a transaction with loan reference<br>
   * <b>When</b> the GET /users/{userId}/transactions endpoint is called<br>
   * <b>Then</b> the loan reference should be included in the response
   */
  @Test
  public void getUserTransactionsShouldIncludeLoanReference() {
    var transactionId = createTransaction("CREDIT", "Loan Disbursement", "Personal loan received", null,
        destinationAccountId, loanId, 2000000, "OTHER");

    var response = Unirest.get(baseUrl + "/v1/users/" + USER_ID + "/transactions")
        .header("Authorization", "Bearer " + token)
        .asString();

    assertEquals(200, response.getStatus());
    var transactions = new JSONArray(response.getBody());

    boolean foundLoanTransaction = false;
    for (int i = 0; i < transactions.length(); i++) {
      var transaction = transactions.getJSONObject(i);
      if (transaction.getString("id").equals(transactionId)) {
        foundLoanTransaction = true;
        assertEquals(loanId, transaction.getString("loan"));
        assertEquals("Loan Disbursement", transaction.getString("title"));
        break;
      }
    }

    assertTrue(foundLoanTransaction, "Should find transaction with loan reference");
  }

  /**
   * <b>Given</b> a request without authorization header<br>
   * <b>When</b> the GET /users/{userId}/transactions endpoint is called<br>
   * <b>Then</b> the request should fail with status 401
   */
  @Test
  public void getUserTransactionsWithoutAuthorizationShouldFail() {
    var response = Unirest.get(baseUrl + "/v1/users/" + USER_ID + "/transactions")
        .asString();

    assertEquals(401, response.getStatus());
  }

  /**
   * <b>Given</b> a request with invalid token<br>
   * <b>When</b> the GET /users/{userId}/transactions endpoint is called<br>
   * <b>Then</b> the request should fail with status 401
   */
  @Test
  public void getUserTransactionsWithInvalidTokenShouldFail() {
    var response = Unirest.get(baseUrl + "/v1/users/" + USER_ID + "/transactions")
        .header("Authorization", "Bearer invalid-token")
        .asString();

    assertEquals(401, response.getStatus());
  }

  /**
   * <b>Given</b> a user requesting another user's transactions<br>
   * <b>When</b> the GET /users/{userId}/transactions endpoint is called<br>
   * <b>Then</b> the request should return only the authenticated user's
   * transactions
   */
  @Test
  public void getUserTransactionsShouldReturnOnlyOwnTransactions() {
    // Create transactions for the authenticated user
    createTransaction("DEBIT", "My Shopping", "My groceries", sourceAccountId, null, null, 50000, "FOOD");

    // Try to get transactions for another user
    var otherUserId = "otheruser@email.com";
    var response = Unirest.get(baseUrl + "/v1/users/" + otherUserId + "/transactions")
        .header("Authorization", "Bearer " + token)
        .asString();

    // Should return empty list or only authenticated user's transactions
    assertEquals(200, response.getStatus());
    var transactions = new JSONArray(response.getBody());

    // All returned transactions should belong to the authenticated user
    for (int i = 0; i < transactions.length(); i++) {
      var transaction = transactions.getJSONObject(i);
      assertEquals(USER_ID, transaction.getString("user"));
    }
  }

  /**
   * <b>Given</b> transactions with different categories<br>
   * <b>When</b> the GET /users/{userId}/transactions endpoint is called<br>
   * <b>Then</b> all categories should be represented correctly
   */
  @Test
  public void getUserTransactionsShouldReturnCorrectCategories() {
    createTransaction("DEBIT", "Groceries", "Food shopping", sourceAccountId, null, null, 50000, "FOOD");
    createTransaction("DEBIT", "Transport", "Bus fare", sourceAccountId, null, null, 10000, "TRANSPORTATION");
    createTransaction("CREDIT", "Salary", "Monthly income", null, destinationAccountId, null, 5000000, "SALARY");

    var response = Unirest.get(baseUrl + "/v1/users/" + USER_ID + "/transactions")
        .header("Authorization", "Bearer " + token)
        .asString();

    assertEquals(200, response.getStatus());
    var transactions = new JSONArray(response.getBody());

    boolean foundFood = false;
    boolean foundTransportation = false;
    boolean foundIncome = false;

    for (int i = 0; i < transactions.length(); i++) {
      var transaction = transactions.getJSONObject(i);
      if (transaction.has("category")) {
        var category = transaction.getString("category");
        if ("FOOD".equals(category)) {
          foundFood = true;
        } else if ("TRANSPORTATION".equals(category)) {
          foundTransportation = true;
        } else if ("SALARY".equals(category)) {
          foundIncome = true;
        }
      }
    }

    assertTrue(foundFood, "Should find FOOD category");
    assertTrue(foundTransportation, "Should find TRANSPORTATION category");
    assertTrue(foundIncome, "Should find SALARY category");
  }

  /**
   * <b>Given</b> transactions with dates and times<br>
   * <b>When</b> the GET /users/{userId}/transactions endpoint is called<br>
   * <b>Then</b> date and time information should be included
   */
  @Test
  public void getUserTransactionsShouldIncludeDateAndTime() {
    createTransaction("DEBIT", "Shopping", "Daily shopping", sourceAccountId, null, null, 50000, "FOOD");

    var response = Unirest.get(baseUrl + "/v1/users/" + USER_ID + "/transactions")
        .header("Authorization", "Bearer " + token)
        .asString();

    assertEquals(200, response.getStatus());
    var transactions = new JSONArray(response.getBody());

    assertTrue(transactions.length() > 0, "Should have at least one transaction");

    // Verify the first transaction has date/time fields
    for (int i = 0; i < transactions.length(); i++) {
      var transaction = transactions.getJSONObject(i);
      // Date and time might be optional in some cases, but check they exist
      assertNotNull(transaction.getLong("date"));
      assertNotNull(transaction.getInt("time"));
      // At minimum, verify the structure is correct
      assertNotNull(transaction.getString("id"));
      assertTrue(transaction.has("type"));
      assertTrue(transaction.has("title"));
    }
  }

  /**
   * <b>Given</b> multiple transactions with amounts<br>
   * <b>When</b> the GET /users/{userId}/transactions endpoint is called<br>
   * <b>Then</b> all amounts should be correct and in the proper currency
   */
  @Test
  public void getUserTransactionsShouldReturnCorrectAmounts() {
    var smallAmount = 1000L;
    var mediumAmount = 50000L;
    var largeAmount = 5000000L;

    var smallId = createTransaction("DEBIT", "Coffee", "Morning coffee", sourceAccountId, null, null, smallAmount,
        "FOOD");
    var mediumId = createTransaction("DEBIT", "Groceries", "Weekly groceries", sourceAccountId, null, null,
        mediumAmount, "FOOD");
    var largeId = createTransaction("CREDIT", "Salary", "Monthly salary", null, destinationAccountId, null,
        largeAmount, "SALARY");

    var response = Unirest.get(baseUrl + "/v1/users/" + USER_ID + "/transactions")
        .header("Authorization", "Bearer " + token)
        .asString();

    assertEquals(200, response.getStatus());
    var transactions = new JSONArray(response.getBody());

    boolean foundSmall = false;
    boolean foundMedium = false;
    boolean foundLarge = false;

    for (int i = 0; i < transactions.length(); i++) {
      var transaction = transactions.getJSONObject(i);
      var id = transaction.getString("id");

      if (id.equals(smallId)) {
        foundSmall = true;
        assertEquals(smallAmount, transaction.getLong("amount"));
        assertEquals("IDR", transaction.getString("currency"));
      } else if (id.equals(mediumId)) {
        foundMedium = true;
        assertEquals(mediumAmount, transaction.getLong("amount"));
        assertEquals("IDR", transaction.getString("currency"));
      } else if (id.equals(largeId)) {
        foundLarge = true;
        assertEquals(largeAmount, transaction.getLong("amount"));
        assertEquals("IDR", transaction.getString("currency"));
      }
    }

    assertTrue(foundSmall, "Should find small amount transaction");
    assertTrue(foundMedium, "Should find medium amount transaction");
    assertTrue(foundLarge, "Should find large amount transaction");
  }
}
