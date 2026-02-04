package io.dkakunsi.bitapp.transaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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

public class CreateTransactionIT extends AppTestUtil {

  private static final int port = 20010;

  private static CreateTransactionIT sut = new CreateTransactionIT();

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

  private String createLoan(String type, String partyName, String title, long amount, double interestRate,
      String accountId) {
    var body = String.format("""
        {
          "type": "%s",
          "partyName": "%s",
          "title": "%s",
          "description": "Test loan",
          "amount": %d,
          "currency": "IDR",
          "interestRate": %.1f,
          "account": "%s"
        }
        """, type, partyName, title, amount, interestRate, accountId);

    var response = Unirest.post(baseUrl + "/loans")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(200, response.getStatus());
    return new JSONObject(response.getBody()).getString("id");
  }

  /**
   * <b>Given</b> a valid DEBIT transaction creation request<br>
   * <b>When</b> the POST /transactions endpoint is called<br>
   * <b>Then</b> a new transaction should be created with status 200 and source
   * account balance decreased
   */
  @Test
  public void createDebitTransactionShouldBeOk() {
    var body = String.format("""
        {
          "type": "DEBIT",
          "title": "Grocery Shopping",
          "description": "Weekly groceries at supermarket",
          "date": "2024-06-15",
          "time": "14:30:00",
          "source": "%s",
          "amount": 50000,
          "currency": "IDR",
          "category": "FOOD"
        }
        """, sourceAccountId);

    var response = Unirest.post(baseUrl + "/transactions")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(200, response.getStatus());
    var responseBody = new JSONObject(response.getBody());
    assertNotNull(responseBody.getString("id"));
    assertEquals("DEBIT", responseBody.getString("type"));
    assertEquals("Grocery Shopping", responseBody.getString("title"));
    assertEquals("Weekly groceries at supermarket", responseBody.getString("description"));
    assertEquals("2024-06-15", responseBody.getString("date"));
    assertEquals("14:30", responseBody.getString("time"));
    assertEquals(sourceAccountId, responseBody.getString("source"));
    assertEquals(50000, responseBody.getLong("amount"));
    assertEquals("IDR", responseBody.getString("currency"));
    assertEquals("FOOD", responseBody.getString("category"));
    assertEquals(USER_ID, responseBody.getString("user"));

    var accountResponse = Unirest.get(baseUrl + "/accounts/" + sourceAccountId)
        .header("Authorization", "Bearer " + token)
        .asString();
    assertEquals(200, accountResponse.getStatus());
    var account = new JSONObject(accountResponse.getBody());
    assertEquals(950000.0, account.getBigDecimal("balance").doubleValue(), 0.01);
  }

  /**
   * <b>Given</b> a valid CREDIT transaction creation request<br>
   * <b>When</b> the POST /transactions endpoint is called<br>
   * <b>Then</b> a new transaction should be created with status 200 and
   * destination account balance increased
   */
  @Test
  public void createCreditTransactionShouldBeOk() {
    var body = String.format("""
        {
          "type": "CREDIT",
          "title": "Salary Payment",
          "description": "Monthly salary",
          "date": "2024-06-01",
          "time": "09:00:00",
          "destination": "%s",
          "amount": 5000000,
          "currency": "IDR",
          "category": "INCOME"
        }
        """, destinationAccountId);

    var response = Unirest.post(baseUrl + "/transactions")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(200, response.getStatus());
    var responseBody = new JSONObject(response.getBody());
    assertNotNull(responseBody.getString("id"));
    assertEquals("CREDIT", responseBody.getString("type"));
    assertEquals("Salary Payment", responseBody.getString("title"));
    assertEquals("Monthly salary", responseBody.getString("description"));
    assertEquals("2024-06-01", responseBody.getString("date"));
    assertEquals("09:00", responseBody.getString("time"));
    assertEquals(destinationAccountId, responseBody.getString("destination"));
    assertEquals(5000000, responseBody.getLong("amount"));
    assertEquals("IDR", responseBody.getString("currency"));
    assertEquals("INCOME", responseBody.getString("category"));
    assertEquals(USER_ID, responseBody.getString("user"));

    var accountResponse = Unirest.get(baseUrl + "/accounts/" + destinationAccountId)
        .header("Authorization", "Bearer " + token)
        .asString();
    assertEquals(200, accountResponse.getStatus());
    var account = new JSONObject(accountResponse.getBody());
    assertEquals(5500000.0, account.getBigDecimal("balance").doubleValue(), 0.01);
  }

  /**
   * <b>Given</b> a valid TRANSFER transaction creation request<br>
   * <b>When</b> the POST /transactions endpoint is called<br>
   * <b>Then</b> a new transaction should be created with status 200, source
   * balance decreased and destination balance increased
   */
  @Test
  public void createTransferTransactionShouldBeOk() {
    var body = String.format("""
        {
          "type": "TRANSFER",
          "title": "Transfer to Savings",
          "description": "Moving money to savings account",
          "date": "2024-06-10",
          "time": "16:45:00",
          "source": "%s",
          "destination": "%s",
          "amount": 100000,
          "currency": "IDR"
        }
        """, sourceAccountId, destinationAccountId);

    var response = Unirest.post(baseUrl + "/transactions")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(200, response.getStatus());
    var responseBody = new JSONObject(response.getBody());
    assertNotNull(responseBody.getString("id"));
    assertEquals("TRANSFER", responseBody.getString("type"));
    assertEquals("Transfer to Savings", responseBody.getString("title"));
    assertEquals("Moving money to savings account", responseBody.getString("description"));
    assertEquals("2024-06-10", responseBody.getString("date"));
    assertEquals("16:45", responseBody.getString("time"));
    assertEquals(sourceAccountId, responseBody.getString("source"));
    assertEquals(destinationAccountId, responseBody.getString("destination"));
    assertEquals(100000, responseBody.getLong("amount"));
    assertEquals("IDR", responseBody.getString("currency"));
    assertEquals(USER_ID, responseBody.getString("user"));

    var sourceResponse = Unirest.get(baseUrl + "/accounts/" + sourceAccountId)
        .header("Authorization", "Bearer " + token)
        .asString();
    assertEquals(200, sourceResponse.getStatus());
    var sourceAccount = new JSONObject(sourceResponse.getBody());
    assertEquals(900000.0, sourceAccount.getBigDecimal("balance").doubleValue(), 0.01);

    var destResponse = Unirest.get(baseUrl + "/accounts/" + destinationAccountId)
        .header("Authorization", "Bearer " + token)
        .asString();
    assertEquals(200, destResponse.getStatus());
    var destAccount = new JSONObject(destResponse.getBody());
    assertEquals(600000.0, destAccount.getBigDecimal("balance").doubleValue(), 0.01);
  }

  /**
   * <b>Given</b> a valid transaction creation request with loan reference<br>
   * <b>When</b> the POST /transactions endpoint is called<br>
   * <b>Then</b> a new transaction should be created with status 200, loan
   * reference, and loan remaining amount decreased
   */
  @Test
  public void createTransactionWithLoanShouldBeOk() {
    var loanId = createLoan("BORROW", "John Doe", "Personal Loan", 2000000, 5.0, sourceAccountId);

    var body = String.format("""
        {
          "type": "DEBIT",
          "title": "Loan Repayment",
          "description": "Monthly loan installment",
          "date": "2024-06-05",
          "time": "10:00:00",
          "source": "%s",
          "loan": "%s",
          "amount": 500000,
          "currency": "IDR",
          "category": "LOAN"
        }
        """, sourceAccountId, loanId);

    var response = Unirest.post(baseUrl + "/transactions")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(200, response.getStatus());
    var responseBody = new JSONObject(response.getBody());
    assertNotNull(responseBody.getString("id"));
    assertEquals("DEBIT", responseBody.getString("type"));
    assertEquals("Loan Repayment", responseBody.getString("title"));
    assertEquals("Monthly loan installment", responseBody.getString("description"));
    assertEquals(loanId, responseBody.getString("loan"));
    assertEquals(500000, responseBody.getLong("amount"));
    assertEquals(USER_ID, responseBody.getString("user"));

    var loanResponse = Unirest.get(baseUrl + "/loans/" + loanId)
        .header("Authorization", "Bearer " + token)
        .asString();
    assertEquals(200, loanResponse.getStatus());
    var loan = new JSONObject(loanResponse.getBody());
    assertEquals(1500000.0, loan.getBigDecimal("remainingAmount").doubleValue(), 0.01);
  }

  /**
   * <b>Given</b> a transaction creation request without authorization header<br>
   * <b>When</b> the POST /transactions endpoint is called<br>
   * <b>Then</b> the request should fail with status 401
   */
  @Test
  public void createTransactionWithoutAuthorizationShouldFail() {
    var body = String.format("""
        {
          "type": "DEBIT",
          "title": "Test Transaction",
          "description": "Test description",
          "source": "%s",
          "amount": 50000,
          "currency": "IDR"
        }
        """, sourceAccountId);

    var response = Unirest.post(baseUrl + "/transactions")
        .body(body)
        .asString();

    assertEquals(401, response.getStatus());
  }

  /**
   * <b>Given</b> a transaction creation request with invalid token<br>
   * <b>When</b> the POST /transactions endpoint is called<br>
   * <b>Then</b> the request should fail with status 401
   */
  @Test
  public void createTransactionWithInvalidTokenShouldFail() {
    var body = String.format("""
        {
          "type": "DEBIT",
          "title": "Test Transaction",
          "description": "Test description",
          "source": "%s",
          "amount": 50000,
          "currency": "IDR"
        }
        """, sourceAccountId);

    var response = Unirest.post(baseUrl + "/transactions")
        .header("Authorization", "Bearer invalid_token")
        .body(body)
        .asString();

    assertEquals(401, response.getStatus());
  }

  /**
   * <b>Given</b> a transaction creation request with invalid type<br>
   * <b>When</b> the POST /transactions endpoint is called<br>
   * <b>Then</b> the request should fail with status 400
   */
  @Test
  public void createTransactionWithInvalidTypeShouldFail() {
    var body = String.format("""
        {
          "type": "INVALID_TYPE",
          "title": "Test Transaction",
          "description": "Test description",
          "source": "%s",
          "amount": 50000,
          "currency": "IDR"
        }
        """, sourceAccountId);

    var response = Unirest.post(baseUrl + "/transactions")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(400, response.getStatus());
    assertEquals("type: invalid value", response.getBody());
  }

  /**
   * <b>Given</b> a transaction creation request with missing type<br>
   * <b>When</b> the POST /transactions endpoint is called<br>
   * <b>Then</b> the request should fail with status 400
   */
  @Test
  public void createTransactionWithMissingTypeShouldFail() {
    var body = """
        {
          "title": "Test Transaction",
          "description": "Test description",
          "source": "account-123",
          "amount": 50000,
          "currency": "IDR"
        }
        """;

    var response = Unirest.post(baseUrl + "/transactions")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(400, response.getStatus());
    assertEquals("type: invalid value", response.getBody());
  }

  /**
   * <b>Given</b> a transaction creation request with missing title<br>
   * <b>When</b> the POST /transactions endpoint is called<br>
   * <b>Then</b> the request should fail with status 400
   */
  @Test
  public void createTransactionWithMissingTitleShouldFail() {
    var body = String.format("""
        {
          "type": "DEBIT",
          "description": "Test description",
          "source": "%s",
          "amount": 50000,
          "currency": "IDR"
        }
        """, sourceAccountId);

    var response = Unirest.post(baseUrl + "/transactions")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(400, response.getStatus());
    assertEquals("title: invalid value", response.getBody());
  }

  /**
   * <b>Given</b> a transaction creation request with empty title<br>
   * <b>When</b> the POST /transactions endpoint is called<br>
   * <b>Then</b> the request should fail with status 400
   */
  @Test
  public void createTransactionWithEmptyTitleShouldFail() {
    var body = String.format("""
        {
          "type": "DEBIT",
          "title": "",
          "description": "Test description",
          "source": "%s",
          "amount": 50000,
          "currency": "IDR"
        }
        """, sourceAccountId);

    var response = Unirest.post(baseUrl + "/transactions")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(400, response.getStatus());
    assertEquals("title: invalid value", response.getBody());
  }

  /**
   * <b>Given</b> a transaction creation request with negative amount<br>
   * <b>When</b> the POST /transactions endpoint is called<br>
   * <b>Then</b> the request should fail with status 400
   */
  @Test
  public void createTransactionWithNegativeAmountShouldFail() {
    var body = String.format("""
        {
          "type": "DEBIT",
          "title": "Test Transaction",
          "description": "Test description",
          "source": "%s",
          "amount": -50000,
          "currency": "IDR"
        }
        """, sourceAccountId);

    var response = Unirest.post(baseUrl + "/transactions")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(400, response.getStatus());
    assertEquals("amount: invalid value", response.getBody());
  }

  /**
   * <b>Given</b> a transaction creation request with zero amount<br>
   * <b>When</b> the POST /transactions endpoint is called<br>
   * <b>Then</b> the request should fail with status 400
   */
  @Test
  public void createTransactionWithZeroAmountShouldFail() {
    var body = String.format("""
        {
          "type": "DEBIT",
          "title": "Test Transaction",
          "description": "Test description",
          "source": "%s",
          "amount": 0,
          "currency": "IDR"
        }
        """, sourceAccountId);

    var response = Unirest.post(baseUrl + "/transactions")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(400, response.getStatus());
    assertEquals("amount: invalid value", response.getBody());
  }

  /**
   * <b>Given</b> a transaction creation request with missing amount<br>
   * <b>When</b> the POST /transactions endpoint is called<br>
   * <b>Then</b> the request should fail with status 400
   */
  @Test
  public void createTransactionWithMissingAmountShouldFail() {
    var body = String.format("""
        {
          "type": "DEBIT",
          "title": "Test Transaction",
          "description": "Test description",
          "source": "%s",
          "currency": "IDR"
        }
        """, sourceAccountId);

    var response = Unirest.post(baseUrl + "/transactions")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(400, response.getStatus());
    assertEquals("amount: invalid value", response.getBody());
  }

  /**
   * <b>Given</b> a transaction creation request without currency<br>
   * <b>When</b> the POST /transactions endpoint is called<br>
   * <b>Then</b> a new transaction should be created with IDR as default currency
   */
  @Test
  public void createTransactionWithoutCurrencyShouldUseDefault() {
    var body = String.format("""
        {
          "type": "DEBIT",
          "title": "Test Transaction",
          "description": "Test description",
          "source": "%s",
          "amount": 50000
        }
        """, sourceAccountId);

    var response = Unirest.post(baseUrl + "/transactions")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(200, response.getStatus());
    var responseBody = new JSONObject(response.getBody());
    assertEquals("IDR", responseBody.getString("currency"));
  }

  /**
   * <b>Given</b> a transaction creation request without date and time<br>
   * <b>When</b> the POST /transactions endpoint is called<br>
   * <b>Then</b> a new transaction should be created with current date and time
   */
  @Test
  public void createTransactionWithoutDateTimeShouldUseNow() {
    var body = String.format("""
        {
          "type": "DEBIT",
          "title": "Test Transaction",
          "description": "Test description",
          "source": "%s",
          "amount": 50000,
          "currency": "IDR"
        }
        """, sourceAccountId);

    var response = Unirest.post(baseUrl + "/transactions")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(200, response.getStatus());
    var responseBody = new JSONObject(response.getBody());
    assertNotNull(responseBody.getString("date"));
    assertNotNull(responseBody.getString("time"));
  }

  /**
   * <b>Given</b> a DEBIT transaction creation request with missing source<br>
   * <b>When</b> the POST /transactions endpoint is called<br>
   * <b>Then</b> the request should fail with status 400
   */
  @Test
  public void createDebitTransactionWithMissingSourceShouldFail() {
    var body = """
        {
          "type": "DEBIT",
          "title": "Test Transaction",
          "description": "Test description",
          "amount": 50000,
          "currency": "IDR"
        }
        """;

    var response = Unirest.post(baseUrl + "/transactions")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(400, response.getStatus());
    assertEquals("source: invalid value", response.getBody());
  }

  /**
   * <b>Given</b> a CREDIT transaction creation request with missing
   * destination<br>
   * <b>When</b> the POST /transactions endpoint is called<br>
   * <b>Then</b> the request should fail with status 400
   */
  @Test
  public void createCreditTransactionWithMissingDestinationShouldFail() {
    var body = """
        {
          "type": "CREDIT",
          "title": "Test Transaction",
          "description": "Test description",
          "amount": 50000,
          "currency": "IDR"
        }
        """;

    var response = Unirest.post(baseUrl + "/transactions")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(400, response.getStatus());
    assertEquals("destination: invalid value", response.getBody());
  }

  /**
   * <b>Given</b> a TRANSFER transaction creation request with missing source and
   * destination<br>
   * <b>When</b> the POST /transactions endpoint is called<br>
   * <b>Then</b> the request should fail with status 400
   */
  @Test
  public void createTransferTransactionWithMissingAccountsShouldFail() {
    var body = """
        {
          "type": "TRANSFER",
          "title": "Test Transaction",
          "description": "Test description",
          "amount": 50000,
          "currency": "IDR"
        }
        """;

    var response = Unirest.post(baseUrl + "/transactions")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(400, response.getStatus());
  }

  /**
   * <b>Given</b> a transaction creation request with invalid date format<br>
   * <b>When</b> the POST /transactions endpoint is called<br>
   * <b>Then</b> the request should fail with status 400
   */
  @Test
  public void createTransactionWithInvalidDateShouldFail() {
    var body = String.format("""
        {
          "type": "DEBIT",
          "title": "Test Transaction",
          "description": "Test description",
          "source": "%s",
          "amount": 50000,
          "currency": "IDR",
          "date": "invalid-date"
        }
        """, sourceAccountId);

    var response = Unirest.post(baseUrl + "/transactions")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(400, response.getStatus());
    assertEquals("date: invalid value", response.getBody());
  }

  /**
   * <b>Given</b> a transaction creation request with invalid time format<br>
   * <b>When</b> the POST /transactions endpoint is called<br>
   * <b>Then</b> the request should fail with status 400
   */
  @Test
  public void createTransactionWithInvalidTimeShouldFail() {
    var body = String.format("""
        {
          "type": "DEBIT",
          "title": "Test Transaction",
          "description": "Test description",
          "source": "%s",
          "amount": 50000,
          "currency": "IDR",
          "time": "invalid-time"
        }
        """, sourceAccountId);

    var response = Unirest.post(baseUrl + "/transactions")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(400, response.getStatus());
    assertEquals("time: invalid value", response.getBody());
  }

  /**
   * <b>Given</b> a DEBIT transaction creation request with non-existent source
   * account<br>
   * <b>When</b> the POST /transactions endpoint is called<br>
   * <b>Then</b> the request should fail with status 404 and message about missing
   * source account
   */
  @Test
  public void createDebitTransactionWithNonExistentSourceShouldFail() {
    var body = """
        {
          "type": "DEBIT",
          "title": "Test Transaction",
          "description": "Test description",
          "source": "non-existent-account",
          "amount": 50000,
          "currency": "IDR"
        }
        """;

    var response = Unirest.post(baseUrl + "/transactions")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(400, response.getStatus());
    assertEquals("source account not found", response.getBody());
  }

  /**
   * <b>Given</b> a CREDIT transaction creation request with non-existent
   * destination account<br>
   * <b>When</b> the POST /transactions endpoint is called<br>
   * <b>Then</b> the request should fail with status 404 and message about missing
   * destination account
   */
  @Test
  public void createCreditTransactionWithNonExistentDestinationShouldFail() {
    var body = """
        {
          "type": "CREDIT",
          "title": "Test Transaction",
          "description": "Test description",
          "destination": "non-existent-account",
          "amount": 50000,
          "currency": "IDR"
        }
        """;

    var response = Unirest.post(baseUrl + "/transactions")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(400, response.getStatus());
    assertEquals("destination account not found", response.getBody());
  }

  /**
   * <b>Given</b> a TRANSFER transaction creation request with non-existent source
   * account<br>
   * <b>When</b> the POST /transactions endpoint is called<br>
   * <b>Then</b> the request should fail with status 404 and message about missing
   * source account
   */
  @Test
  public void createTransferTransactionWithNonExistentSourceShouldFail() {
    var body = """
        {
          "type": "TRANSFER",
          "title": "Test Transaction",
          "description": "Test description",
          "source": "non-existent-account",
          "destination": "account-456",
          "amount": 50000,
          "currency": "IDR"
        }
        """;

    var response = Unirest.post(baseUrl + "/transactions")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(400, response.getStatus());
    assertEquals("source account not found", response.getBody());
  }

  /**
   * <b>Given</b> a TRANSFER transaction creation request with non-existent
   * destination account<br>
   * <b>When</b> the POST /transactions endpoint is called<br>
   * <b>Then</b> the request should fail with status 404 and message about missing
   * destination account
   */
  @Test
  public void createTransferTransactionWithNonExistentDestinationShouldFail() {
    var body = String.format("""
        {
          "type": "TRANSFER",
          "title": "Test Transaction",
          "description": "Test description",
          "source": "%s",
          "destination": "non-existent-account",
          "amount": 50000,
          "currency": "IDR"
        }
        """, sourceAccountId);

    var response = Unirest.post(baseUrl + "/transactions")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(400, response.getStatus());
    assertEquals("destination account not found", response.getBody());
  }

  /**
   * <b>Given</b> a transaction creation request with non-existent loan<br>
   * <b>When</b> the POST /transactions endpoint is called<br>
   * <b>Then</b> the request should fail with status 404 and message about missing
   * loan
   */
  @Test
  public void createTransactionWithNonExistentLoanShouldFail() {
    var body = String.format("""
        {
          "type": "DEBIT",
          "title": "Loan Payment",
          "description": "Test description",
          "source": "%s",
          "loan": "non-existent-loan",
          "amount": 50000,
          "currency": "IDR"
        }
        """, sourceAccountId);

    var response = Unirest.post(baseUrl + "/transactions")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(400, response.getStatus());
    assertEquals("loan not found", response.getBody());
  }
}
