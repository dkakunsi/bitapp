package io.dkakunsi.bitapp.test;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Map;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

public class SecureTestUtil {

  public static final String PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtS8hW+pA+8vuWFFL259x8tbdy14MvJTph93XLr6M8J/gAuWPO7+ay6VwG6EjRY1Qox2SzZ4f+iJKFovUb2nqgB+TfezZVdgffu48RI7GWpqEg1meC5++N+l5QgQ4NHhkfEuH9Yiw9nzRFiS4N0z14Ih5fDlByCzA6SX7LkxjurkOm6W/AIjdulCZh5//M9pJGeLWczQ0Gx8+3AITPNpuk7pddSD1Kp6qKvN6W9J8pfrKF8VGVkuJmTstRrnR0KCC7pOpUfeIQWoin8CaMama2QtISeNhkGNaGF6bLeBxrg1VMzda3Yzp9EnGQMElJYQ4p6+ArJMaREzpERXQgTQdGwIDAQAB";

  private static final String PRIVATE_KEY = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQC1LyFb6kD7y+5YUUvbn3Hy1t3LXgy8lOmH3dcuvozwn+AC5Y87v5rLpXAboSNFjVCjHZLNnh/6IkoWi9RvaeqAH5N97NlV2B9+7jxEjsZamoSDWZ4Ln7436XlCBDg0eGR8S4f1iLD2fNEWJLg3TPXgiHl8OUHILMDpJfsuTGO6uQ6bpb8AiN26UJmHn/8z2kkZ4tZzNDQbHz7cAhM82m6Tul11IPUqnqoq83pb0nyl+soXxUZWS4mZOy1GudHQoILuk6lR94hBaiKfwJoxqZrZC0hJ42GQY1oYXpst4HGuDVUzN1rdjOn0ScZAwSUlhDinr4CskxpETOkRFdCBNB0bAgMBAAECggEAPfw6yGxTARHan+JoNmHNJQw3YvzxFI9Jec7+cCKGq0e25qbMot9BQQx/VySAoanf/X3/nFLNk9CpUh9SdS9iJ6Ul233tOL4wwEcW3UmPOK4GSb2eIVHsTMqWTmyNIf5SOmfIwsqZ6Cn5ij7Tuy+cKs3l6gbYp3gQI3N4BHXj5JwZoFAZVZgMfYlMpzbrOFSUrSqAR80gTOmTbgo4ZTw3QU5lToMzQ/KJPMJsjxBkTal3dZ6HU9rGBTzNVpGOD0BvTEKTSof+jIofbj59IoXW04O5rWZoE9gBF7DdvYc6+ie8/djJAYkJBPNEmC1s3Utp7/5nmckHVpvkKV1tlup70QKBgQDeYIC6+EBZ9uhC1Iqec8Hgmtg2eL1ldPlpd7E8IB5gNBFuxJsAQs2bQkZOBe1ZU7h3JlkAH+isCfNuOtH16G54CBSqONjvP2bsGWJqYzpG/psZvjsFoPCmvB1rlDPXsLxDP7ps1tjkqrYlY45A8rblEPPJpkg2F0XRbFYMGjD1QwKBgQDQlC8rllziGjs/UkDi7x3MHrrSAg1ldKzy3aJ2LkbhYZ3IE0sfeDtp3yV6yDkFVl9EU8PiWO99Rc7tYEAuMqJjW6rtF0cqGZsJZbPLKGKXt4/On2MDUYPin1XHYomlSe8OtpOEGVYxlsIXxqvOXuUJ7W/HgoMCO1rA8dvPXA/PSQKBgQDSqDJqa+9yCf7eCD/EeL4JykXV3Cz2pof6zCL+ZSLBWbHF78MxzRa+5Fp7YQwF2dReMtqOzqt4Bfkvy9LIE8ZKOMVyt2Vxxur179oWFCfJxzkget+oplwyZvOrzHoL8mV1gzJUFnbir4DbDGNezU5K0vNObBHuA7/k8q7Uyh7kxwKBgFfieFWnT4+9ecVehRSZqDZ/pDwkvTxIgy76ECA3s4n3taG972NdJ7ueWI55mv0SvaVunhTbYF2qclw2uBQ/JYkz8LthmYy1qUu2XKF3bMN8hs2K/w9A448zj9MpQ9IvatkKOPHqMxVF7pZSEcYs2djrALRR252vILg3sGSY59hxAoGBANVNJ5P+64vpCc6lxLszuVfBtUvBgvFtZQoGMlKCXEahVlmztqZyQWhZFZiI3kxSZqNZ8f3fk3Bu1m9yJOWpDA/QJieGHYehnom5rTn7rCEojU7bu74Zpk+fOPylD2/41GGpCcw0cBvNqmxUf400vfLtVbd2qSANy3+qXpsvosAW";

  private static final String RSA = "RSA";

  public static String createToken(Map<String, Object> payload)
      throws NoSuchAlgorithmException, InvalidKeySpecException {
    var privateKey = privateKey();
    var algorithm = Algorithm.RSA256(null, privateKey);
    return JWT.create().withPayload(payload).sign(algorithm);
  }

  public static String generateToken(String userId) {
    try {
      var payload = Map.<String, Object>of(
          "sub", userId,
          "email", userId,
          "exp", System.currentTimeMillis() / 1000 + 3600 // expire 1 hour later
      );
      return createToken(payload);
    } catch (Exception e) {
      throw new RuntimeException("Failed to generate token", e);
    }
  }

  public static RSAPrivateKey privateKey()
      throws NoSuchAlgorithmException, InvalidKeySpecException {
    var decodedPrivateKey = Base64.getDecoder().decode(PRIVATE_KEY.getBytes(StandardCharsets.UTF_8));
    var keySpec = new PKCS8EncodedKeySpec(decodedPrivateKey);
    var keyFactory = KeyFactory.getInstance(RSA);
    return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
  }

  public static void main(String[] args) {
    var userId = AppTestUtil.USER_ID;
    var token = generateToken(userId);
    System.out.println(token);
  }
}
