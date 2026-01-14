package io.dkakunsi.bitapp.money;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.dkakunsi.bitapp.test.AppTestUtil;
import kong.unirest.Unirest;

public class GetUserIT extends AppTestUtil {

  private static final int port = 20003;

  private static GetUserIT sut = new GetUserIT();
  private static String baseUrl;

  @BeforeAll
  static void setup() throws Exception {
    sut.create(Map.of(APP_PORT, Integer.toString(port)));
    sut.startServer(new AppLauncher());

    baseUrl = "http://localhost:" + port;
  }

  @AfterAll
  static void tearDown() throws Exception {
    sut.destroy();
  }

  /**
   * <b>Given</b> a user is registered in the system<br>
   * <b>When</b> the GET /user/{email} endpoint is called with an existing user's
   * email<br>
   * <b>Then</b> the user's complete details should be returned with status 200
   */
  @Test
  public void shouldReturnUserData_WhenUserExists() {
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

    var getResponse = Unirest.get(baseUrl + "/user/john.doe@example.com").asString();
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
  public void shouldReturnEmpty_WhenUserNotExists() {
    var getResponse = Unirest.get(baseUrl + "/user/notexist@example.com").asString();
    assertEquals(200, getResponse.getStatus());
    assertEquals("", getResponse.getBody());
  }
}
