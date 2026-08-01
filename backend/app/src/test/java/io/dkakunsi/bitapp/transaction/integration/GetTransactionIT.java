package io.dkakunsi.bitapp.transaction.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Map;

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

public class GetTransactionIT extends AppTestUtil {

  private static GetTransactionIT sut = new GetTransactionIT();

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

  /**
   * <b>Given</b> a DEBIT transaction exists in the system<br>
   * <b>When</b> the GET /transactions/{id} endpoint is called with the
   * transaction ID<br>
   * <b>Then</b> the transaction's complete details should be returned with status
   * 200
   */
  @Test
  public void getExistingDebitTransactionShouldBeOk() {
    // Given - Create a DEBIT transaction
    var createBody = String.format("""
        {
          "type": "DEBIT",
          "title": "Grocery Shopping",
          "description": "Weekly groceries",
          "source": "%s",
          "amount": 150000,
          "currency": "IDR",
          "category": "FOOD"
        }
        """, sourceAccountId);

    var postResponse = Unirest.post(baseUrl + "/v1/transactions")
        .header("Authorization", "Bearer " + token)
        .body(createBody)
        .asString();
    assertEquals(200, postResponse.getStatus());
    var transactionId = new JSONObject(postResponse.getBody()).getString("id");

    // When - Get the transaction by ID
    var getResponse = Unirest.get(baseUrl + "/v1/transactions/" + transactionId)
        .header("Authorization", "Bearer " + token)
        .asString();

    // Then
    assertEquals(200, getResponse.getStatus());
    var responseBody = new JSONObject(getResponse.getBody());
    assertEquals(transactionId, responseBody.getString("id"));
    assertEquals(USER_ID, responseBody.getString("user"));
    assertEquals("DEBIT", responseBody.getString("type"));
    assertEquals("Grocery Shopping", responseBody.getString("title"));
    assertEquals("Weekly groceries", responseBody.getString("description"));
    assertEquals(sourceAccountId, responseBody.getString("source"));
    assertEquals(150000, responseBody.getBigDecimal("amount").intValue());
    assertEquals("IDR", responseBody.getString("currency"));
    assertEquals("FOOD", responseBody.getString("category"));
    assertNotNull(responseBody.getLong("date"));
    assertNotNull(responseBody.getInt("time"));
  }

  /**
   * <b>Given</b> a CREDIT transaction exists in the system<br>
   * <b>When</b> the GET /transactions/{id} endpoint is called with the
   * transaction ID<br>
   * <b>Then</b> the transaction's complete details should be returned with status
   * 200
   */
  @Test
  public void getExistingCreditTransactionShouldBeOk() {
    // Given - Create a CREDIT transaction
    var createBody = String.format("""
        {
          "type": "CREDIT",
          "title": "Salary Payment",
          "description": "Monthly salary",
          "destination": "%s",
          "amount": 5000000,
          "currency": "IDR",
          "category": "SALARY"
        }
        """, destinationAccountId);

    var postResponse = Unirest.post(baseUrl + "/v1/transactions")
        .header("Authorization", "Bearer " + token)
        .body(createBody)
        .asString();
    assertEquals(200, postResponse.getStatus());
    var transactionId = new JSONObject(postResponse.getBody()).getString("id");

    // When - Get the transaction by ID
    var getResponse = Unirest.get(baseUrl + "/v1/transactions/" + transactionId)
        .header("Authorization", "Bearer " + token)
        .asString();

    // Then
    assertEquals(200, getResponse.getStatus());
    var responseBody = new JSONObject(getResponse.getBody());
    assertEquals(transactionId, responseBody.getString("id"));
    assertEquals(USER_ID, responseBody.getString("user"));
    assertEquals("CREDIT", responseBody.getString("type"));
    assertEquals("Salary Payment", responseBody.getString("title"));
    assertEquals("Monthly salary", responseBody.getString("description"));
    assertEquals(destinationAccountId, responseBody.getString("destination"));
    assertEquals(5000000, responseBody.getBigDecimal("amount").intValue());
    assertEquals("IDR", responseBody.getString("currency"));
    assertEquals("SALARY", responseBody.getString("category"));
    assertNotNull(responseBody.getLong("date"));
    assertNotNull(responseBody.getInt("time"));
  }

  /**
   * <b>Given</b> a TRANSFER transaction exists in the system<br>
   * <b>When</b> the GET /transactions/{id} endpoint is called with the
   * transaction ID<br>
   * <b>Then</b> the transaction's complete details should be returned with status
   * 200
   */
  @Test
  public void getExistingTransferTransactionShouldBeOk() {
    // Given - Create a TRANSFER transaction
    var createBody = String.format("""
        {
          "type": "TRANSFER",
          "title": "Internal Transfer",
          "description": "Moving funds between accounts",
          "source": "%s",
          "destination": "%s",
          "amount": 200000,
          "currency": "IDR"
        }
        """, sourceAccountId, destinationAccountId);

    var postResponse = Unirest.post(baseUrl + "/v1/transactions")
        .header("Authorization", "Bearer " + token)
        .body(createBody)
        .asString();
    assertEquals(200, postResponse.getStatus());
    var transactionId = new JSONObject(postResponse.getBody()).getString("id");

    // When - Get the transaction by ID
    var getResponse = Unirest.get(baseUrl + "/v1/transactions/" + transactionId)
        .header("Authorization", "Bearer " + token)
        .asString();

    // Then
    assertEquals(200, getResponse.getStatus());
    var responseBody = new JSONObject(getResponse.getBody());
    assertEquals(transactionId, responseBody.getString("id"));
    assertEquals(USER_ID, responseBody.getString("user"));
    assertEquals("TRANSFER", responseBody.getString("type"));
    assertEquals("Internal Transfer", responseBody.getString("title"));
    assertEquals("Moving funds between accounts", responseBody.getString("description"));
    assertEquals(sourceAccountId, responseBody.getString("source"));
    assertEquals(destinationAccountId, responseBody.getString("destination"));
    assertEquals(200000, responseBody.getBigDecimal("amount").intValue());
    assertEquals("IDR", responseBody.getString("currency"));
    assertNotNull(responseBody.getLong("date"));
    assertNotNull(responseBody.getInt("time"));
  }

  /**
   * <b>Given</b> a transaction with a loan reference exists in the system<br>
   * <b>When</b> the GET /transactions/{id} endpoint is called with the
   * transaction ID<br>
   * <b>Then</b> the transaction's complete details including loan reference
   * should
   * be returned with status 200
   */
  @Test
  public void getTransactionWithLoanShouldBeOk() {
    // Given - Create a DEBIT transaction with loan reference
    var createBody = String.format("""
        {
          "type": "DEBIT",
          "title": "Loan Payment",
          "description": "Monthly loan payment",
          "source": "%s",
          "loan": "%s",
          "amount": 100000,
          "currency": "IDR",
          "category": "LOAN"
        }
        """, sourceAccountId, loanId);

    var postResponse = Unirest.post(baseUrl + "/v1/transactions")
        .header("Authorization", "Bearer " + token)
        .body(createBody)
        .asString();
    assertEquals(200, postResponse.getStatus());
    var transactionId = new JSONObject(postResponse.getBody()).getString("id");

    // When - Get the transaction by ID
    var getResponse = Unirest.get(baseUrl + "/v1/transactions/" + transactionId)
        .header("Authorization", "Bearer " + token)
        .asString();

    // Then
    assertEquals(200, getResponse.getStatus());
    var responseBody = new JSONObject(getResponse.getBody());
    assertEquals(transactionId, responseBody.getString("id"));
    assertEquals(USER_ID, responseBody.getString("user"));
    assertEquals("DEBIT", responseBody.getString("type"));
    assertEquals("Loan Payment", responseBody.getString("title"));
    assertEquals("Monthly loan payment", responseBody.getString("description"));
    assertEquals(sourceAccountId, responseBody.getString("source"));
    assertEquals(loanId, responseBody.getString("loan"));
    assertEquals(100000, responseBody.getBigDecimal("amount").intValue());
    assertEquals("IDR", responseBody.getString("currency"));
    assertEquals("LOAN", responseBody.getString("category"));
  }

  /**
   * <b>Given</b> a transaction ID that does not exist in the system<br>
   * <b>When</b> the GET /transactions/{id} endpoint is called<br>
   * <b>Then</b> a 404 status should be returned with an error message
   */
  @Test
  public void getNonExistentTransactionShouldReturn404() {
    // Given
    var nonExistentId = "non-existent-transaction-id";

    // When
    var response = Unirest.get(baseUrl + "/v1/transactions/" + nonExistentId)
        .header("Authorization", "Bearer " + token)
        .asString();

    // Then
    assertEquals(404, response.getStatus());
  }

  /**
   * <b>Given</b> a request without an Authorization header<br>
   * <b>When</b> the GET /transactions/{id} endpoint is called<br>
   * <b>Then</b> the request should be rejected with status 401 (Unauthorized)
   */
  @Test
  public void getTransactionWithoutAuthorizationShouldReturn401() {
    // Given - Create a transaction first
    var createBody = String.format("""
        {
          "type": "DEBIT",
          "title": "Test Transaction",
          "description": "Test description",
          "source": "%s",
          "amount": 10000,
          "currency": "IDR"
        }
        """, sourceAccountId);

    var postResponse = Unirest.post(baseUrl + "/v1/transactions")
        .header("Authorization", "Bearer " + token)
        .body(createBody)
        .asString();
    assertEquals(200, postResponse.getStatus());
    var transactionId = new JSONObject(postResponse.getBody()).getString("id");

    // When - Try to get without authorization
    var response = Unirest.get(baseUrl + "/v1/transactions/" + transactionId)
        .asString();

    // Then
    assertEquals(401, response.getStatus());
  }

  /**
   * <b>Given</b> a valid token for a different user<br>
   * <b>When</b> the GET /transactions/{id} endpoint is called for another user's
   * transaction<br>
   * <b>Then</b> the request should be rejected with status 403 (Forbidden) or 404
   * (Not Found)
   */
  @Test
  public void getAnotherUserTransactionShouldBeForbidden() {
    // Given - Create a transaction for USER_ID
    var createBody = String.format("""
        {
          "type": "DEBIT",
          "title": "Private Transaction",
          "description": "Should not be accessible by other users",
          "source": "%s",
          "amount": 50000,
          "currency": "IDR"
        }
        """, sourceAccountId);

    var postResponse = Unirest.post(baseUrl + "/v1/transactions")
        .header("Authorization", "Bearer " + token)
        .body(createBody)
        .asString();
    assertEquals(200, postResponse.getStatus());
    var transactionId = new JSONObject(postResponse.getBody()).getString("id");

    // When - Try to get with a different user's token
    var otherUserId = "otheruser@email.com";
    var otherToken = SecureTestUtil.generateToken(otherUserId);

    var response = Unirest.get(baseUrl + "/v1/transactions/" + transactionId)
        .header("Authorization", "Bearer " + otherToken)
        .asString();

    // Then - Should be forbidden or not found
    assertTrue(response.getStatus() == 403 || response.getStatus() == 404,
        "Expected 403 or 404 but got " + response.getStatus());
  }

  /**
   * <b>Given</b> a transaction with an invalid token<br>
   * <b>When</b> the GET /transactions/{id} endpoint is called<br>
   * <b>Then</b> the request should be rejected with status 401 (Unauthorized)
   */
  @Test
  public void getTransactionWithInvalidTokenShouldReturn401() {
    // Given - Create a transaction first
    var createBody = String.format("""
        {
          "type": "DEBIT",
          "title": "Test Transaction",
          "description": "Test description",
          "source": "%s",
          "amount": 10000,
          "currency": "IDR"
        }
        """, sourceAccountId);

    var postResponse = Unirest.post(baseUrl + "/v1/transactions")
        .header("Authorization", "Bearer " + token)
        .body(createBody)
        .asString();
    assertEquals(200, postResponse.getStatus());
    var transactionId = new JSONObject(postResponse.getBody()).getString("id");

    // When - Try to get with invalid token
    var response = Unirest.get(baseUrl + "/v1/transactions/" + transactionId)
        .header("Authorization", "Bearer invalid-token")
        .asString();

    // Then
    assertEquals(401, response.getStatus());
  }
}
