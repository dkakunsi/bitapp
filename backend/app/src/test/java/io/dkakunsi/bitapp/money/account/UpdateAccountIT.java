package io.dkakunsi.bitapp.money.account;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.dkakunsi.bitapp.jwt.JWTAuthorizer;
import io.dkakunsi.bitapp.money.AppLauncher;
import io.dkakunsi.bitapp.test.AppTestUtil;
import io.dkakunsi.bitapp.test.SecureTestUtil;
import kong.unirest.Unirest;

public class UpdateAccountIT extends AppTestUtil {

    private static final int port = 20005;

    private static UpdateAccountIT sut = new UpdateAccountIT();

    private static String baseUrl;

    private static String token;

    @BeforeAll
    static void setup() throws Exception {
        var appEnv = Map.of(APP_PORT, Integer.toString(port),
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

    /**
     * <b>Given</b> an existing account and a valid update request with all fields
     * (name, type, themeColor)<br>
     * <b>When</b> the PUT /accounts/{id} endpoint is called with valid
     * authorization<br>
     * <b>Then</b> all account fields should be updated successfully with status 200
     */
    @Test
    void givenValidUpdateRequestWhenAllFieldsProvidedThenShouldUpdateSuccessfully() {
        // Given - Create an account first
        var createRequest = new JSONObject()
                .put("name", "Original Account")
                .put("type", "BANK")
                .put("themeColor", "#FF0000");

        var createResponse = Unirest.post(baseUrl + "/accounts")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .body(createRequest.toString())
                .asJson();

        assertEquals(200, createResponse.getStatus());
        var createdAccount = createResponse.getBody().getObject();
        var accountId = createdAccount.getString("id");

        // When - Update all fields
        var updateRequest = new JSONObject()
                .put("name", "Updated Account")
                .put("type", "CASH")
                .put("themeColor", "#00FF00");

        var updateResponse = Unirest.put(baseUrl + "/accounts/" + accountId)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .body(updateRequest.toString())
                .asJson();

        // Then
        assertEquals(200, updateResponse.getStatus());
        var updated = updateResponse.getBody().getObject();
        assertEquals(accountId, updated.getString("id"));
        assertEquals("Updated Account", updated.getString("name"));
        assertEquals("CASH", updated.getString("type"));
        assertEquals("#00FF00", updated.getString("themeColor"));
    }

    /**
     * <b>Given</b> an existing account and an update request with only the name
     * field<br>
     * <b>When</b> the PUT /accounts/{id} endpoint is called<br>
     * <b>Then</b> only the name should be updated while other fields remain
     * unchanged
     */
    @Test
    void givenUpdateRequestWithOnlyNameWhenProcessedThenShouldUpdateOnlyName() {
        // Given - Create an account first
        var createRequest = new JSONObject()
                .put("name", "Test Account")
                .put("type", "EWALLET")
                .put("themeColor", "#0000FF");

        var createResponse = Unirest.post(baseUrl + "/accounts")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .body(createRequest.toString())
                .asJson();

        var accountId = createResponse.getBody().getObject().getString("id");

        // When - Update only name
        var updateRequest = new JSONObject()
                .put("name", "Name Changed");

        var updateResponse = Unirest.put(baseUrl + "/accounts/" + accountId)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .body(updateRequest.toString())
                .asJson();

        // Then
        assertEquals(200, updateResponse.getStatus());
        var updated = updateResponse.getBody().getObject();
        assertEquals("Name Changed", updated.getString("name"));
        assertEquals("EWALLET", updated.getString("type"));
        assertEquals("#0000FF", updated.getString("themeColor"));
    }

    /**
     * <b>Given</b> an update request with empty name<br>
     * <b>When</b> the PUT /accounts/{id} endpoint is called<br>
     * <b>Then</b> should return status 400 with validation error message
     */
    @Test
    void givenUpdateRequestWithEmptyNameWhenProcessedThenShouldReturn400() {
        // Given - Create an account first
        var createRequest = new JSONObject()
                .put("name", "Valid Account")
                .put("type", "BANK")
                .put("themeColor", "#FF0000");

        var createResponse = Unirest.post(baseUrl + "/accounts")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .body(createRequest.toString())
                .asJson();

        assertEquals(200, createResponse.getStatus());
        var accountId = createResponse.getBody().getObject().getString("id");

        // When - Update with empty name
        var updateRequest = new JSONObject()
                .put("name", "");

        var response = Unirest.put(baseUrl + "/accounts/" + accountId)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .body(updateRequest.toString())
                .asString();

        // Then
        assertEquals(400, response.getStatus());
        assertEquals("name: must not be blank", response.getBody());
    }

    /**
     * <b>Given</b> an update request with blank name (only whitespace)<br>
     * <b>When</b> the PUT /accounts/{id} endpoint is called<br>
     * <b>Then</b> should return status 400 with validation error message
     */
    @Test
    void givenUpdateRequestWithBlankNameWhenProcessedThenShouldReturn400() {
        // Given - Create an account first
        var createRequest = new JSONObject()
                .put("name", "Valid Account")
                .put("type", "BANK")
                .put("themeColor", "#FF0000");

        var createResponse = Unirest.post(baseUrl + "/accounts")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .body(createRequest.toString())
                .asJson();

        assertEquals(200, createResponse.getStatus());
        var accountId = createResponse.getBody().getObject().getString("id");

        // When - Update with blank name
        var updateRequest = new JSONObject()
                .put("name", "   ");

        var response = Unirest.put(baseUrl + "/accounts/" + accountId)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .body(updateRequest.toString())
                .asString();

        // Then
        assertEquals(400, response.getStatus());
        assertEquals("name: must not be blank", response.getBody());
    }

    /**
     * <b>Given</b> an update request with null name<br>
     * <b>When</b> the PUT /accounts/{id} endpoint is called<br>
     * <b>Then</b> should return status 400 with validation error message
     */
    @Test
    void givenUpdateRequestWithNullNameWhenProcessedThenShouldReturn400() {
        // Given - Create an account first
        var createRequest = new JSONObject()
                .put("name", "Valid Account")
                .put("type", "BANK")
                .put("themeColor", "#FF0000");

        var createResponse = Unirest.post(baseUrl + "/accounts")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .body(createRequest.toString())
                .asJson();

        assertEquals(200, createResponse.getStatus());
        var accountId = createResponse.getBody().getObject().getString("id");

        // When - Update with null name
        var updateRequest = new JSONObject()
                .put("name", JSONObject.NULL);

        var response = Unirest.put(baseUrl + "/accounts/" + accountId)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .body(updateRequest.toString())
                .asString();

        // Then
        assertEquals(400, response.getStatus());
        assertEquals("name: must not be blank", response.getBody());
    }

    /**
     * <b>Given</b> an existing account and an update request with only the type and
     * name fields<br>
     * <b>When</b> the PUT /accounts/{id} endpoint is called<br>
     * <b>Then</b> only the type should be updated while other fields remain
     * unchanged
     */
    @Test
    void givenUpdateRequestWithOnlyTypeAndNameWhenProcessedThenShouldUpdateOnlyType() {
        // Given
        var createRequest = new JSONObject()
                .put("name", "Type Test Account")
                .put("type", "BANK")
                .put("themeColor", "#AABBCC");

        var createResponse = Unirest.post(baseUrl + "/accounts")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .body(createRequest.toString())
                .asJson();

        var accountId = createResponse.getBody().getObject().getString("id");

        // When - Update only type
        var updateRequest = new JSONObject()
                .put("name", "Type Test Account")
                .put("type", "OTHER");

        var updateResponse = Unirest.put(baseUrl + "/accounts/" + accountId)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .body(updateRequest.toString())
                .asJson();

        // Then
        assertEquals(200, updateResponse.getStatus());
        var updated = updateResponse.getBody().getObject();
        assertEquals("Type Test Account", updated.getString("name"));
        assertEquals("OTHER", updated.getString("type"));
        assertEquals("#AABBCC", updated.getString("themeColor"));
    }

    /**
     * <b>Given</b> an existing account and an update request with only the
     * themeColor and name fields<br>
     * <b>When</b> the PUT /accounts/{id} endpoint is called<br>
     * <b>Then</b> only the theme color should be updated while other fields remain
     * unchanged
     */
    @Test
    void givenUpdateRequestWithOnlyThemeColorAndNameWhenProcessedThenShouldUpdateOnlyThemeColor() {
        // Given
        var createRequest = new JSONObject()
                .put("name", "Color Test Account")
                .put("type", "CASH")
                .put("themeColor", "#111111");

        var createResponse = Unirest.post(baseUrl + "/accounts")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .body(createRequest.toString())
                .asJson();

        var accountId = createResponse.getBody().getObject().getString("id");

        // When - Update only theme color
        var updateRequest = new JSONObject()
                .put("name", "Color Test Account")
                .put("themeColor", "#FFFFFF");

        var updateResponse = Unirest.put(baseUrl + "/accounts/" + accountId)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .body(updateRequest.toString())
                .asJson();

        // Then
        assertEquals(200, updateResponse.getStatus());
        var updated = updateResponse.getBody().getObject();
        assertEquals("Color Test Account", updated.getString("name"));
        assertEquals("CASH", updated.getString("type"));
        assertEquals("#FFFFFF", updated.getString("themeColor"));
    }

    /**
     * <b>Given</b> an update request with an invalid account type<br>
     * <b>When</b> the PUT /accounts/{id} endpoint is called<br>
     * <b>Then</b> the request should be rejected with status 400 (Bad Request)
     */
    @Test
    void givenUpdateRequestWithInvalidAccountTypeWhenProcessedThenShouldReturn400() {
        // Given - Create an account first
        var createRequest = new JSONObject()
                .put("name", "Test Account")
                .put("type", "BANK")
                .put("themeColor", "#FF0000");

        var createResponse = Unirest.post(baseUrl + "/accounts")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .body(createRequest.toString())
                .asJson();

        assertEquals(200, createResponse.getStatus());
        var accountId = createResponse.getBody().getObject().getString("id");

        // When - Try to update with invalid account type
        var updateRequest = new JSONObject()
                .put("type", "INVALID_TYPE");

        var response = Unirest.put(baseUrl + "/accounts/" + accountId)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .body(updateRequest.toString())
                .asString();

        // Then
        assertEquals(400, response.getStatus());
        assertEquals("Invalid account type: INVALID_TYPE", response.getBody());
    }

    /**
     * <b>Given</b> an update request with empty request body<br>
     * <b>When</b> the PUT /accounts/{id} endpoint is called<br>
     * <b>Then</b> the request should be rejected with status 400
     */
    @Test
    void givenUpdateRequestWithEmptyBodyWhenProcessedThenShouldReturn400() {
        // Given
        var createRequest = new JSONObject()
                .put("name", "Test Account")
                .put("type", "BANK")
                .put("themeColor", "#FF0000");

        var createResponse = Unirest.post(baseUrl + "/accounts")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .body(createRequest.toString())
                .asJson();

        var accountId = createResponse.getBody().getObject().getString("id");

        // When
        var response = Unirest.put(baseUrl + "/accounts/" + accountId)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .body(new JSONObject().toString())
                .asString();

        // Then
        assertEquals(400, response.getStatus());
        assertEquals("name: must not be blank", response.getBody());
    }

    /**
     * <b>Given</b> an update request with malformed JSON body<br>
     * <b>When</b> the PUT /accounts/{id} endpoint is called<br>
     * <b>Then</b> the request should be rejected with status 400
     */
    @Test
    void givenUpdateRequestWithMalformedJsonWhenProcessedThenShouldReturn400() {
        // Given
        var createRequest = new JSONObject()
                .put("name", "Test Account")
                .put("type", "BANK")
                .put("themeColor", "#FF0000");

        var createResponse = Unirest.post(baseUrl + "/accounts")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .body(createRequest.toString())
                .asJson();

        var accountId = createResponse.getBody().getObject().getString("id");

        // When
        var response = Unirest.put(baseUrl + "/accounts/" + accountId)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .body("{invalid json")
                .asString();

        // Then
        assertEquals(400, response.getStatus());
        assertEquals("Invalid request body", response.getBody());
    }

    /**
     * <b>Given</b> an update request without an Authorization header<br>
     * <b>When</b> the PUT /accounts/{id} endpoint is called<br>
     * <b>Then</b> the request should be rejected with status 401 (Unauthorized)
     */
    @Test
    void givenUpdateRequestWithoutAuthorizationWhenProcessedThenShouldReturn401() {
        // When
        var updateRequest = new JSONObject()
                .put("name", "Unauthorized Update");

        var response = Unirest.put(baseUrl + "/accounts/some-id")
                .header("Content-Type", "application/json")
                .body(updateRequest.toString())
                .asString();

        // Then
        assertEquals(401, response.getStatus());
    }

    /**
     * <b>Given</b> an update request with an invalid or malformed JWT token<br>
     * <b>When</b> the PUT /accounts/{id} endpoint is called<br>
     * <b>Then</b> the request should be rejected with status 401 (Unauthorized)
     */
    @Test
    void givenUpdateRequestWithInvalidTokenWhenProcessedThenShouldReturn401() {
        // When
        var updateRequest = new JSONObject()
                .put("name", "Invalid Token Update");

        var response = Unirest.put(baseUrl + "/accounts/some-id")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer invalid.token.here")
                .body(updateRequest.toString())
                .asString();

        // Then
        assertEquals(401, response.getStatus());
    }

    /**
     * <b>Given</b> an update request for an account ID that does not exist in the
     * system<br>
     * <b>When</b> the PUT /accounts/{id} endpoint is called<br>
     * <b>Then</b> the request should fail with status 404 (Not Found)
     */
    @Test
    void givenUpdateRequestForNonExistentAccountWhenProcessedThenShouldReturn404() {
        // Given

        var updateRequest = new JSONObject()
                .put("name", "Non-existent Account");

        // When
        var response = Unirest.put(baseUrl + "/accounts/nonexistent-id")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .body(updateRequest.toString())
                .asString();

        // Then - Should return 404 as the account doesn't exist
        assertEquals(404, response.getStatus());
    }

    /**
     * <b>Given</b> user2 attempts to update an account that belongs to user1<br>
     * <b>When</b> the PUT /accounts/{id} endpoint is called with user2's token<br>
     * <b>Then</b> the request should be rejected with status 400 as users can only
     * update their own accounts
     */
    @Test
    void givenUpdateRequestForOtherUsersAccountWhenProcessedThenShouldReturn400() {
        // Given - User1 creates an account
        var createRequest = new JSONObject()
                .put("name", "User1 Account")
                .put("type", "BANK")
                .put("themeColor", "#FF0000");

        var createResponse = Unirest.post(baseUrl + "/accounts")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .body(createRequest.toString())
                .asJson();

        assertEquals(200, createResponse.getStatus());
        var accountId = createResponse.getBody().getObject().getString("id");

        // When - User2 tries to update User1's account
        var user2Id = "user2@email.com";
        var user2Token = SecureTestUtil.generateToken(user2Id);

        var updateRequest = new JSONObject()
                .put("name", "Unauthorized Update");

        var response = Unirest.put(baseUrl + "/accounts/" + accountId)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + user2Token)
                .body(updateRequest.toString())
                .asString();

        // Then - Should return 400 as user can only update their own account
        assertEquals(400, response.getStatus());
        assertEquals("User can only update their own account", response.getBody());
    }

    /**
     * <b>Given</b> an account owner attempts to update their own account<br>
     * <b>When</b> the PUT /accounts/{id} endpoint is called with the owner's
     * token<br>
     * <b>Then</b> the update should succeed with status 200 and reflect the changes
     */
    @Test
    void givenUpdateRequestByOwnerWhenProcessedThenShouldSucceed() {
        // Given - Create an account
        var createRequest = new JSONObject()
                .put("name", "Owner Account")
                .put("type", "BANK")
                .put("themeColor", "#123456");

        var createResponse = Unirest.post(baseUrl + "/accounts")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .body(createRequest.toString())
                .asJson();

        var accountId = createResponse.getBody().getObject().getString("id");

        // When - Same owner updates their own account
        var updateRequest = new JSONObject()
                .put("name", "Updated by Owner");

        var response = Unirest.put(baseUrl + "/accounts/" + accountId)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .body(updateRequest.toString())
                .asJson();

        // Then - Should succeed
        assertEquals(200, response.getStatus());
        var updated = response.getBody().getObject();
        assertEquals("Updated by Owner", updated.getString("name"));
    }

    /**
     * <b>Given</b> an existing account that will be updated multiple times
     * sequentially<br>
     * <b>When</b> the PUT /accounts/{id} endpoint is called three times with
     * different field updates<br>
     * <b>Then</b> all sequential updates should be applied correctly and persist in
     * the final state
     */
    @Test
    void givenMultipleSequentialUpdatesWhenProcessedThenShouldApplyAllChanges() {
        // Given - Create an account
        var createRequest = new JSONObject()
                .put("name", "Sequential Test")
                .put("type", "BANK")
                .put("themeColor", "#000000");

        var createResponse = Unirest.post(baseUrl + "/accounts")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .body(createRequest.toString())
                .asJson();

        var accountId = createResponse.getBody().getObject().getString("id");

        // When - First update: change name
        var update1 = new JSONObject().put("name", "Step 1");
        var response1 = Unirest.put(baseUrl + "/accounts/" + accountId)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .body(update1.toString())
                .asJson();

        assertEquals(200, response1.getStatus());
        assertEquals("Step 1", response1.getBody().getObject().getString("name"));

        // When - Second update: change type
        var update2 = new JSONObject()
                .put("name", "Step 1")
                .put("type", "CASH");
        var response2 = Unirest.put(baseUrl + "/accounts/" + accountId)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .body(update2.toString())
                .asJson();

        assertEquals(200, response2.getStatus());
        assertEquals("CASH", response2.getBody().getObject().getString("type"));

        // When - Third update: change theme color
        var update3 = new JSONObject()
                .put("name", "Step 1")
                .put("themeColor", "#ABCDEF");
        var response3 = Unirest.put(baseUrl + "/accounts/" + accountId)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .body(update3.toString())
                .asJson();

        // Then - Verify final state has all updates
        assertEquals(200, response3.getStatus());
        var finalState = response3.getBody().getObject();
        assertEquals("Step 1", finalState.getString("name"));
        assertEquals("CASH", finalState.getString("type"));
        assertEquals("#ABCDEF", finalState.getString("themeColor"));
    }
}
