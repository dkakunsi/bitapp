package io.dkakunsi.bitapp.jwt;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;

import io.dkakunsi.bitapp.common.Configuration;
import io.dkakunsi.bitapp.test.SecureTestUtil;

class JWKAuthorizerTest {

  private static final String TEST_KID = "google-test-key-id";

  private JWKAuthorizer authorizer;
  private JwkProvider jwkProvider;

  @BeforeEach
  void setup() throws JwkException, NoSuchAlgorithmException, InvalidKeySpecException {
    jwkProvider = mock(JwkProvider.class);
    var jwk = mock(Jwk.class);
    when(jwk.getPublicKey()).thenReturn(toPublicKey(SecureTestUtil.PUBLIC_KEY));
    when(jwkProvider.get(TEST_KID)).thenReturn(jwk);

    authorizer = new JWKAuthorizer(jwkProvider);
  }

  @Test
  void testBuildAuthorizerWithInvalidJwksUrl() {
    // Given
    var configuration = mock(Configuration.class);
    when(configuration.get(JWKAuthorizer.JWK_URL)).thenReturn(Optional.of("invalid-url"));

    // When
    var ex = assertThrows(IllegalArgumentException.class, () -> JWKAuthorizer.of(configuration));

    // Then
    assertEquals("JWK_URL is not configured correctly", ex.getMessage());
  }

  @Test
  void testBuildAuthorizerWithDefaultJwksUrlWhenNotProvided() {
    // Given
    var configuration = mock(Configuration.class);
    when(configuration.get(JWKAuthorizer.JWK_URL)).thenReturn(Optional.empty());

    // When
    var builtAuthorizer = JWKAuthorizer.of(configuration);

    // Then
    assertInstanceOf(JWKAuthorizer.class, builtAuthorizer);
  }

  @Test
  void testVerify() throws NoSuchAlgorithmException, InvalidKeySpecException {
    // Given
    var payload = new HashMap<String, Object>();
    payload.put("name", "Deddy Kakunsi");
    payload.put("email", "deddy@kakunsi.com");
    payload.put("sub", "0123456789");
    payload.put("exp", System.currentTimeMillis() / 1000 + 3600);

    var privateKey = SecureTestUtil.privateKey();
    var token = JWT.create()
        .withKeyId(TEST_KID)
        .withPayload(payload)
        .sign(Algorithm.RSA256(null, privateKey));

    // When
    var user = authorizer.verify(token);

    // Then
    assertEquals("deddy@kakunsi.com", user.email());
  }

  @Test
  void testVerifyExpireToken() throws NoSuchAlgorithmException, InvalidKeySpecException {
    // Given
    var payload = new HashMap<String, Object>();
    payload.put("name", "Deddy Kakunsi");
    payload.put("email", "deddy@kakunsi.com");
    payload.put("sub", "0123456789");
    payload.put("exp", System.currentTimeMillis() / 1000 - 3600);

    var privateKey = SecureTestUtil.privateKey();
    var token = JWT.create()
        .withKeyId(TEST_KID)
        .withPayload(payload)
        .sign(Algorithm.RSA256(null, privateKey));

    // When
    var ex = assertThrows(IllegalArgumentException.class, () -> authorizer.verify(token));

    // Then
    assertEquals("Token is not valid", ex.getMessage());
    assertInstanceOf(TokenExpiredException.class, ex.getCause());
  }

  @Test
  void testVerifyWithMissingKid() throws NoSuchAlgorithmException, InvalidKeySpecException {
    // Given
    var payload = new HashMap<String, Object>();
    payload.put("name", "Deddy Kakunsi");
    payload.put("email", "deddy@kakunsi.com");
    payload.put("sub", "0123456789");
    payload.put("exp", System.currentTimeMillis() / 1000 + 3600);

    var privateKey = SecureTestUtil.privateKey();
    var token = JWT.create()
        .withPayload(payload)
        .sign(Algorithm.RSA256(null, privateKey));

    // When
    var ex = assertThrows(IllegalArgumentException.class, () -> authorizer.verify(token));

    // Then
    assertEquals("Token doesn't have a valid key ID", ex.getMessage());
    assertNull(ex.getCause());
  }

  @Test
  void testVerifyWithJwkProviderFailure() throws JwkException, NoSuchAlgorithmException, InvalidKeySpecException {
    // Given
    when(jwkProvider.get(anyString())).thenThrow(new JwkException("google key fetch failed"));
    var payload = new HashMap<String, Object>();
    payload.put("name", "Deddy Kakunsi");
    payload.put("email", "deddy@kakunsi.com");
    payload.put("sub", "0123456789");
    payload.put("exp", System.currentTimeMillis() / 1000 + 3600);

    var privateKey = SecureTestUtil.privateKey();
    var token = JWT.create()
        .withKeyId(TEST_KID)
        .withPayload(payload)
        .sign(Algorithm.RSA256(null, privateKey));

    // When
    var ex = assertThrows(RuntimeException.class, () -> authorizer.verify(token));

    // Then
    assertEquals("Cannot retrieve public key for authentication", ex.getMessage());
    assertInstanceOf(JwkException.class, ex.getCause());
  }

  @Test
  void testVerifyWithNullToken() {
    // When
    var ex = assertThrows(IllegalArgumentException.class, () -> authorizer.verify(null));

    // Then
    assertEquals("Token is empty", ex.getMessage());
    assertNull(ex.getCause());
  }

  @Test
  void testVerifyWithBlankToken() {
    // When
    var ex = assertThrows(IllegalArgumentException.class, () -> authorizer.verify(""));

    // Then
    assertEquals("Token is empty", ex.getMessage());
    assertNull(ex.getCause());
  }

  @Test
  void testVerifyWithWhitespaceToken() {
    // When
    var ex = assertThrows(IllegalArgumentException.class, () -> authorizer.verify("   "));

    // Then
    assertEquals("Token is empty", ex.getMessage());
    assertNull(ex.getCause());
  }

  @Test
  void testVerifyWithBearerPrefix() throws NoSuchAlgorithmException, InvalidKeySpecException {
    // Given
    var payload = new HashMap<String, Object>();
    payload.put("name", "Deddy Kakunsi");
    payload.put("email", "deddy@kakunsi.com");
    payload.put("sub", "0123456789");
    payload.put("exp", System.currentTimeMillis() / 1000 + 3600);

    var privateKey = SecureTestUtil.privateKey();
    var token = "Bearer " + JWT.create()
        .withKeyId(TEST_KID)
        .withPayload(payload)
        .sign(Algorithm.RSA256(null, privateKey));

    // When
    var user = authorizer.verify(token);

    // Then
    assertEquals("deddy@kakunsi.com", user.email());
  }

  @Test
  void testVerifyWithInvalidTokenFormat() {
    // When
    var ex = assertThrows(IllegalArgumentException.class, () -> authorizer.verify("invalid.token.format"));

    // Then
    assertEquals("Token is not valid", ex.getMessage());
  }

  @Test
  void testVerifyWithMalformedToken() {
    // When
    var ex = assertThrows(IllegalArgumentException.class, () -> authorizer.verify("not-a-jwt-token"));

    // Then
    assertEquals("Token is not valid", ex.getMessage());
  }

  private RSAPublicKey toPublicKey(String base64EncodedKey)
      throws NoSuchAlgorithmException, InvalidKeySpecException {
    var decodedPublicKey = Base64.getDecoder().decode(base64EncodedKey.getBytes(StandardCharsets.UTF_8));
    var keySpec = new X509EncodedKeySpec(decodedPublicKey);
    var keyFactory = KeyFactory.getInstance("RSA");
    return (RSAPublicKey) keyFactory.generatePublic(keySpec);
  }
}
