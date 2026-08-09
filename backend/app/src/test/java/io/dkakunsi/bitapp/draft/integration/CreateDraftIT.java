package io.dkakunsi.bitapp.draft.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.langchain4j.model.chat.ChatModel;
import io.dkakunsi.bitapp.AppLauncher;
import io.dkakunsi.bitapp.jwt.JWTAuthorizer;
import io.dkakunsi.bitapp.test.AppTestUtil;
import io.dkakunsi.bitapp.test.SecureTestUtil;
import kong.unirest.Unirest;

public class CreateDraftIT extends AppTestUtil {

  private static final ChatModel chatModel = mock(ChatModel.class);

  private static final CreateDraftIT sut = new CreateDraftIT();

  private static String baseUrl;

  private static String token;

  @BeforeAll
  static void setup() throws Exception {
    var port = getPort();
    AppTestUtil.setTestDependency(ChatModel.class, chatModel);

    var appEnv = Map.of(
        APP_PORT, Integer.toString(port),
        JWTAuthorizer.JWT_PUBLIC_KEY, SecureTestUtil.PUBLIC_KEY);

    sut.create(appEnv);
    sut.startServer(new AppLauncher());

    baseUrl = "http://localhost:" + port;
    token = SecureTestUtil.generateToken(USER_ID);
  }

  @AfterAll
  static void tearDown() throws Exception {
    sut.destroy();
  }

  @BeforeEach
  void resetModel() {
    reset(chatModel);
  }

  /**
   * <b>Given</b> a valid create draft request<br>
   * <b>When</b> the POST /chats endpoint is called<br>
   * <b>Then</b> a new draft should be created with parsed model result
   */
  @Test
  public void createDraftShouldBeOk() {
    when(chatModel.chat(anyString())).thenReturn("""
        {
          "data": {
            "name": "Food Account",
            "type": "BANK",
            "themeColor": "#FFFFFF"
          }
        }
        """);

    var body = """
        {
          "type": "ACCOUNT",
          "draftId": "draft-seed-1",
          "message": "create bank account for food",
          "language": "en"
        }
        """;

    var response = Unirest.post(baseUrl + "/v1/chats")
        .header("Authorization", "Bearer " + token)
        .body(body)
        .asString();

    assertEquals(200, response.getStatus());

    var responseBody = new JSONObject(response.getBody());
    assertEquals("ACCOUNT", responseBody.getString("type"));
    assertTrue(responseBody.getBoolean("success"));
    assertFalse(responseBody.getBoolean("confirmed"));

    assertNotNull(getIdValue(responseBody.get("id")));
    assertEquals(USER_ID, getIdValue(responseBody.get("userId")));

    var modelResult = responseBody.getJSONObject("modelResult");
    assertTrue(modelResult.length() > 0);

    assertEquals(0, responseBody.getJSONArray("crossDomainReferences").length());
  }

  private static String getIdValue(Object value) {
    if (value instanceof JSONObject jsonObject) {
      return jsonObject.getString("value");
    }
    return value.toString();
  }
}