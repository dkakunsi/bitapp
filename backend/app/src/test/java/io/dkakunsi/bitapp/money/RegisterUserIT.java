package io.dkakunsi.bitapp.money;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.dkakunsi.bitapp.jwt.JWTAuthorizer;
import io.dkakunsi.bitapp.test.AppTestUtil;
import io.dkakunsi.bitapp.test.SecureTestUtil;
import kong.unirest.Unirest;
import kong.unirest.json.JSONObject;

public class RegisterUserIT extends AppTestUtil {

  private static final int port = 20004;

  private static RegisterUserIT sut = new RegisterUserIT();

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
   * <b>Given</b> a valid user registration request with all required fields<br>
   * <b>When</b> the POST /users endpoint is called and the user is retrieved<br>
   * <b>Then</b> the user should be created with status 200 and default language
   * EN, and retrievable via GET
   */
  @Test
  public void givenValidRegisterRequest_WhenSent_ThenShouldSuccess() {
    var body = """
        {
          "name": "John Doe",
          "email": "john.doe@example.com",
          "phone": "1234567890",
          "photoUrl": "http://example.com/photo.jpg"
        }
        """;

    var postResponse = Unirest.post(baseUrl + "/users").body(body).asString();
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
   * <b>Given</b> a user registration request with an invalid email format<br>
   * <b>When</b> the POST /users endpoint is called<br>
   * <b>Then</b> the request should fail with status 400 and "Invalid data"
   * message
   */
  @Test
  public void givenInvalidEmailOnRegisterRequest_WhenSent_ThenShouldFailWithBadRequest() {
    var body = """
        {
          "name": "John Doe",
          "email": "john.doe",
          "phone": "1234567890",
          "photoUrl": "http://example.com/photo.jpg"
        }
        """;
    var response = Unirest.post(baseUrl + "/users").body(body).asString();

    assertEquals(400, response.getStatus());
    assertEquals("email: must be a well-formed email address", response.getBody());
  }

  /**
   * <b>Given</b> a user registration request with invalid name (null, empty, or
   * whitespace)<br>
   * <b>When</b> the POST /users endpoint is called<br>
   * <b>Then</b> the request should fail with status 400 and validation message
   */
  @Test
  public void givenNullNameOnRegisterRequest_WhenSent_ThenShouldFailWithBadRequest() {
    var body = """
        {
          "name": null,
          "email": "john.doe@example.com",
          "phone": "1234567890",
          "photoUrl": "http://example.com/photo.jpg"
        }
        """;
    var response = Unirest.post(baseUrl + "/users").body(body).asString();

    assertEquals(400, response.getStatus());
    assertEquals("name: must not be blank", response.getBody());
  }

  @Test
  public void givenEmptyNameOnRegisterRequest_WhenSent_ThenShouldFailWithBadRequest() {
    var body = """
        {
          "name": "",
          "email": "john.doe@example.com",
          "phone": "1234567890",
          "photoUrl": "http://example.com/photo.jpg"
        }
        """;
    var response = Unirest.post(baseUrl + "/users").body(body).asString();

    assertEquals(400, response.getStatus());
    assertEquals("name: must not be blank", response.getBody());
  }

  @Test
  public void givenWhitespaceNameOnRegisterRequest_WhenSent_ThenShouldFailWithBadRequest() {
    var body = """
        {
          "name": "   ",
          "email": "john.doe@example.com",
          "phone": "1234567890",
          "photoUrl": "http://example.com/photo.jpg"
        }
        """;
    var response = Unirest.post(baseUrl + "/users").body(body).asString();

    assertEquals(400, response.getStatus());
    assertEquals("name: must not be blank", response.getBody());
  }
}
