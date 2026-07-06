package io.dkakunsi.bitapp.account.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.dkakunsi.bitapp.AppLauncher;
import io.dkakunsi.bitapp.jwt.JWTAuthorizer;
import io.dkakunsi.bitapp.test.AppTestUtil;
import io.dkakunsi.bitapp.test.SecureTestUtil;
import kong.unirest.Unirest;
import kong.unirest.json.JSONObject;

public class GetAccountIT extends AppTestUtil {

  private static GetAccountIT sut = new GetAccountIT();

  private static String baseUrl;

  private static String token;

  @BeforeAll
  static void setup() throws Exception {
    var port = getPort();
    sut.create(Map.of(APP_PORT, Integer.toString(port),
        JWTAuthorizer.JWT_PUBLIC_KEY, SecureTestUtil.PUBLIC_KEY));
    sut.startServer(new AppLauncher());

    baseUrl = "http://localhost:" + port;
    token = SecureTestUtil.generateToken(USER_ID);
  }

  @AfterAll
  static void tearDown() throws Exception {
    sut.destroy();
  }

  /**
   * <b>Given</b> an account exists in the system<br>
   * <b>When</b> the GET /accounts/{id} endpoint is called with the account ID<br>
   * <b>Then</b> the account's complete details should be returned with status 200
   */
  @Test
  public void getExistingAccountShouldBeOk() {
    var createBody = """
        {
          "name": "My Savings Account",
          "type": "BANK",
          "themeColor": "#FF5733"
        }
        """;

    var postResponse = Unirest.post(baseUrl + "/v1/accounts")
        .header("Authorization", "Bearer " + token)
        .body(createBody)
        .asString();
    assertEquals(200, postResponse.getStatus());
    var postResponseBody = new JSONObject(postResponse.getBody());
    var accountId = postResponseBody.getString("id");
    assertNotNull(accountId);
    assertEquals("My Savings Account", postResponseBody.getString("name"));
    assertEquals("BANK", postResponseBody.getString("type"));
    assertEquals("#FF5733", postResponseBody.getString("themeColor"));

    var getResponse = Unirest.get(baseUrl + "/v1/accounts/" + accountId)
        .header("Authorization", "Bearer " + token)
        .asString();
    assertEquals(200, getResponse.getStatus());
    var getResponseBody = new JSONObject(getResponse.getBody());
    assertEquals(accountId, getResponseBody.getString("id"));
    assertEquals("My Savings Account", getResponseBody.getString("name"));
    assertEquals("BANK", getResponseBody.getString("type"));
    assertEquals("#FF5733", getResponseBody.getString("themeColor"));
    assertEquals(0, getResponseBody.getBigDecimal("balance").intValue());
    assertEquals(USER_ID, getResponseBody.getString("user"));
  }

  /**
   * <b>Given</b> an account ID that does not exist in the system<br>
   * <b>When</b> the GET /accounts/{id} endpoint is called<br>
   * <b>Then</b> a 404 status should be returned with an error message
   */
  @Test
  public void getNonExistingAccountShouldReturn404() {
    var getResponse = Unirest.get(baseUrl + "/v1/accounts/550e8400-e29b-41d4-a716-446655440000")
        .header("Authorization", "Bearer " + token)
        .asString();
    assertEquals(404, getResponse.getStatus());
    assertEquals("Account not found", getResponse.getBody());
  }

  /**
   * <b>Given</b> a request without Authorization header<br>
   * <b>When</b> the GET /accounts/{id} endpoint is called<br>
   * <b>Then</b> a 401 status should be returned
   */
  @Test
  public void getAccountWithoutAuthorizationHeaderShouldReturn401() {
    var getResponse = Unirest.get(baseUrl + "/v1/accounts/some-account-id")
        .asString();
    assertEquals(401, getResponse.getStatus());
  }

  /**
   * <b>Given</b> a request with an invalid Authorization header<br>
   * <b>When</b> the GET /accounts/{id} endpoint is called<br>
   * <b>Then</b> a 401 status should be returned
   */
  @Test
  public void getAccountWithInvalidTokenShouldReturn401() {
    var getResponse = Unirest.get(baseUrl + "/v1/accounts/some-account-id")
        .header("Authorization", "Bearer invalid-token")
        .asString();
    assertEquals(401, getResponse.getStatus());
  }
}
