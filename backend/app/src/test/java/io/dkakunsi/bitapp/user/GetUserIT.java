package io.dkakunsi.bitapp.user;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

public class GetUserIT extends AppTestUtil {

  private static final int port = 20003;

  private static GetUserIT sut = new GetUserIT();

  private static String baseUrl;

  private static String token;

  @BeforeAll
  static void setup() throws Exception {
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
   * <b>Given</b> a user is registered in the system<br>
   * <b>When</b> the GET /users/{email} endpoint is called with an existing user's
   * email<br>
   * <b>Then</b> the user's complete details should be returned with status 200
   */
  @Test
  public void getExistingDataShouldBeOk() {
    var body = """
        {
          "name": "John Doe",
          "email": "john.doe@example.com",
          "phone": "1234567890",
          "photoUrl": "http://example.com/photo.jpg"
        }
        """;

    var postResponse = Unirest.post(baseUrl + "/users").body(body)
        .asString();
    assertEquals(200, postResponse.getStatus());
    var postResponseBody = new JSONObject(postResponse.getBody());
    assertEquals("John Doe", postResponseBody.getString("name"));
    assertEquals("john.doe@example.com", postResponseBody.getString("email"));
    assertEquals("1234567890", postResponseBody.getString("phone"));
    assertEquals("http://example.com/photo.jpg", postResponseBody.getString("photoUrl"));
    assertEquals("EN", postResponseBody.getString("language"));

    var getResponse = Unirest.get(baseUrl + "/users/john.doe@example.com")
        .header("Authorization", "Bearer " + token)
        .asString();
    assertEquals(200, getResponse.getStatus());
    var getResponseBody = new JSONObject(getResponse.getBody());
    assertEquals("John Doe", getResponseBody.getString("name"));
    assertEquals("john.doe@example.com", getResponseBody.getString("email"));
    assertEquals("1234567890", getResponseBody.getString("phone"));
    assertEquals("http://example.com/photo.jpg", getResponseBody.getString("photoUrl"));
    assertEquals("EN", getResponseBody.getString("language"));
  }

  /**
   * <b>Given</b> a user email that does not exist in the system<br>
   * <b>When</b> the GET /user/{email} endpoint is called<br>
   * <b>Then</b> an empty response should be returned with status 200
   */
  @Test
  public void getNonExistingUserShouldReturn404() {
    var getResponse = Unirest.get(baseUrl + "/users/notexist@example.com")
        .header("Authorization", "Bearer " + token)
        .asString();
    assertEquals(404, getResponse.getStatus());
    assertEquals("User not found", getResponse.getBody());
  }

  /**
   * <b>Given</b> a request without Authorization header<br>
   * <b>When</b> the GET /user/{email} endpoint is called<br>
   * <b>Then</b> an empty response should be returned with status 401
   */
  @Test
  public void getUserWithoutAuthorizationHeaderShouldReturn401() {
    var getResponse = Unirest.get(baseUrl + "/users/test@example.com")
        .asString();
    assertEquals(401, getResponse.getStatus());
  }

  /**
   * <b>Given</b> a request with an invalid Authorization header<br>
   * <b>When</b> the GET /user/{email} endpoint is called<br>
   * <b>Then</b> an empty response should be returned with status 401
   */
  @Test
  public void getUserWithInvalidTokenShouldReturn401() {
    var getResponse = Unirest.get(baseUrl + "/users/test@example.com")
        .header("Authorization", "Bearer invalid-token")
        .asString();
    assertEquals(401, getResponse.getStatus());
  }
}
