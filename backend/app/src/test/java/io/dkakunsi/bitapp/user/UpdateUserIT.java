package io.dkakunsi.bitapp.user;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

public class UpdateUserIT extends AppTestUtil {

  private static final int port = 20006;

  private static UpdateUserIT sut = new UpdateUserIT();

  private static String baseUrl;

  @BeforeAll
  static void setup() throws Exception {
    sut.create(Map.of(APP_PORT, Integer.toString(port),
        JWTAuthorizer.JWT_PUBLIC_KEY, SecureTestUtil.PUBLIC_KEY));
    sut.startServer(new AppLauncher());

    baseUrl = "http://localhost:" + port;
  }

  @AfterAll
  static void tearDown() throws Exception {
    sut.destroy();
  }

  /**
   * <b>Given</b> a registered user with default language EN<br>
   * <b>When</b> the PUT /users/{email} endpoint is called with
   * language ID<br>
   * <b>Then</b> the user's language should be updated to ID with status 200 and
   * verified via GET
   */
  @Test
  public void updateUserLanguageShouldBeOk() {
    // First, register a user
    var registerBody = """
        {
          "name": "Jane Doe",
          "email": "jane.doe@example.com",
          "phone": "9876543210",
          "photoUrl": "http://example.com/jane.jpg"
        }
        """;

    var registerResponse = Unirest.post(baseUrl + "/v1/users").body(registerBody).asString();
    assertEquals(200, registerResponse.getStatus());
    var registerResponseBody = new JSONObject(registerResponse.getBody());
    assertEquals("EN", registerResponseBody.getString("language"));

    // Update the language to ID
    var updateBody = """
        {
          "language": "ID"
        }
        """;
    var janeToken = SecureTestUtil.generateToken("jane.doe@example.com");
    var updateResponse = Unirest.put(baseUrl + "/v1/users/jane.doe@example.com")
        .header("Authorization", "Bearer " + janeToken)
        .body(updateBody)
        .asString();
    assertEquals(200, updateResponse.getStatus());
    var updateResponseBody = new JSONObject(updateResponse.getBody());
    assertEquals("jane.doe@example.com", updateResponseBody.getString("email"));
    assertEquals("ID", updateResponseBody.getString("language"));

    // Verify the language was updated by getting the user
    var getResponse = Unirest.get(baseUrl + "/v1/users/jane.doe@example.com")
        .header("Authorization", "Bearer " + janeToken)
        .asString();
    assertEquals(200, getResponse.getStatus());
    var getResponseBody = new JSONObject(getResponse.getBody());
    assertEquals("Jane Doe", getResponseBody.getString("name"));
    assertEquals("jane.doe@example.com", getResponseBody.getString("email"));
    assertEquals("ID", getResponseBody.getString("language"));
  }

  /**
   * <b>Given</b> a registered user with language ID<br>
   * <b>When</b> the PUT /users/{email} endpoint is called with
   * language EN<br>
   * <b>Then</b> the user's language should be updated back to EN with status 200
   * and verified via GET
   */
  @Test
  public void updateUserLanguageToEnShouldBeOk() {
    // First, register a user
    var registerBody = """
        {
          "name": "Bob Smith",
          "email": "bob.smith@example.com",
          "phone": "5551234567",
          "photoUrl": "http://example.com/bob.jpg"
        }
        """;

    var registerResponse = Unirest.post(baseUrl + "/v1/users").body(registerBody).asString();
    assertEquals(200, registerResponse.getStatus());

    // Update to ID first
    var updateBodyId = """
        {
          "language": "ID"
        }
        """;
    var bobToken = SecureTestUtil.generateToken("bob.smith@example.com");
    var updateToIdResponse = Unirest.put(baseUrl + "/v1/users/bob.smith@example.com")
        .header("Authorization", "Bearer " + bobToken)
        .body(updateBodyId)
        .asString();
    assertEquals(200, updateToIdResponse.getStatus());
    var updateToIdResponseBody = new JSONObject(updateToIdResponse.getBody());
    assertEquals("ID", updateToIdResponseBody.getString("language"));

    // Update back to EN
    var updateBodyEn = """
        {
          "language": "EN"
        }
        """;
    var updateToEnResponse = Unirest.put(baseUrl + "/v1/users/bob.smith@example.com")
        .header("Authorization", "Bearer " + bobToken)
        .body(updateBodyEn)
        .asString();
    assertEquals(200, updateToEnResponse.getStatus());
    var updateToEnResponseBody = new JSONObject(updateToEnResponse.getBody());
    assertEquals("bob.smith@example.com", updateToEnResponseBody.getString("email"));
    assertEquals("EN", updateToEnResponseBody.getString("language"));

    // Verify the final state
    var getResponse = Unirest.get(baseUrl + "/v1/users/bob.smith@example.com")
        .header("Authorization", "Bearer " + bobToken)
        .asString();
    assertEquals(200, getResponse.getStatus());
    var getResponseBody = new JSONObject(getResponse.getBody());
    assertEquals("EN", getResponseBody.getString("language"));
  }

  /**
   * <b>Given</b> a user email that does not exist in the system<br>
   * <b>When</b> the PUT /users/{email} endpoint is called<br>
   * <b>Then</b> the request should fail with status 404 and "User not found"
   * message
   */
  @Test
  public void updateUserLanguageNonExistingUserShouldReturn404() {
    var updateBody = """
        {
          "language": "ID"
        }
        """;
    var nonExistentUserToken = SecureTestUtil.generateToken("nonexistent@example.com");
    var updateResponse = Unirest.put(baseUrl + "/v1/users/nonexistent@example.com")
        .header("Authorization", "Bearer " + nonExistentUserToken)
        .body(updateBody)
        .asString();
    assertEquals(404, updateResponse.getStatus());
    assertEquals("User not found", updateResponse.getBody());
  }

  /**
   * <b>Given</b> a registered user with language EN<br>
   * <b>When</b> the PUT /users/{email} endpoint is called with
   * an unsupported language code<br>
   * <b>Then</b> the request should fail with status 400
   */
  @Test
  public void updateUserLanguageWithUnsupportedLanguageShouldReturn400() {
    // First, register a user
    var registerBody = """
        {
          "name": "Alice Johnson",
          "email": "alice.johnson@example.com",
          "phone": "5559876543",
          "photoUrl": "http://example.com/alice.jpg"
        }
        """;

    var registerResponse = Unirest.post(baseUrl + "/v1/users").body(registerBody).asString();
    assertEquals(200, registerResponse.getStatus());

    // Try to update to an unsupported language
    var updateBodyUnsupported = """
        {
          "language": "FR"
        }
        """;
    var aliceToken = SecureTestUtil.generateToken("alice.johnson@example.com");
    var updateResponse = Unirest.put(baseUrl + "/v1/users/alice.johnson@example.com")
        .header("Authorization", "Bearer " + aliceToken)
        .body(updateBodyUnsupported)
        .asString();
    assertEquals(400, updateResponse.getStatus());
    assertEquals("language: invalid value", updateResponse.getBody());
  }

  /**
   * <b>Given</b> a registered user<br>
   * <b>When</b> the PUT /users/{email} endpoint is called without
   * an authorization header<br>
   * <b>Then</b> the request should fail with status 401
   */
  @Test
  public void updateUserLanguageWithoutAuthorizationHeaderShouldReturn401() {
    // First, register a user
    var registerBody = """
        {
          "name": "Charlie Brown",
          "email": "charlie.brown@example.com",
          "phone": "5551112222",
          "photoUrl": "http://example.com/charlie.jpg"
        }
        """;

    var registerResponse = Unirest.post(baseUrl + "/v1/users").body(registerBody).asString();
    assertEquals(200, registerResponse.getStatus());

    // Try to update language without authorization header
    var updateBodyId = """
        {
          "language": "ID"
        }
        """;
    var updateResponse = Unirest.put(baseUrl + "/v1/users/charlie.brown@example.com")
        .body(updateBodyId)
        .asString();
    assertEquals(401, updateResponse.getStatus());
  }

  /**
   * <b>Given</b> a registered user<br>
   * <b>When</b> the PUT /users/{email} endpoint is called with
   * an invalid authorization token<br>
   * <b>Then</b> the request should fail with status 401
   */
  @Test
  public void updateUserLanguageWithInvalidTokenShouldReturn401() {
    // First, register a user
    var registerBody = """
        {
          "name": "Diana Prince",
          "email": "diana.prince@example.com",
          "phone": "5553334444",
          "photoUrl": "http://example.com/diana.jpg"
        }
        """;

    var registerResponse = Unirest.post(baseUrl + "/v1/users").body(registerBody).asString();
    assertEquals(200, registerResponse.getStatus());

    // Try to update language with invalid token
    var updateBodyId = """
        {
          "language": "ID"
        }
        """;
    var updateResponse = Unirest.put(baseUrl + "/v1/users/diana.prince@example.com")
        .header("Authorization", "Bearer invalid-token")
        .body(updateBodyId)
        .asString();
    assertEquals(401, updateResponse.getStatus());
  }
}
