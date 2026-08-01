package io.dkakunsi.bitapp.loan.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import dev.langchain4j.model.chat.ChatModel;
import io.dkakunsi.bitapp.AppLauncher;
import io.dkakunsi.bitapp.DateTimeConverter;
import io.dkakunsi.bitapp.jwt.JWTAuthorizer;
import io.dkakunsi.bitapp.test.AppTestUtil;
import io.dkakunsi.bitapp.test.SecureTestUtil;
import kong.unirest.Unirest;

public class GetLoanIT extends AppTestUtil {

  private static GetLoanIT sut = new GetLoanIT();

  private static String baseUrl;

  private static String token;

  @BeforeAll
  static void setup() throws Exception {
    AppTestUtil.setTestDependency(ChatModel.class, mock(ChatModel.class));
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
   * <b>Given</b> a loan exists in the system<br>
   * <b>When</b> the GET /loans/{id} endpoint is called with the loan ID<br>
   * <b>Then</b> the loan's complete details should be returned with status 200
   */
  @Test
  public void getExistingLoanShouldBeOk() {
    var date = DateTimeConverter.epochMilli(LocalDate.of(2026, 1, 15));
    var time = DateTimeConverter.minutesSinceMidnight(LocalTime.of(10, 30));
    var createBody = """
        {
          "type": "BORROW",
          "partyName": "John Doe",
          "title": "Personal Loan",
          "description": "Loan for personal use",
          "amount": 10000,
          "interestRate": 5.5,
          "date": %d,
          "time": %d,
          "currency": "IDR"
        }
        """.formatted(date, time);

    var postResponse = Unirest.post(baseUrl + "/v1/loans")
        .header("Authorization", "Bearer " + token)
        .body(createBody)
        .asString();
    assertEquals(200, postResponse.getStatus());
    var postResponseBody = new JSONObject(postResponse.getBody());
    var loanId = postResponseBody.getString("id");
    assertNotNull(loanId);
    assertEquals("BORROW", postResponseBody.getString("type"));
    assertEquals("John Doe", postResponseBody.getString("partyName"));
    assertEquals("Personal Loan", postResponseBody.getString("title"));

    var getResponse = Unirest.get(baseUrl + "/v1/loans/" + loanId)
        .header("Authorization", "Bearer " + token)
        .asString();
    assertEquals(200, getResponse.getStatus());
    var getResponseBody = new JSONObject(getResponse.getBody());
    assertEquals(loanId, getResponseBody.getString("id"));
    assertEquals(USER_ID, getResponseBody.getString("user"));
    assertEquals("BORROW", getResponseBody.getString("type"));
    assertEquals(1768435200000L, getResponseBody.getLong("date"));
    assertEquals(630, getResponseBody.getInt("time"));
    assertEquals("John Doe", getResponseBody.getString("partyName"));
    assertEquals("Personal Loan", getResponseBody.getString("title"));
    assertEquals("Loan for personal use", getResponseBody.getString("description"));
    assertEquals(10000, getResponseBody.getBigDecimal("amount").intValue());
    assertEquals(10000, getResponseBody.getBigDecimal("remainingAmount").intValue());
    assertEquals("IDR", getResponseBody.getString("currency"));
    assertEquals(5.5, getResponseBody.getDouble("interestRate"));
  }

  /**
   * <b>Given</b> a loan ID that does not exist in the system<br>
   * <b>When</b> the GET /loans/{id} endpoint is called<br>
   * <b>Then</b> a 404 status should be returned with an error message
   */
  @Test
  public void getNonExistingLoanShouldReturn404() {
    var getResponse = Unirest.get(baseUrl + "/v1/loans/550e8400-e29b-41d4-a716-446655440001")
        .header("Authorization", "Bearer " + token)
        .asString();
    assertEquals(404, getResponse.getStatus());
    assertEquals("Loan not found", getResponse.getBody());
  }

  /**
   * <b>Given</b> a request without Authorization header<br>
   * <b>When</b> the GET /loans/{id} endpoint is called<br>
   * <b>Then</b> a 401 status should be returned
   */
  @Test
  public void getLoanWithoutAuthorizationHeaderShouldReturn401() {
    var getResponse = Unirest.get(baseUrl + "/v1/loans/some-loan-id")
        .asString();
    assertEquals(401, getResponse.getStatus());
  }

  /**
   * <b>Given</b> a request with an invalid Authorization header<br>
   * <b>When</b> the GET /loans/{id} endpoint is called<br>
   * <b>Then</b> a 401 status should be returned
   */
  @Test
  public void getLoanWithInvalidTokenShouldReturn401() {
    var getResponse = Unirest.get(baseUrl + "/v1/loans/some-loan-id")
        .header("Authorization", "Bearer invalid-token")
        .asString();
    assertEquals(401, getResponse.getStatus());
  }
}
