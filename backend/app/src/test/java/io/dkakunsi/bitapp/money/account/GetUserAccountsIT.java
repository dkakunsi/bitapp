package io.dkakunsi.bitapp.money.account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Map;

import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.dkakunsi.bitapp.jwt.JWTAuthorizer;
import io.dkakunsi.bitapp.money.AppLauncher;
import io.dkakunsi.bitapp.test.AppTestUtil;
import io.dkakunsi.bitapp.test.SecureTestUtil;
import kong.unirest.Unirest;

public class GetUserAccountsIT extends AppTestUtil {

  private static final int port = 20002;

  private static final String OTHER_USER_ID = "otheruser456";

  private static GetUserAccountsIT sut = new GetUserAccountsIT();

  private static String baseUrl;

  private static String token;

  private static String otherUserToken;

  @BeforeAll
  static void setup() throws Exception {
    var appEnv = Map.of(APP_PORT, Integer.toString(port),
        JWTAuthorizer.JWT_PUBLIC_KEY, SecureTestUtil.PUBLIC_KEY);
    sut.create(appEnv);
    sut.startServer(new AppLauncher());

    baseUrl = "http://localhost:" + port;
    token = SecureTestUtil.generateToken(USER_ID);
    otherUserToken = SecureTestUtil.generateToken(OTHER_USER_ID);
  }

  @AfterAll
  static void tearDown() throws Exception {
    sut.destroy();
  }

  @BeforeEach
  void setupTestData() {
    // Clear and setup test data for each test
    // Create accounts for the test user
    createAccount(token, "Savings Account", "BANK", "#FF5733");
    createAccount(token, "Cash Wallet", "CASH", "#3357FF");
    createAccount(token, "Digital Wallet", "EWALLET", "#00FF00");

    // Create accounts for another user to ensure filtering works
    createAccount(otherUserToken, "Other User Account", "BANK", "#000000");
  }

  private void createAccount(String token, String name, String type, String themeColor) {
    var body = String.format("""
        {
          "name": "%s",
          "type": "%s",
          "themeColor": "%s"
        }
        """, name, type, themeColor);

    Unirest.post(baseUrl + "/accounts")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();
  }

  /**
   * <b>Given</b> a user with multiple accounts of different types (BANK, CASH,
   * EWALLET)<br>
   * <b>When</b> the GET /users/{userId}/accounts endpoint is called with valid
   * authorization<br>
   * <b>Then</b> all accounts belonging to the user should be returned with status
   * 200 and complete details
   */
  @Test
  public void shouldReturnAllAccountsForUser_WhenMultipleAccountsExist() {
    // Given
    // When
    var response = Unirest.get(baseUrl + "/users/{userId}/accounts")
        .routeParam("userId", USER_ID)
        .header("Authorization", "Bearer " + token)
        .asString();

    // Then
    assertEquals(200, response.getStatus());

    var responseBody = new JSONObject(response.getBody());
    var accounts = responseBody.getJSONArray("accounts");

    // Should have at least 3 accounts for this user
    assertTrue(accounts.length() >= 3, "Should have at least 3 accounts");

    // Verify all accounts belong to the correct user
    for (int i = 0; i < accounts.length(); i++) {
      var account = accounts.getJSONObject(i);
      assertEquals(USER_ID, account.getString("userId"));
      assertNotNull(account.getString("id"));
      assertNotNull(account.getString("name"));
      assertNotNull(account.getString("type"));
      assertNotNull(account.getBigDecimal("balance"));
    }
  }

  /**
   * <b>Given</b> multiple users with their own accounts in the system<br>
   * <b>When</b> the GET /users/{userId}/accounts endpoint is called for a
   * specific user<br>
   * <b>Then</b> only that user's accounts should be returned, not accounts from
   * other users
   */
  @Test
  public void shouldReturnOnlyUserSpecificAccounts_NotOtherUsersAccounts() {
    // Given
    // When
    var response = Unirest.get(baseUrl + "/users/{userId}/accounts")
        .routeParam("userId", USER_ID)
        .header("Authorization", "Bearer " + token)
        .asString();

    // Then
    assertEquals(200, response.getStatus());

    var responseBody = new JSONObject(response.getBody());
    var accounts = responseBody.getJSONArray("accounts");

    // Verify none of the accounts belong to other users
    for (int i = 0; i < accounts.length(); i++) {
      var account = accounts.getJSONObject(i);
      assertEquals(USER_ID, account.getString("userId"));

      // Ensure no account named "Other User Account" is returned
      assertTrue(!account.getString("name").equals("Other User Account"),
          "Should not return accounts from other users");
    }
  }

  /**
   * <b>Given</b> a user with multiple accounts having specific names, types, and
   * theme colors<br>
   * <b>When</b> the GET /users/{userId}/accounts endpoint is called<br>
   * <b>Then</b> all account details should match exactly what was created (name,
   * type, color, balance)
   */
  @Test
  public void shouldReturnAccountsWithCorrectDetails_WhenRequested() {
    // Given
    // When
    var response = Unirest.get(baseUrl + "/users/{userId}/accounts")
        .routeParam("userId", USER_ID)
        .header("Authorization", "Bearer " + token)
        .asString();

    // Then
    assertEquals(200, response.getStatus());

    var responseBody = new JSONObject(response.getBody());
    var accounts = responseBody.getJSONArray("accounts");

    // Find and verify the savings account
    boolean foundSavings = false;
    boolean foundCash = false;
    boolean foundEWallet = false;

    for (int i = 0; i < accounts.length(); i++) {
      var account = accounts.getJSONObject(i);
      var name = account.getString("name");

      if (name.equals("Savings Account")) {
        foundSavings = true;
        assertEquals("BANK", account.getString("type"));
        assertEquals("#FF5733", account.getString("themeColor"));
        assertEquals(0, new BigDecimal("0").compareTo(account.getBigDecimal("balance")));
        assertEquals(USER_ID, account.getString("userId"));
      } else if (name.equals("Cash Wallet")) {
        foundCash = true;
        assertEquals("CASH", account.getString("type"));
        assertEquals("#3357FF", account.getString("themeColor"));
      } else if (name.equals("Digital Wallet")) {
        foundEWallet = true;
        assertEquals("EWALLET", account.getString("type"));
        assertEquals("#00FF00", account.getString("themeColor"));
      }
    }

    assertTrue(foundSavings, "Should find Savings Account");
    assertTrue(foundCash, "Should find Cash Wallet");
    assertTrue(foundEWallet, "Should find Digital Wallet");
  }

  /**
   * <b>Given</b> a user who has no accounts in the system<br>
   * <b>When</b> the GET /users/{userId}/accounts endpoint is called<br>
   * <b>Then</b> an empty accounts array should be returned with status 200
   */
  @Test
  public void shouldReturnEmptyList_WhenUserHasNoAccounts() {
    // Given
    var newUserId = "userwithnoaccount@email.com";
    var token = SecureTestUtil.generateToken(newUserId);

    // When
    var response = Unirest.get(baseUrl + "/users/{userId}/accounts")
        .routeParam("userId", newUserId)
        .header("Authorization", "Bearer " + token)
        .asString();

    // Then
    assertEquals(200, response.getStatus());

    var responseBody = new JSONObject(response.getBody());
    var accounts = responseBody.getJSONArray("accounts");

    assertEquals(0, accounts.length(), "Should return empty array for user with no accounts");
  }

  /**
   * <b>Given</b> two different users with their own separate accounts<br>
   * <b>When</b> the GET /users/{userId}/accounts endpoint is called for each
   * user<br>
   * <b>Then</b> each user should receive only their own accounts, properly
   * segregated
   */
  @Test
  public void shouldReturnCorrectAccountsForDifferentUsers() {
    // Given
    // When - Get accounts for first user
    var response1 = Unirest.get(baseUrl + "/users/{userId}/accounts")
        .routeParam("userId", USER_ID)
        .header("Authorization", "Bearer " + token)
        .asString();

    // When - Get accounts for second user
    var response2 = Unirest.get(baseUrl + "/users/{userId}/accounts")
        .routeParam("userId", OTHER_USER_ID)
        .header("Authorization", "Bearer " + otherUserToken)
        .asString();

    // Then - First user should have at least 3 accounts
    assertEquals(200, response1.getStatus());
    var responseBody1 = new JSONObject(response1.getBody());
    var accounts1 = responseBody1.getJSONArray("accounts");
    assertTrue(accounts1.length() >= 3);

    // Then - Second user should have at least 1 account
    assertEquals(200, response2.getStatus());
    var responseBody2 = new JSONObject(response2.getBody());
    var accounts2 = responseBody2.getJSONArray("accounts");
    assertTrue(accounts2.length() >= 1);

    // Verify accounts belong to correct users
    for (int i = 0; i < accounts1.length(); i++) {
      assertEquals(USER_ID, accounts1.getJSONObject(i).getString("userId"));
    }

    for (int i = 0; i < accounts2.length(); i++) {
      assertEquals(OTHER_USER_ID, accounts2.getJSONObject(i).getString("userId"));
    }
  }

  /**
   * <b>Given</b> a request without an Authorization header<br>
   * <b>When</b> the GET /users/{userId}/accounts endpoint is called<br>
   * <b>Then</b> the request should be rejected with status 401 (Unauthorized)
   */
  @Test
  public void shouldReturn401_WhenNoAuthorizationHeaderProvided() {
    // When
    var response = Unirest.get(baseUrl + "/users/{userId}/accounts")
        .routeParam("userId", USER_ID)
        .asString();

    // Then
    assertEquals(401, response.getStatus());
  }

  /**
   * <b>Given</b> a request with an invalid or malformed JWT token<br>
   * <b>When</b> the GET /users/{userId}/accounts endpoint is called<br>
   * <b>Then</b> the request should be rejected with status 401 (Unauthorized)
   */
  @Test
  public void shouldReturn401_WhenInvalidTokenProvided() {
    // When
    var response = Unirest.get(baseUrl + "/users/{userId}/accounts")
        .routeParam("userId", USER_ID)
        .header("Authorization", "Bearer invalid.token.here")
        .asString();

    // Then
    assertEquals(401, response.getStatus());
  }

  /**
   * <b>Given</b> a user with accounts in the system<br>
   * <b>When</b> the GET /users/{userId}/accounts endpoint is called multiple
   * times consecutively<br>
   * <b>Then</b> all responses should be identical, ensuring consistency and
   * idempotency
   */
  @Test
  public void shouldReturnConsistentResults_WhenCalledMultipleTimes() {
    // Given
    // When - First call
    var response1 = Unirest.get(baseUrl + "/users/{userId}/accounts")
        .routeParam("userId", USER_ID)
        .header("Authorization", "Bearer " + token)
        .asString();

    // When - Second call
    var response2 = Unirest.get(baseUrl + "/users/{userId}/accounts")
        .routeParam("userId", USER_ID)
        .header("Authorization", "Bearer " + token)
        .asString();

    // Then - Both responses should be identical
    assertEquals(200, response1.getStatus());
    assertEquals(200, response2.getStatus());

    var accounts1 = new JSONObject(response1.getBody()).getJSONArray("accounts");
    var accounts2 = new JSONObject(response2.getBody()).getJSONArray("accounts");

    assertEquals(accounts1.length(), accounts2.length(),
        "Should return same number of accounts on consecutive calls");
  }
}
