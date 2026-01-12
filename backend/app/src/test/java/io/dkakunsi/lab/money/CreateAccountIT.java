package io.dkakunsi.lab.money;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.util.Map;

import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.dkakunsi.bitapp.security.jwt.JWTAuthorizer;
import io.dkakunsi.lab.test.AppTestUtil;
import io.dkakunsi.lab.test.SecureTestUtil;
import kong.unirest.Unirest;

public class CreateAccountIT extends AppTestUtil {

  private static final int port = 20002;

  private static CreateAccountIT sut = new CreateAccountIT();
  private static String baseUrl;
  private static String token;

  @BeforeAll
  static void setup() throws Exception {
    var appEnv = Map.of(APP_PORT, Integer.toString(port), JWTAuthorizer.JWT_PUBLIC_KEY, SecureTestUtil.PUBLIC_KEY);
    sut.create(appEnv);
    sut.startServer(new AppLauncher());

    baseUrl = "http://localhost:" + port;
    token = SecureTestUtil.generateToken("User001");
  }

  @AfterAll
  static void tearDown() throws Exception {
    sut.destroy();
  }

  /**
   * <b>Given</b> a valid account creation request with BANK type<br>
   * <b>When</b> the POST /accounts endpoint is called<br>
   * <b>Then</b> a new bank account should be created with status 200 and all
   * provided details
   */
  @Test
  public void shouldCreateBankAccount_WhenValidRequestProvided() {
    var body = """
        {
          "name": "My Bank Account",
          "type": "BANK",
          "themeColor": "#0000FF"
        }
        """;

    var response = Unirest.post(baseUrl + "/accounts")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(200, response.getStatus());
    var responseBody = new JSONObject(response.getBody());
    assertNotNull(responseBody.getString("id"));
    assertEquals("My Bank Account", responseBody.getString("name"));
    assertEquals("BANK", responseBody.getString("type"));
    assertEquals("#0000FF", responseBody.getString("themeColor"));
    assertEquals(new BigDecimal("0"), responseBody.getBigDecimal("balance"));
    assertEquals("User001", responseBody.getString("user"));
  }

  /**
   * <b>Given</b> a valid account creation request with CASH type<br>
   * <b>When</b> the POST /accounts endpoint is called<br>
   * <b>Then</b> a new cash account should be created with status 200 and all
   * provided details
   */
  @Test
  public void shouldCreateCashAccount_WhenValidRequestProvided() {
    var body = """
        {
          "name": "My Cash Wallet",
          "type": "CASH",
          "themeColor": "#00FF00"
        }
        """;

    var response = Unirest.post(baseUrl + "/accounts")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(200, response.getStatus());
    var responseBody = new JSONObject(response.getBody());
    assertNotNull(responseBody.getString("id"));
    assertEquals("My Cash Wallet", responseBody.getString("name"));
    assertEquals("CASH", responseBody.getString("type"));
    assertEquals("#00FF00", responseBody.getString("themeColor"));
    assertEquals(new BigDecimal("0"), responseBody.getBigDecimal("balance"));
    assertEquals("User001", responseBody.getString("user"));
  }

  /**
   * <b>Given</b> a valid account creation request with EWALLET type<br>
   * <b>When</b> the POST /accounts endpoint is called<br>
   * <b>Then</b> a new e-wallet account should be created with status 200 and all
   * provided details
   */
  @Test
  public void shouldCreateEWalletAccount_WhenValidRequestProvided() {
    var body = """
        {
          "name": "Digital Wallet",
          "type": "EWALLET",
          "themeColor": "#FF0000"
        }
        """;

    var response = Unirest.post(baseUrl + "/accounts")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(200, response.getStatus());
    var responseBody = new JSONObject(response.getBody());
    assertNotNull(responseBody.getString("id"));
    assertEquals("Digital Wallet", responseBody.getString("name"));
    assertEquals("EWALLET", responseBody.getString("type"));
    assertEquals("#FF0000", responseBody.getString("themeColor"));
    assertEquals(new BigDecimal("0"), responseBody.getBigDecimal("balance"));
    assertEquals("User001", responseBody.getString("user"));
  }

  /**
   * <b>Given</b> a valid account creation request without themeColor field<br>
   * <b>When</b> the POST /accounts endpoint is called<br>
   * <b>Then</b> a new account should be created successfully with status 200,
   * using EMPTY theme color
   */
  @Test
  public void shouldCreateAccountWithoutThemeColor_WhenNotProvided() {
    var body = """
        {
          "name": "Simple Account",
          "type": "BANK"
        }
        """;

    var response = Unirest.post(baseUrl + "/accounts")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(200, response.getStatus());
    var responseBody = new JSONObject(response.getBody());
    assertNotNull(responseBody.getString("id"));
    assertEquals("Simple Account", responseBody.getString("name"));
    assertEquals("BANK", responseBody.getString("type"));
    assertEquals(new BigDecimal("0"), responseBody.getBigDecimal("balance"));
    assertEquals("User001", responseBody.getString("user"));
    assertEquals("#FFFFFF", responseBody.getString("themeColor"));
  }

  /**
   * <b>Given</b> an account creation request with an empty name field<br>
   * <b>When</b> the POST /accounts endpoint is called<br>
   * <b>Then</b> the request should fail with status 400 and "Invalid data"
   * message
   */
  @Test
  public void shouldFailWithBadRequest_WhenNameIsEmpty() {
    var body = """
        {
          "name": "",
          "type": "BANK",
          "themeColor": "#0000FF"
        }
        """;

    var response = Unirest.post(baseUrl + "/accounts")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(400, response.getStatus());
    assertEquals("Invalid data", response.getBody());
  }

  /**
   * <b>Given</b> an account creation request without a name field<br>
   * <b>When</b> the POST /accounts endpoint is called<br>
   * <b>Then</b> the request should fail with status 400 and "Invalid data"
   * message
   */
  @Test
  public void shouldFailWithBadRequest_WhenNameIsMissing() {
    var body = """
        {
          "type": "BANK",
          "themeColor": "#0000FF"
        }
        """;

    var response = Unirest.post(baseUrl + "/accounts")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(400, response.getStatus());
    assertEquals("Invalid data", response.getBody());
  }

  /**
   * <b>Given</b> multiple valid account creation requests with different account
   * types<br>
   * <b>When</b> the POST /accounts endpoint is called sequentially for each
   * account<br>
   * <b>Then</b> all accounts should be created successfully with unique IDs and
   * correct types
   */
  @Test
  public void shouldCreateMultipleAccounts_WithDifferentTypes() {
    // Create first account
    var body1 = """
        {
          "name": "First Account",
          "type": "BANK",
          "themeColor": "#0000FF"
        }
        """;

    var response1 = Unirest.post(baseUrl + "/accounts")
        .header("Authorization", "Bearer " + token)
        .body(body1)
        .asString();

    assertEquals(200, response1.getStatus());
    var responseBody1 = new JSONObject(response1.getBody());
    var firstAccountId = responseBody1.getString("id");
    assertEquals("First Account", responseBody1.getString("name"));
    assertEquals("BANK", responseBody1.getString("type"));

    // Create second account
    var body2 = """
        {
          "name": "Second Account",
          "type": "CASH",
          "themeColor": "#00FF00"
        }
        """;

    var response2 = Unirest.post(baseUrl + "/accounts")
        .header("Authorization", "Bearer " + token)
        .body(body2)
        .asString();

    assertEquals(200, response2.getStatus());
    var responseBody2 = new JSONObject(response2.getBody());
    var secondAccountId = responseBody2.getString("id");
    assertEquals("Second Account", responseBody2.getString("name"));
    assertEquals("CASH", responseBody2.getString("type"));

    // Ensure different IDs
    assertNotNull(firstAccountId);
    assertNotNull(secondAccountId);
    assertEquals(false, firstAccountId.equals(secondAccountId));
  }
}
