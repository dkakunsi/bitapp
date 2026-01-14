package io.dkakunsi.bitapp.security.jwt;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.auth0.jwt.exceptions.TokenExpiredException;

import io.dkakunsi.bitapp.common.Configuration;
import io.dkakunsi.bitapp.jwt.JWTAuthorizer;
import io.dkakunsi.bitapp.test.SecureTestUtil;

class JWTAuthorizerTest {

  private JWTAuthorizer authorizer;

  private Configuration configuration;

  @BeforeEach
  void setup() {
    configuration = mock(Configuration.class);
    when(configuration.get(JWTAuthorizer.JWT_PUBLIC_KEY)).thenReturn(Optional.of(SecureTestUtil.PUBLIC_KEY));
    authorizer = JWTAuthorizer.of(configuration);
  }

  @Test
  void testBuildAuthorizerWithInvalidKey() {
    // Given
    when(configuration.get(JWTAuthorizer.JWT_PUBLIC_KEY)).thenReturn(Optional.of("asd"));

    // When
    var ex = assertThrows(RuntimeException.class, () -> JWTAuthorizer.of(configuration));

    // Then
    assertEquals("Cannot create RSA key for authentication", ex.getMessage());
    assertInstanceOf(InvalidKeySpecException.class, ex.getCause());
  }

  @Test
  void testBuildAuthorizerWhenKeyNotProvided() {
    // Given
    when(configuration.get(JWTAuthorizer.JWT_PUBLIC_KEY)).thenReturn(Optional.empty());

    // When
    var ex = assertThrows(RuntimeException.class, () -> JWTAuthorizer.of(configuration));

    // Then
    assertEquals("JWT_PUBLIC_KEY is not configured correctly", ex.getMessage());
    assertInstanceOf(RuntimeException.class, ex);
  }

  @Test
  void testVerify() throws NoSuchAlgorithmException, InvalidKeySpecException {
    // Given
    var payload = new HashMap<String, Object>();
    payload.put("name", "Deddy Kakunsi");
    payload.put("email", "deddy@kakunsi.com");
    payload.put("sub", "0123456789");
    payload.put("exp", System.currentTimeMillis() / 1000 + 3600); // expire 1 hour later
    var token = SecureTestUtil.createToken(payload);

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
    payload.put("exp", System.currentTimeMillis() / 1000 - 3600); // expire 1 hour ago
    var token = SecureTestUtil.createToken(payload);

    // When
    var ex = assertThrows(IllegalArgumentException.class, () -> authorizer.verify(token));

    // Then
    assertEquals("Token is not valid", ex.getMessage());
    assertInstanceOf(TokenExpiredException.class, ex.getCause());
  }

  @Test
  void testVerifyWithNullToken() {
    // When
    var ex = assertThrows(IllegalArgumentException.class, () -> authorizer.verify(null));

    // Then
    assertEquals("Token is not valid", ex.getMessage());
    assertNull(ex.getCause());
  }

  @Test
  void testVerifyWithBlankToken() {
    // When
    var ex = assertThrows(IllegalArgumentException.class, () -> authorizer.verify(""));

    // Then
    assertEquals("Token is not valid", ex.getMessage());
    assertNull(ex.getCause());
  }

  @Test
  void testVerifyWithWhitespaceToken() {
    // When
    var ex = assertThrows(IllegalArgumentException.class, () -> authorizer.verify("   "));

    // Then
    assertEquals("Token is not valid", ex.getMessage());
    assertNull(ex.getCause());
  }

  @Test
  void testVerifyWithBearerPrefix() throws NoSuchAlgorithmException, InvalidKeySpecException {
    // Given
    var payload = new HashMap<String, Object>();
    payload.put("name", "Deddy Kakunsi");
    payload.put("email", "deddy@kakunsi.com");
    payload.put("sub", "0123456789");
    payload.put("exp", System.currentTimeMillis() / 1000 + 3600); // expire 1 hour later
    var token = "Bearer " + SecureTestUtil.createToken(payload);

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
}
