package io.dkakunsi.lab.test;

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

public class SecureTest {

  private static final String RSA = "RSA";

  public static String createToken(String privateKeyString, Map<String, Object> payload)
      throws NoSuchAlgorithmException, InvalidKeySpecException {
    var privateKey = privateKey(privateKeyString);
    var algorithm = Algorithm.RSA256(null, privateKey);
    return JWT.create().withPayload(payload).sign(algorithm);
  }

  public static RSAPrivateKey privateKey(String privateKeyString)
      throws NoSuchAlgorithmException, InvalidKeySpecException {
    var decodedPrivateKey = Base64.getDecoder().decode(privateKeyString.getBytes(StandardCharsets.UTF_8));
    var keySpec = new PKCS8EncodedKeySpec(decodedPrivateKey);
    var keyFactory = KeyFactory.getInstance(RSA);
    return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
  }
}
