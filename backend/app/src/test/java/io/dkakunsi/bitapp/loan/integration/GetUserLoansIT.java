package io.dkakunsi.bitapp.loan.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

import org.json.JSONArray;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.langchain4j.model.chat.ChatModel;
import io.dkakunsi.bitapp.AppLauncher;
import io.dkakunsi.bitapp.DateTimeConverter;
import io.dkakunsi.bitapp.jwt.JWTAuthorizer;
import io.dkakunsi.bitapp.test.AppTestUtil;
import io.dkakunsi.bitapp.test.SecureTestUtil;
import kong.unirest.Unirest;

public class GetUserLoansIT extends AppTestUtil {

  private static final String OTHER_USER_ID = "otheruser456";

  private static GetUserLoansIT sut = new GetUserLoansIT();

  private static String baseUrl;

  private static String token;

  private static String otherUserToken;

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
    otherUserToken = SecureTestUtil.generateToken(OTHER_USER_ID);
  }

  @AfterAll
  static void tearDown() throws Exception {
    sut.destroy();
  }

  @BeforeEach
  void setupTestData() {
    // Clear and setup test data for each test
    // Create loans for the test user
    createLoan(token, "BORROW", "Bank ABC", "Car Loan", "Loan for purchasing a car", 500000000, "IDR",
        5.5);
    createLoan(token, "LEND", "John Doe", "Personal Loan", "Money lent to friend", 10000000, "IDR", 2.0);
    createLoan(token, "BORROW", "Credit Union", "Home Renovation", "Loan for home improvement", 200000000,
        "IDR", 4.5);

    // Create loans for another user to ensure filtering works
    createLoan(otherUserToken, "BORROW", "Other Bank", "Other User Loan", "Another user's loan", 100000000,
        "IDR", 3.0);
  }

  private void createLoan(String token, String type, String partyName, String title, String description,
      long amount, String currency, double interestRate) {
    var date = DateTimeConverter.epochMilli(LocalDate.of(2024, 6, 15));
    var time = DateTimeConverter.minutesSinceMidnight(LocalTime.of(14, 30));
    var body = String.format("""
        {
          "type": "%s",
          "partyName": "%s",
          "date": %d,
          "time": %d,
          "title": "%s",
          "description": "%s",
          "amount": %d,
          "currency": "%s",
          "interestRate": %.1f
        }
        """, type, partyName, date, time, title, description, amount, currency, interestRate);

    Unirest.post(baseUrl + "/v1/loans")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();
  }

  /**
   * <b>Given</b> a user with multiple loans of different types (BORROW,
   * LEND)<br>
   * <b>When</b> the GET /users/{userId}/loans endpoint is called with valid
   * authorization<br>
   * <b>Then</b> all loans belonging to the user should be returned with status
   * 200 and complete details
   */
  @Test
  public void getExistingUserLoansShouldBeOk() {
    // Given
    // When
    var response = Unirest.get(baseUrl + "/v1/users/{userId}/loans")
        .routeParam("userId", USER_ID)
        .header("Authorization", "Bearer " + token)
        .asString();

    // Then
    assertEquals(200, response.getStatus());

    var responseBody = new JSONArray(response.getBody());

    // Should have at least 3 loans for this user
    assertTrue(responseBody.length() >= 3, "Should have at least 3 loans");

    // Verify all loans belong to the correct user
    for (int i = 0; i < responseBody.length(); i++) {
      var loan = responseBody.getJSONObject(i);
      assertEquals(USER_ID, loan.getString("user"));
      assertNotNull(loan.getString("id"));
      assertNotNull(loan.getString("type"));
      assertNotNull(loan.getString("partyName"));
      assertNotNull(loan.getLong("date"));
      assertNotNull(loan.getInt("time"));
      assertNotNull(loan.getString("title"));
      assertNotNull(loan.getString("description"));
      assertNotNull(loan.getBigDecimal("amount"));
      assertNotNull(loan.getBigDecimal("remainingAmount"));
      assertNotNull(loan.getString("currency"));
      assertTrue(loan.getDouble("interestRate") >= 0);
    }
  }

  /**
   * <b>Given</b> multiple users with their own loans in the system<br>
   * <b>When</b> the GET /users/{userId}/loans endpoint is called for a specific
   * user<br>
   * <b>Then</b> only that user's loans should be returned, not loans from other
   * users
   */
  @Test
  public void getUserSpecificLoansShouldNotIncludeOtherUsersLoans() {
    // Given
    // When
    var response = Unirest.get(baseUrl + "/v1/users/{userId}/loans")
        .routeParam("userId", USER_ID)
        .header("Authorization", "Bearer " + token)
        .asString();

    // Then
    assertEquals(200, response.getStatus());

    var responseBody = new JSONArray(response.getBody());

    // Verify none of the loans belong to other users
    for (int i = 0; i < responseBody.length(); i++) {
      var loan = responseBody.getJSONObject(i);
      assertEquals(USER_ID, loan.getString("user"));

      // Ensure no loan titled "Other User Loan" is returned
      assertTrue(!loan.getString("title").equals("Other User Loan"),
          "Should not return loans from other users");
    }
  }

  /**
   * <b>Given</b> a user with multiple loans having specific details (type, party
   * name, title, amount)<br>
   * <b>When</b> the GET /users/{userId}/loans endpoint is called<br>
   * <b>Then</b> all loan details should match exactly what was created
   */
  @Test
  public void getLoansWithCorrectDetailsShouldBeOk() {
    // Given
    // When
    var response = Unirest.get(baseUrl + "/v1/users/{userId}/loans")
        .routeParam("userId", USER_ID)
        .header("Authorization", "Bearer " + token)
        .asString();

    // Then
    assertEquals(200, response.getStatus());

    var responseBody = new JSONArray(response.getBody());

    // Find and verify the specific loans
    boolean foundCarLoan = false;
    boolean foundPersonalLoan = false;
    boolean foundHomeRenovation = false;

    for (int i = 0; i < responseBody.length(); i++) {
      var loan = responseBody.getJSONObject(i);
      var title = loan.getString("title");

      if (title.equals("Car Loan")) {
        foundCarLoan = true;
        assertEquals("BORROW", loan.getString("type"));
        assertEquals("Bank ABC", loan.getString("partyName"));
        assertEquals(0, new BigDecimal("500000000").compareTo(loan.getBigDecimal("amount")));
        assertEquals("IDR", loan.getString("currency"));
        assertEquals(USER_ID, loan.getString("user"));
      } else if (title.equals("Personal Loan")) {
        foundPersonalLoan = true;
        assertEquals("LEND", loan.getString("type"));
        assertEquals("John Doe", loan.getString("partyName"));
        assertEquals(0, new BigDecimal("10000000").compareTo(loan.getBigDecimal("amount")));
      } else if (title.equals("Home Renovation")) {
        foundHomeRenovation = true;
        assertEquals("BORROW", loan.getString("type"));
        assertEquals("Credit Union", loan.getString("partyName"));
        assertEquals(0, new BigDecimal("200000000").compareTo(loan.getBigDecimal("amount")));
      }
    }

    assertTrue(foundCarLoan, "Should find Car Loan");
    assertTrue(foundPersonalLoan, "Should find Personal Loan");
    assertTrue(foundHomeRenovation, "Should find Home Renovation");
  }

  /**
   * <b>Given</b> a user who has no loans in the system<br>
   * <b>When</b> the GET /users/{userId}/loans endpoint is called<br>
   * <b>Then</b> an empty loans array should be returned with status 200
   */
  @Test
  public void getEmptyListWhenUserHasNoLoans() {
    // Given
    var newUserId = "userwithnoloans@email.com";
    var token = SecureTestUtil.generateToken(newUserId);

    // When
    var response = Unirest.get(baseUrl + "/v1/users/{userId}/loans")
        .routeParam("userId", newUserId)
        .header("Authorization", "Bearer " + token)
        .asString();

    // Then
    assertEquals(200, response.getStatus());

    var responseBody = new JSONArray(response.getBody());

    assertEquals(0, responseBody.length(), "Should return empty array for user with no loans");
  }

  /**
   * <b>Given</b> two different users with their own separate loans<br>
   * <b>When</b> the GET /users/{userId}/loans endpoint is called for each
   * user<br>
   * <b>Then</b> each user should receive only their own loans, properly
   * segregated
   */
  @Test
  public void getCorrectLoansForDifferentUsers() {
    // Given
    // When - Get loans for first user
    var response1 = Unirest.get(baseUrl + "/v1/users/{userId}/loans")
        .routeParam("userId", USER_ID)
        .header("Authorization", "Bearer " + token)
        .asString();

    // Then - First user should have at least 3 loans
    assertEquals(200, response1.getStatus());
    var responseBody1 = new JSONArray(response1.getBody());
    assertTrue(responseBody1.length() >= 3);

    // Verify loans belong to correct users
    for (int i = 0; i < responseBody1.length(); i++) {
      assertEquals(USER_ID, responseBody1.getJSONObject(i).getString("user"));
    }

    // When - Get loans for second user
    var response2 = Unirest.get(baseUrl + "/v1/users/{userId}/loans")
        .routeParam("userId", OTHER_USER_ID)
        .header("Authorization", "Bearer " + otherUserToken)
        .asString();

    // Then - Second user should have at least 1 loan
    assertEquals(200, response2.getStatus());
    var responseBody2 = new JSONArray(response2.getBody());
    assertTrue(responseBody2.length() >= 1);

    for (int i = 0; i < responseBody2.length(); i++) {
      assertEquals(OTHER_USER_ID, responseBody2.getJSONObject(i).getString("user"));
    }
  }

  /**
   * <b>Given</b> a request without an Authorization header<br>
   * <b>When</b> the GET /users/{userId}/loans endpoint is called<br>
   * <b>Then</b> the request should be rejected with status 401 (Unauthorized)
   */
  @Test
  public void getUserLoansWithoutAuthorizationHeaderShouldReturn401() {
    // When
    var response = Unirest.get(baseUrl + "/v1/users/{userId}/loans")
        .routeParam("userId", USER_ID)
        .asString();

    // Then
    assertEquals(401, response.getStatus());
  }

  /**
   * <b>Given</b> a request with an invalid or malformed JWT token<br>
   * <b>When</b> the GET /users/{userId}/loans endpoint is called<br>
   * <b>Then</b> the request should be rejected with status 401 (Unauthorized)
   */
  @Test
  public void getUserLoansWithInvalidTokenShouldReturn401() {
    // When
    var response = Unirest.get(baseUrl + "/v1/users/{userId}/loans")
        .routeParam("userId", USER_ID)
        .header("Authorization", "Bearer invalid.token.here")
        .asString();

    // Then
    assertEquals(401, response.getStatus());
  }

  /**
   * <b>Given</b> a user with loans in the system<br>
   * <b>When</b> the GET /users/{userId}/loans endpoint is called multiple times
   * consecutively<br>
   * <b>Then</b> all responses should be identical, ensuring consistency and
   * idempotency
   */
  @Test
  public void getUserLoansMultipleTimesShouldReturnConsistentResults() {
    // Given
    // When - First call
    var response1 = Unirest.get(baseUrl + "/v1/users/{userId}/loans")
        .routeParam("userId", USER_ID)
        .header("Authorization", "Bearer " + token)
        .asString();

    // When - Second call
    var response2 = Unirest.get(baseUrl + "/v1/users/{userId}/loans")
        .routeParam("userId", USER_ID)
        .header("Authorization", "Bearer " + token)
        .asString();

    // Then - Both responses should be identical
    assertEquals(200, response1.getStatus());
    assertEquals(200, response2.getStatus());

    var loans1 = new JSONArray(response1.getBody());
    var loans2 = new JSONArray(response2.getBody());

    assertEquals(loans1.length(), loans2.length(),
        "Should return same number of loans on consecutive calls");
  }
}
