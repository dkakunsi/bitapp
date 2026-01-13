package io.dkakunsi.lab.javalin.endpoint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.dkakunsi.bitapp.account.dto.UpdateAccountInput;
import io.dkakunsi.bitapp.account.dto.UpdateAccountResult;
import io.dkakunsi.bitapp.account.usecase.UpdateAccount;
import io.dkakunsi.bitapp.common.AppError.Code;
import io.dkakunsi.bitapp.common.Context;
import io.dkakunsi.bitapp.common.usecase.Result;
import io.dkakunsi.lab.javalin.JavalinServer;
import kong.unirest.Unirest;

class UpdateAccountJavalinEndpointTest {

  private static final int PORT = 20005;

  private static String baseUrl;

  private static final String ACCOUNT_ID = "account123";

  private static UpdateAccount usecase;

  private static JavalinServer server;

  @BeforeAll
  static void setup() throws Exception {
    baseUrl = "http://localhost:" + PORT;
    usecase = mock(UpdateAccount.class);
    var endpoint = new UpdateAccountJavalinEndpoint(usecase)
        .withValidator();
    server = JavalinServer.of(PORT);
    server.addEndpoint(endpoint);
    server.start();
  }

  @AfterAll
  static void teardown() throws Exception {
    server.stop();
  }

  @AfterEach
  void resetMocks() {
    reset(usecase);
  }

  @Test
  void givenValidUpdateRequestWhenProcessedThenShouldReturn200WithUpdatedAccount() {
    // Given
    var updateResult = UpdateAccountResult.builder()
        .id(ACCOUNT_ID)
        .name("Updated Account")
        .type("CASH")
        .themeColor("#00FF00")
        .build();

    var result = Result.success(updateResult);
    when(usecase.process(any(Context.class), any(UpdateAccountInput.class))).thenReturn(result);

    var requestBody = """
        {
          "name": "Updated Account",
          "type": "CASH",
          "themeColor": "#00FF00"
        }
        """;

    // When
    var response = Unirest.put(baseUrl + "/accounts/" + ACCOUNT_ID)
        .header("Content-Type", "application/json")
        .body(requestBody)
        .asJson();

    // Then
    assertEquals(200, response.getStatus());
    var json = response.getBody().getObject();
    assertNotNull(json);
    assertEquals(ACCOUNT_ID, json.getString("id"));
    assertEquals("Updated Account", json.getString("name"));
    assertEquals("CASH", json.getString("type"));
    assertEquals("#00FF00", json.getString("themeColor"));
  }

  @Test
  void givenPartialUpdateWithOnlyNameWhenProcessedThenShouldReturn200() {
    // Given
    var updateResult = UpdateAccountResult.builder()
        .id(ACCOUNT_ID)
        .name("New Name")
        .type("BANK")
        .themeColor("#FF5733")
        .build();

    var result = Result.success(updateResult);
    when(usecase.process(any(Context.class), any(UpdateAccountInput.class))).thenReturn(result);

    var requestBody = """
        {
          "name": "New Name"
        }
        """;

    // When
    var response = Unirest.put(baseUrl + "/accounts/" + ACCOUNT_ID)
        .header("Content-Type", "application/json")
        .body(requestBody)
        .asJson();

    // Then
    assertEquals(200, response.getStatus());
    var json = response.getBody().getObject();
    assertEquals("New Name", json.getString("name"));
  }

  @Test
  void givenUpdateRequestWhenUseCaseReturnsErrorThenShouldReturn500() {
    // Given
    var result = Result.<UpdateAccountResult>failure(Code.SERVER_ERROR, "Update failed");
    when(usecase.process(any(Context.class), any(UpdateAccountInput.class))).thenReturn(result);

    var requestBody = """
        {
          "name": "Updated Account"
        }
        """;

    // When
    var response = Unirest.put(baseUrl + "/accounts/" + ACCOUNT_ID)
        .header("Content-Type", "application/json")
        .body(requestBody)
        .asString();

    // Then
    assertEquals(500, response.getStatus());
  }

  @Test
  void givenInvalidAccountIdInPathWhenProcessedThenShouldStillPassToUseCase() {
    // Given - ID will be validated at use case or repository level
    var updateResult = UpdateAccountResult.builder()
        .id("invalid-id")
        .name("Test")
        .type("BANK")
        .themeColor("#000000")
        .build();

    var result = Result.success(updateResult);
    when(usecase.process(any(Context.class), any(UpdateAccountInput.class))).thenReturn(result);

    var requestBody = """
        {
          "name": "Test"
        }
        """;

    // When
    var response = Unirest.put(baseUrl + "/accounts/invalid-id")
        .header("Content-Type", "application/json")
        .body(requestBody)
        .asJson();

    // Then
    assertEquals(200, response.getStatus());
  }
}
