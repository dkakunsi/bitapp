package io.dkakunsi.bitapp.money.user;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.dkakunsi.bitapp.jwt.JWTAuthorizer;
import io.dkakunsi.bitapp.money.AppLauncher;
import io.dkakunsi.bitapp.test.AppTestUtil;
import io.dkakunsi.bitapp.test.SecureTestUtil;
import kong.unirest.Unirest;
import kong.unirest.json.JSONObject;

public class UpdateUserLanguageIT extends AppTestUtil {

  private static final int port = 20006;

  private static UpdateUserLanguageIT sut = new UpdateUserLanguageIT();

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
   * <b>When</b> the PATCH /users/{email}/language endpoint is called with
   * language ID<br>
   * <b>Then</b> the user's language should be updated to ID with status 200 and
   * verified via GET
   */
  @Test
  public void givenValidUpdateLanguageRequest_WhenUserExists_ThenShouldUpdateLanguage() {
    // First, register a user
    var registerBody = """
        {
          "name": "Jane Doe",
          "email": "jane.doe@example.com",
          "phone": "9876543210",
          "photoUrl": "http://example.com/jane.jpg"
        }
        """;

    var registerResponse = Unirest.post(baseUrl + "/users").body(registerBody).asString();
    assertEquals(200, registerResponse.getStatus());
    var registerResponseBody = new JSONObject(registerResponse.getBody());
    assertEquals("EN", registerResponseBody.getString("language"));

    // Update the language to ID
    var janeToken = SecureTestUtil.generateToken("jane.doe@example.com");
    var updateResponse = Unirest.patch(baseUrl + "/users/jane.doe@example.com/language/ID")
        .header("Authorization", "Bearer " + janeToken)
        .asString();
    assertEquals(200, updateResponse.getStatus());
    var updateResponseBody = new JSONObject(updateResponse.getBody());
    assertEquals("jane.doe@example.com", updateResponseBody.getString("email"));
    assertEquals("ID", updateResponseBody.getString("language"));

    // Verify the language was updated by getting the user
    var getResponse = Unirest.get(baseUrl + "/users/jane.doe@example.com")
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
   * <b>When</b> the PATCH /users/{email}/language endpoint is called with
   * language EN<br>
   * <b>Then</b> the user's language should be updated back to EN with status 200
   * and verified via GET
   */
  @Test
  public void givenValidUpdateLanguageRequest_WhenUpdatingToEN_ThenShouldUpdateLanguage() {
    // First, register a user
    var registerBody = """
        {
          "name": "Bob Smith",
          "email": "bob.smith@example.com",
          "phone": "5551234567",
          "photoUrl": "http://example.com/bob.jpg"
        }
        """;

    var registerResponse = Unirest.post(baseUrl + "/users").body(registerBody).asString();
    assertEquals(200, registerResponse.getStatus());

    // Update to ID first
    var bobToken = SecureTestUtil.generateToken("bob.smith@example.com");
    var updateToIdResponse = Unirest.patch(baseUrl + "/users/bob.smith@example.com/language/ID")
        .header("Authorization", "Bearer " + bobToken)
        .asString();
    assertEquals(200, updateToIdResponse.getStatus());
    var updateToIdResponseBody = new JSONObject(updateToIdResponse.getBody());
    assertEquals("ID", updateToIdResponseBody.getString("language"));

    // Update back to EN
    var updateToEnResponse = Unirest.patch(baseUrl + "/users/bob.smith@example.com/language/EN")
        .header("Authorization", "Bearer " + bobToken)
        .asString();
    assertEquals(200, updateToEnResponse.getStatus());
    var updateToEnResponseBody = new JSONObject(updateToEnResponse.getBody());
    assertEquals("bob.smith@example.com", updateToEnResponseBody.getString("email"));
    assertEquals("EN", updateToEnResponseBody.getString("language"));

    // Verify the final state
    var getResponse = Unirest.get(baseUrl + "/users/bob.smith@example.com")
        .header("Authorization", "Bearer " + bobToken)
        .asString();
    assertEquals(200, getResponse.getStatus());
    var getResponseBody = new JSONObject(getResponse.getBody());
    assertEquals("EN", getResponseBody.getString("language"));
  }

  /**
   * <b>Given</b> a user email that does not exist in the system<br>
   * <b>When</b> the PATCH /users/{email}/language endpoint is called<br>
   * <b>Then</b> the request should fail with status 404 and "User not found"
   * message
   */
  @Test
  public void givenUpdateLanguageRequest_WhenUserDoesNotExist_ThenShouldReturn404() {
    var nonExistentUserToken = SecureTestUtil.generateToken("nonexistent@example.com");
    var updateResponse = Unirest.patch(baseUrl + "/users/nonexistent@example.com/language/ID")
        .header("Authorization", "Bearer " + nonExistentUserToken)
        .asString();
    assertEquals(404, updateResponse.getStatus());
    assertEquals("User not found", updateResponse.getBody());
  }

  /**
   * <b>Given</b> a registered user with language EN<br>
   * <b>When</b> the PATCH /users/{email}/language endpoint is called with
   * an unsupported language code<br>
   * <b>Then</b> the request should fail with status 400
   */
  @Test
  public void givenUpdateLanguageRequest_WhenLanguageIsUnsupported_ThenShouldReturn400() {
    // First, register a user
    var registerBody = """
        {
         "name": "Alice Johnson",
         "email": "alice.johnson@example.com",
         "phone": "5559876543",
         "photoUrl": "http://example.com/alice.jpg"
        }
        """;

    var registerResponse = Unirest.post(baseUrl + "/users").body(registerBody).asString();
    assertEquals(200, registerResponse.getStatus());

    // Try to update to an unsupported language
    var aliceToken = SecureTestUtil.generateToken("alice.johnson@example.com");
    var updateResponse = Unirest.patch(baseUrl + "/users/alice.johnson@example.com/language/FR")
        .header("Authorization", "Bearer " + aliceToken)
        .asString();
    assertEquals(400, updateResponse.getStatus());
    assertEquals("Invalid language: FR", updateResponse.getBody());
  }
}
