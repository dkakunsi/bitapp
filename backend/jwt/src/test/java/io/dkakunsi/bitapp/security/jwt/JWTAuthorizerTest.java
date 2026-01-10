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
import io.dkakunsi.lab.test.SecureTest;

class JWTAuthorizerTest {

  private static final String PRIVATE_KEY = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQC1LyFb6kD7y+5YUUvbn3Hy1t3LXgy8lOmH3dcuvozwn+AC5Y87v5rLpXAboSNFjVCjHZLNnh/6IkoWi9RvaeqAH5N97NlV2B9+7jxEjsZamoSDWZ4Ln7436XlCBDg0eGR8S4f1iLD2fNEWJLg3TPXgiHl8OUHILMDpJfsuTGO6uQ6bpb8AiN26UJmHn/8z2kkZ4tZzNDQbHz7cAhM82m6Tul11IPUqnqoq83pb0nyl+soXxUZWS4mZOy1GudHQoILuk6lR94hBaiKfwJoxqZrZC0hJ42GQY1oYXpst4HGuDVUzN1rdjOn0ScZAwSUlhDinr4CskxpETOkRFdCBNB0bAgMBAAECggEAPfw6yGxTARHan+JoNmHNJQw3YvzxFI9Jec7+cCKGq0e25qbMot9BQQx/VySAoanf/X3/nFLNk9CpUh9SdS9iJ6Ul233tOL4wwEcW3UmPOK4GSb2eIVHsTMqWTmyNIf5SOmfIwsqZ6Cn5ij7Tuy+cKs3l6gbYp3gQI3N4BHXj5JwZoFAZVZgMfYlMpzbrOFSUrSqAR80gTOmTbgo4ZTw3QU5lToMzQ/KJPMJsjxBkTal3dZ6HU9rGBTzNVpGOD0BvTEKTSof+jIofbj59IoXW04O5rWZoE9gBF7DdvYc6+ie8/djJAYkJBPNEmC1s3Utp7/5nmckHVpvkKV1tlup70QKBgQDeYIC6+EBZ9uhC1Iqec8Hgmtg2eL1ldPlpd7E8IB5gNBFuxJsAQs2bQkZOBe1ZU7h3JlkAH+isCfNuOtH16G54CBSqONjvP2bsGWJqYzpG/psZvjsFoPCmvB1rlDPXsLxDP7ps1tjkqrYlY45A8rblEPPJpkg2F0XRbFYMGjD1QwKBgQDQlC8rllziGjs/UkDi7x3MHrrSAg1ldKzy3aJ2LkbhYZ3IE0sfeDtp3yV6yDkFVl9EU8PiWO99Rc7tYEAuMqJjW6rtF0cqGZsJZbPLKGKXt4/On2MDUYPin1XHYomlSe8OtpOEGVYxlsIXxqvOXuUJ7W/HgoMCO1rA8dvPXA/PSQKBgQDSqDJqa+9yCf7eCD/EeL4JykXV3Cz2pof6zCL+ZSLBWbHF78MxzRa+5Fp7YQwF2dReMtqOzqt4Bfkvy9LIE8ZKOMVyt2Vxxur179oWFCfJxzkget+oplwyZvOrzHoL8mV1gzJUFnbir4DbDGNezU5K0vNObBHuA7/k8q7Uyh7kxwKBgFfieFWnT4+9ecVehRSZqDZ/pDwkvTxIgy76ECA3s4n3taG972NdJ7ueWI55mv0SvaVunhTbYF2qclw2uBQ/JYkz8LthmYy1qUu2XKF3bMN8hs2K/w9A448zj9MpQ9IvatkKOPHqMxVF7pZSEcYs2djrALRR252vILg3sGSY59hxAoGBANVNJ5P+64vpCc6lxLszuVfBtUvBgvFtZQoGMlKCXEahVlmztqZyQWhZFZiI3kxSZqNZ8f3fk3Bu1m9yJOWpDA/QJieGHYehnom5rTn7rCEojU7bu74Zpk+fOPylD2/41GGpCcw0cBvNqmxUf400vfLtVbd2qSANy3+qXpsvosAW";

  private static final String PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtS8hW+pA+8vuWFFL259x8tbdy14MvJTph93XLr6M8J/gAuWPO7+ay6VwG6EjRY1Qox2SzZ4f+iJKFovUb2nqgB+TfezZVdgffu48RI7GWpqEg1meC5++N+l5QgQ4NHhkfEuH9Yiw9nzRFiS4N0z14Ih5fDlByCzA6SX7LkxjurkOm6W/AIjdulCZh5//M9pJGeLWczQ0Gx8+3AITPNpuk7pddSD1Kp6qKvN6W9J8pfrKF8VGVkuJmTstRrnR0KCC7pOpUfeIQWoin8CaMama2QtISeNhkGNaGF6bLeBxrg1VMzda3Yzp9EnGQMElJYQ4p6+ArJMaREzpERXQgTQdGwIDAQAB";

  private static final String JWT_PUBLIC_KEY = "JWT_PUBLIC_KEY";

  private JWTAuthorizer authorizer;

  private Configuration configuration;

  @BeforeEach
  void setup() {
    configuration = mock(Configuration.class);
    when(configuration.get(JWT_PUBLIC_KEY)).thenReturn(Optional.of(PUBLIC_KEY));
    authorizer = JWTAuthorizer.of(configuration);
  }

  @Test
  void testBuildAuthorizerWithInvalidKey() {
    // Given
    when(configuration.get(JWT_PUBLIC_KEY)).thenReturn(Optional.of("asd"));

    // When
    var ex = assertThrows(RuntimeException.class, () -> JWTAuthorizer.of(configuration));

    // Then
    assertEquals("Cannot create RSA key for authentication", ex.getMessage());
    assertInstanceOf(InvalidKeySpecException.class, ex.getCause());
  }

  @Test
  void testBuildAuthorizerWhenKeyNotProvided() {
    // Given
    when(configuration.get(JWT_PUBLIC_KEY)).thenReturn(Optional.empty());

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
    var token = SecureTest.createToken(PRIVATE_KEY, payload);

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
    var token = SecureTest.createToken(PRIVATE_KEY, payload);

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
    var token = "Bearer " + SecureTest.createToken(PRIVATE_KEY, payload);

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
