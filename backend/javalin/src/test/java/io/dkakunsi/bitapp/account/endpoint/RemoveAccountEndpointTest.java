package io.dkakunsi.bitapp.account.endpoint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.dkakunsi.bitapp.account.dto.AccountResult;
import io.dkakunsi.bitapp.account.usecase.RemoveAccount;
import io.dkakunsi.bitapp.common.AppError.Code;
import io.dkakunsi.bitapp.domain.usecase.Result;
import io.dkakunsi.bitapp.javalin.JavalinServer;
import kong.unirest.Unirest;

class RemoveAccountEndpointTest {

  private static final int PORT = 20018;

  private static String baseUrl;

  private static RemoveAccount usecase;

  private static JavalinServer server;

  @BeforeAll
  static void setup() throws Exception {
    baseUrl = "http://localhost:" + PORT;
    usecase = mock(RemoveAccount.class);
    var endpoint = new RemoveAccountEndpoint(usecase);
    server = JavalinServer.of(PORT);
    server.addEndpoint(endpoint);
    server.start();
  }

  @AfterAll
  static void destroy() {
    server.stop();
  }

  @Test
  void givenValidAccountId_WhenDeleted_ThenShouldReturnAccountDetails() {
    // Given
    var accountId = "account-123";
    var accountResult = AccountResult.builder()
        .id(accountId)
        .name("Test Account")
        .type("CASH")
        .themeColor("#FFFFFF")
        .balance(new BigDecimal("5000"))
        .user("user@email.com")
        .build();
    var result = Result.success(accountResult);
    when(usecase.process(any(String.class))).thenReturn(result);

    // When
    var response = Unirest.delete(baseUrl + "/accounts/" + accountId).asString();

    // Then
    assertEquals(200, response.getStatus());

    var responseBody = response.getBody();
    assertNotNull(responseBody);
    var resultBody = new JSONObject(responseBody);
    assertEquals(accountId, resultBody.getString("id"));
    assertEquals("Test Account", resultBody.getString("name"));
    assertEquals("CASH", resultBody.getString("type"));
  }

  @Test
  void givenNonExistentAccountId_WhenDeleted_ThenShouldReturn404() {
    // Given
    var accountId = "account-404";
    var result = Result.<AccountResult>failure(Code.NOT_FOUND, "Account not found");
    when(usecase.process(any(String.class))).thenReturn(result);

    // When
    var response = Unirest.delete(baseUrl + "/accounts/" + accountId).asString();

    // Then
    assertEquals(404, response.getStatus());
    assertEquals("Account not found", response.getBody());
  }
}
