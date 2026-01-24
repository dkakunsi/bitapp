package io.dkakunsi.bitapp.account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.util.Map;

import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.dkakunsi.bitapp.AppLauncher;
import io.dkakunsi.bitapp.jwt.JWTAuthorizer;
import io.dkakunsi.bitapp.test.AppTestUtil;
import io.dkakunsi.bitapp.test.SecureTestUtil;
import kong.unirest.Unirest;

public class CreateAccountIT extends AppTestUtil {

  private static final int port = 20001;

  private static CreateAccountIT sut = new CreateAccountIT();

  private static String baseUrl;

  private static String token;

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

  /**
   * <b>Given</b> a valid account creation request with BANK type<br>
   * <b>When</b> the POST /accounts endpoint is called<br>
   * <b>Then</b> a new bank account should be created with status 200 and all
   * provided details
   */
  @Test
  public void createBankAccountShouldBeOk() {
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
    assertEquals("user@email.com", responseBody.getString("user"));
  }

  /**
   * <b>Given</b> a valid account creation request with CASH type<br>
   * <b>When</b> the POST /accounts endpoint is called<br>
   * <b>Then</b> a new cash account should be created with status 200 and all
   * provided details
   */
  @Test
  public void createCashAccountShouldBeOk() {
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
    assertEquals("user@email.com", responseBody.getString("user"));
  }

  /**
   * <b>Given</b> a valid account creation request with EWALLET type<br>
   * <b>When</b> the POST /accounts endpoint is called<br>
   * <b>Then</b> a new e-wallet account should be created with status 200 and all
   * provided details
   */
  @Test
  public void createEWalletAccountShouldBeOk() {
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
    assertEquals("user@email.com", responseBody.getString("user"));
  }

  /**
   * <b>Given</b> an account creation request with an invalid type<br>
   * <b>When</b> the POST /accounts endpoint is called<br>
   * <b>Then</b> the request should fail with status 400
   */
  @Test
  public void createAccountWithInvalidTypeShouldFail() {
    var body = """
        {
          "name": "Invalid Type Account",
          "type": "INVALID_TYPE",
          "themeColor": "#0000FF"
        }
        """;

    var response = Unirest.post(baseUrl + "/accounts")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(400, response.getStatus());
    assertEquals("type: invalid value", response.getBody());
  }

  /**
   * <b>Given</b> an account creation request without a type field<br>
   * <b>When</b> the POST /accounts endpoint is called<br>
   * <b>Then</b> the request should fail with status 400
   */
  @Test
  public void createAccountWithMissingTypeShouldFail() {
    var body = """
        {
          "name": "No Type Account",
          "themeColor": "#0000FF"
        }
        """;

    var response = Unirest.post(baseUrl + "/accounts")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(400, response.getStatus());
    assertEquals("type: invalid value", response.getBody());
  }

  /**
   * <b>Given</b> a valid account creation request without themeColor field<br>
   * <b>When</b> the POST /accounts endpoint is called<br>
   * <b>Then</b> a new account should be created successfully with status 200,
   * using default theme color (white)
   */
  @Test
  public void createAccountWithoutThemeColorShouldBeOk() {
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
    assertEquals("user@email.com", responseBody.getString("user"));
    assertEquals("#FFFFFF", responseBody.getString("themeColor"));
  }

  /**
   * <b>Given</b> an account creation request with an empty name field<br>
   * <b>When</b> the POST /accounts endpoint is called<br>
   * <b>Then</b> the request should fail with status 400 and "name: must not be
   * blank" message
   */
  @Test
  public void createAccountWithEmptyNameShouldFail() {
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
    assertEquals("name: invalid value", response.getBody());
  }

  /**
   * <b>Given</b> an account creation request without a name field<br>
   * <b>When</b> the POST /accounts endpoint is called<br>
   * <b>Then</b> the request should fail with status 400 and "name: must not be
   * blank" message
   */
  @Test
  public void createAccountWithMissingNameShouldFail() {
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
    assertEquals("name: invalid value", response.getBody());
  }

  /**
   * <b>Given</b> an account creation request without authorization token<br>
   * <b>When</b> the POST /accounts endpoint is called<br>
   * <b>Then</b> the request should fail with status 401
   */
  @Test
  public void createAccountWithNoTokenShouldFail() {
    var body = """
        {
          "name": "Unauthorized Account",
          "type": "BANK",
          "themeColor": "#0000FF"
        }
        """;

    var response = Unirest.post(baseUrl + "/accounts")
        .body(body)
        .asString();

    assertEquals(401, response.getStatus());
  }

  /**
   * <b>Given</b> an account creation request with an invalid authorization
   * token<br>
   * <b>When</b> the POST /accounts endpoint is called<br>
   * <b>Then</b> the request should fail with status 401
   */
  @Test
  public void createAccountWithInvalidTokenShouldFail() {
    var body = """
        {
          "name": "Unauthorized Account",
          "type": "BANK",
          "themeColor": "#0000FF"
        }
        """;

    var response = Unirest.post(baseUrl + "/accounts")
        .header("Authorization", "Bearer invalid_token")
        .body(body)
        .asString();

    assertEquals(401, response.getStatus());
  }
}
