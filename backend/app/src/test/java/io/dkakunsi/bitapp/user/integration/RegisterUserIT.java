package io.dkakunsi.bitapp.user.integration;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import dev.langchain4j.model.chat.ChatModel;
import io.dkakunsi.bitapp.AppLauncher;
import io.dkakunsi.bitapp.jwt.JWTAuthorizer;
import io.dkakunsi.bitapp.test.AppTestUtil;
import io.dkakunsi.bitapp.test.SecureTestUtil;
import kong.unirest.Unirest;
import kong.unirest.json.JSONObject;

public class RegisterUserIT extends AppTestUtil {

  private static RegisterUserIT sut = new RegisterUserIT();

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
   * <b>Given</b> a valid user registration request with all required fields<br>
   * <b>When</b> the POST /users endpoint is called and the user is retrieved<br>
   * <b>Then</b> the user should be created with status 200 and default language
   * EN, and retrievable via GET
   */
  @Test
  public void registerUserShouldBeOk() {
    var body = """
        {
          "name": "John Doe",
          "email": "john.doe@example.com",
          "phone": "1234567890",
          "photoUrl": "http://example.com/photo.jpg"
        }
        """;

    var postResponse = Unirest.post(baseUrl + "/v1/users").body(body).asString();
    assertEquals(200, postResponse.getStatus());
    var postResponseBody = new JSONObject(postResponse.getBody());
    assertEquals("John Doe", postResponseBody.getString("name"));
    assertEquals("john.doe@example.com", postResponseBody.getString("email"));
    assertEquals("1234567890", postResponseBody.getString("phone"));
    assertEquals("http://example.com/photo.jpg", postResponseBody.getString("photoUrl"));
    assertEquals("EN", postResponseBody.getString("language"));

    var getResponse = Unirest.get(baseUrl + "/v1/users/john.doe@example.com")
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
   * <b>Given</b> a valid user registration request without phone and photoUrl
   * fields<br>
   * <b>When</b> the POST /users endpoint is called and the user is retrieved<br>
   * <b>Then</b> the user should be created with status 200 and default language
   * EN, and retrievable via GET
   */
  @Test
  public void registerUserWithoutPhoneAndPhotoUrlShouldBeOk() {
    var body = """
        {
          "name": "Jane Doe",
          "email": "jane.doe@example.com"
        }
        """;

    var postResponse = Unirest.post(baseUrl + "/v1/users").body(body).asString();
    assertEquals(200, postResponse.getStatus());
    var postResponseBody = new JSONObject(postResponse.getBody());
    assertEquals("Jane Doe", postResponseBody.getString("name"));
    assertEquals("jane.doe@example.com", postResponseBody.getString("email"));
    assertTrue(postResponseBody.isNull("phone"));
    assertTrue(postResponseBody.isNull("photoUrl"));
    assertEquals("EN", postResponseBody.getString("language"));

    var getResponse = Unirest.get(baseUrl + "/v1/users/jane.doe@example.com")
        .header("Authorization", "Bearer " + token)
        .asString();
    assertEquals(200, getResponse.getStatus());
    var getResponseBody = new JSONObject(getResponse.getBody());
    assertEquals("Jane Doe", getResponseBody.getString("name"));
    assertEquals("jane.doe@example.com", getResponseBody.getString("email"));
    assertTrue(getResponseBody.isNull("phone"));
    assertTrue(getResponseBody.isNull("photoUrl"));
    assertEquals("EN", getResponseBody.getString("language"));
  }

  /**
   * <b>Given</b> a user registration request with null value on email<br>
   * <b>When</b> the POST /users endpoint is called<br>
   * <b>Then</b> the request should fail with status 400 and validation message
   * mentioning that the email invalid value
   */
  @Test
  public void registerUserWithNullEmailShouldFailWithBadRequest() {
    var body = """
        {
          "name": "John Doe",
          "email": null,
          "phone": "1234567890",
          "photoUrl": "http://example.com/photo.jpg"
        }
        """;
    var response = Unirest.post(baseUrl + "/v1/users").body(body).asString();

    assertEquals(400, response.getStatus());
    assertTrue(response.getBody().startsWith("email: invalid value"));
  }

  /**
   * <b>Given</b> a user registration request with empty value on email<br>
   * <b>When</b> the POST /users endpoint is called<br>
   * <b>Then</b> the request should fail with status 400 and validation message
   * mentioning that the email invalid value
   */
  @Test
  public void registerUserWithEmptyEmailShouldFailWithBadRequest() {
    var body = """
        {
          "name": "John Doe",
          "email": "",
          "phone": "1234567890",
          "photoUrl": "http://example.com/photo.jpg"
        }
        """;
    var response = Unirest.post(baseUrl + "/v1/users").body(body).asString();

    assertEquals(400, response.getStatus());
    assertTrue(response.getBody().startsWith("email: invalid value"));
  }

  /**
   * <b>Given</b> a user registration request with whitespace value on email<br>
   * <b>When</b> the POST /users endpoint is called<br>
   * <b>Then</b> the request should fail with status 400 and validation message
   * mentioning that the email invalid value and must be well-formed
   */
  @Test
  public void registerUserWithBlankEmailShouldFailWithBadRequest() {
    var body = """
        {
          "name": "John Doe",
          "email": "   ",
          "phone": "1234567890",
          "photoUrl": "http://example.com/photo.jpg"
        }
        """;
    var response = Unirest.post(baseUrl + "/v1/users").body(body).asString();

    assertEquals(400, response.getStatus());
    assertTrue(response.getBody().contains("email: invalid value"));
    assertTrue(response.getBody().contains("email: invalid value"));
  }

  /**
   * <b>Given</b> a user registration request with an invalid email format<br>
   * <b>When</b> the POST /users endpoint is called<br>
   * <b>Then</b> the request should fail with status 400 and validation
   * message mentioning that the email is not well-formed
   */
  @Test
  public void registerUserWithInvalidEmailShouldFailWithBadRequest() {
    var body = """
        {
          "name": "John Doe",
          "email": "john.doe",
          "phone": "1234567890",
          "photoUrl": "http://example.com/photo.jpg"
        }
        """;
    var response = Unirest.post(baseUrl + "/v1/users").body(body).asString();

    assertEquals(400, response.getStatus());
    assertTrue(response.getBody().startsWith("email: invalid value"));
  }

  /**
   * <b>Given</b> a user registration request with null value on name<br>
   * <b>When</b> the POST /users endpoint is called<br>
   * <b>Then</b> the request should fail with status 400 and validation message
   * mentioning
   * that the name invalid value
   */
  @Test
  public void registerUserWithNullNameShouldFailWithBadRequest() {
    var body = """
        {
          "name": null,
          "email": "john.doe@example.com",
          "phone": "1234567890",
          "photoUrl": "http://example.com/photo.jpg"
        }
        """;
    var response = Unirest.post(baseUrl + "/v1/users").body(body).asString();

    assertEquals(400, response.getStatus());
    assertTrue(response.getBody().startsWith("name: invalid value"));
  }

  /**
   * <b>Given</b> a user registration request with empty value on name<br>
   * <b>When</b> the POST /users endpoint is called<br>
   * <b>Then</b> the request should fail with status 400 and validation message
   * mentioning
   * that the name invalid value
   */
  @Test
  public void registerUserWithEmptyNameShouldFailWithBadRequest() {
    var body = """
        {
          "name": "",
          "email": "john.doe@example.com",
          "phone": "1234567890",
          "photoUrl": "http://example.com/photo.jpg"
        }
        """;
    var response = Unirest.post(baseUrl + "/v1/users").body(body).asString();

    assertEquals(400, response.getStatus());
    assertTrue(response.getBody().startsWith("name: invalid value"));
  }

  /**
   * <b>Given</b> a user registration request with whitespace value on name<br>
   * <b>When</b> the POST /users endpoint is called<br>
   * <b>Then</b> the request should fail with status 400 and validation message
   * mentioning
   * that the name invalid value
   */
  @Test
  public void registerUserWithBlankNameShouldFailWithBadRequest() {
    var body = """
        {
          "name": "   ",
          "email": "john.doe@example.com",
          "phone": "1234567890",
          "photoUrl": "http://example.com/photo.jpg"
        }
        """;
    var response = Unirest.post(baseUrl + "/v1/users").body(body).asString();

    assertEquals(400, response.getStatus());
    assertTrue(response.getBody().startsWith("name: invalid value"));
  }
}
