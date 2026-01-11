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
import io.dkakunsi.lab.test.SecureTestUtil;
import kong.unirest.Unirest;

public class CreateAccountIT extends BaseTest {

  private static final int port = 20002;

  private static CreateAccountIT sut = new CreateAccountIT();
  private static String baseUrl;

  @BeforeAll
  static void setup() throws Exception {
    var appEnv = Map.of(APP_PORT, Integer.toString(port), JWTAuthorizer.JWT_PUBLIC_KEY, SecureTestUtil.PUBLIC_KEY);
    sut.create(appEnv);
    sut.startServer();

    baseUrl = "http://localhost:" + port;
  }

  @AfterAll
  static void tearDown() throws Exception {
    sut.destroy();
  }

  @Test
  public void shouldCreateBankAccount_WhenValidRequestProvided() {
    var body = """
        {
          "user": "User001",
          "name": "My Bank Account",
          "type": "BANK",
          "themeColor": "#0000FF"
        }
        """;

    var response = Unirest.post(baseUrl + "/accounts")
        .body(body)
        .asString();

    assertEquals(201, response.getStatus());
    var responseBody = new JSONObject(response.getBody());
    assertNotNull(responseBody.getString("id"));
    assertEquals("My Bank Account", responseBody.getString("name"));
    assertEquals("BANK", responseBody.getString("type"));
    assertEquals("#0000FF", responseBody.getString("themeColor"));
    assertEquals(new BigDecimal("0"), responseBody.getBigDecimal("balance"));
    assertEquals("User001", responseBody.getString("user"));
  }

  @Test
  public void shouldCreateCashAccount_WhenValidRequestProvided() {
    var body = """
        {
          "user": "User001",
          "name": "My Cash Wallet",
          "type": "CASH",
          "themeColor": "#00FF00"
        }
        """;

    var response = Unirest.post(baseUrl + "/accounts")
        .body(body)
        .asString();

    assertEquals(201, response.getStatus());
    var responseBody = new JSONObject(response.getBody());
    assertNotNull(responseBody.getString("id"));
    assertEquals("My Cash Wallet", responseBody.getString("name"));
    assertEquals("CASH", responseBody.getString("type"));
    assertEquals("#00FF00", responseBody.getString("themeColor"));
    assertEquals(new BigDecimal("0"), responseBody.getBigDecimal("balance"));
    assertEquals("User001", responseBody.getString("user"));
  }

  @Test
  public void shouldCreateEWalletAccount_WhenValidRequestProvided() {
    var body = """
        {
          "user": "User001",
          "name": "Digital Wallet",
          "type": "EWALLET",
          "themeColor": "#FF0000"
        }
        """;

    var response = Unirest.post(baseUrl + "/accounts")
        .body(body)
        .asString();

    assertEquals(201, response.getStatus());
    var responseBody = new JSONObject(response.getBody());
    assertNotNull(responseBody.getString("id"));
    assertEquals("Digital Wallet", responseBody.getString("name"));
    assertEquals("EWALLET", responseBody.getString("type"));
    assertEquals("#FF0000", responseBody.getString("themeColor"));
    assertEquals(new BigDecimal("0"), responseBody.getBigDecimal("balance"));
    assertEquals("User001", responseBody.getString("user"));
  }

  @Test
  public void shouldCreateAccountWithoutThemeColor_WhenNotProvided() {
    var body = """
        {
          "user": "User001",
          "name": "Simple Account",
          "type": "BANK"
        }
        """;

    var response = Unirest.post(baseUrl + "/accounts")
        .body(body)
        .asString();

    assertEquals(201, response.getStatus());
    var responseBody = new JSONObject(response.getBody());
    assertNotNull(responseBody.getString("id"));
    assertEquals("Simple Account", responseBody.getString("name"));
    assertEquals("BANK", responseBody.getString("type"));
    assertEquals(new BigDecimal("0"), responseBody.getBigDecimal("balance"));
    assertEquals("User001", responseBody.getString("user"));
  }

  @Test
  public void shouldFailWithBadRequest_WhenNameIsEmpty() {
    var body = """
        {
          "user": "User001",
          "name": "",
          "type": "BANK",
          "themeColor": "#0000FF"
        }
        """;

    var response = Unirest.post(baseUrl + "/accounts")
        .body(body)
        .asString();

    assertEquals(400, response.getStatus());
    assertEquals("Invalid data", response.getBody());
  }

  @Test
  public void shouldFailWithBadRequest_WhenNameIsMissing() {
    var body = """
        {
          "user": "User001",
          "type": "BANK",
          "themeColor": "#0000FF"
        }
        """;

    var response = Unirest.post(baseUrl + "/accounts")
        .body(body)
        .asString();

    assertEquals(400, response.getStatus());
    assertEquals("Invalid data", response.getBody());
  }

  @Test
  public void shouldCreateMultipleAccounts_WithDifferentTypes() {
    // Create first account
    var body1 = """
        {
          "user": "User001",
          "name": "First Account",
          "type": "BANK",
          "themeColor": "#0000FF"
        }
        """;

    var response1 = Unirest.post(baseUrl + "/accounts")
        .body(body1)
        .asString();

    assertEquals(201, response1.getStatus());
    var responseBody1 = new JSONObject(response1.getBody());
    var firstAccountId = responseBody1.getString("id");
    assertEquals("First Account", responseBody1.getString("name"));
    assertEquals("BANK", responseBody1.getString("type"));

    // Create second account
    var body2 = """
        {
          "user": "User001",
          "name": "Second Account",
          "type": "CASH",
          "themeColor": "#00FF00"
        }
        """;

    var response2 = Unirest.post(baseUrl + "/accounts")
        .body(body2)
        .asString();

    assertEquals(201, response2.getStatus());
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
