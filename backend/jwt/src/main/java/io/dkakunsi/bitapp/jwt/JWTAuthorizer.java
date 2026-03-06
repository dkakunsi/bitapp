package io.dkakunsi.bitapp.jwt;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import org.apache.commons.lang3.StringUtils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;

import io.dkakunsi.bitapp.common.AuthorizedPrincipal;
import io.dkakunsi.bitapp.common.Authorizer;
import io.dkakunsi.bitapp.common.Configuration;

public class JWTAuthorizer implements Authorizer {

  public static final String JWT_PUBLIC_KEY = "JWT_PUBLIC_KEY";

  private static final String EMAIL_CLAIM = "email";

  protected RSAPublicKey publicKey;

  protected JWTAuthorizer(RSAPublicKey publicKey) {
    this.publicKey = publicKey;
  }

  public static JWTAuthorizer of(Configuration configuration) {
    var publicKeyString = configuration.get(JWT_PUBLIC_KEY)
        .orElseThrow(() -> new RuntimeException("JWT_PUBLIC_KEY is not configured correctly"));
    try {
      var publicKey = toRSAPublicKey(publicKeyString.getBytes(StandardCharsets.UTF_8.name()));
      return new JWTAuthorizer(publicKey);
    } catch (UnsupportedEncodingException ex) {
      throw new RuntimeException("Invalid security configuration", ex);
    }
  }

  protected static RSAPublicKey toRSAPublicKey(byte[] byteKey) {
    try {
      var keySpec = new X509EncodedKeySpec(decode(byteKey));
      var keyFactory = KeyFactory.getInstance("RSA");
      return (RSAPublicKey) keyFactory.generatePublic(keySpec);
    } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
      throw new RuntimeException("Cannot create RSA key for authentication", ex);
    }
  }

  @Override
  public AuthorizedPrincipal verify(String key) {
    if (StringUtils.isBlank(key)) {
      throw new IllegalArgumentException("Token is not valid");
    }

    var token = key.replace("Bearer ", "");
    var algorithm = Algorithm.RSA256(publicKey, null);
    var verifier = JWT.require(algorithm).build();
    try {
      var jwt = verifier.verify(token);
      var email = jwt.getClaim(EMAIL_CLAIM).asString();
      return new AuthorizedPrincipal(email);
    } catch (JWTVerificationException ex) {
      throw new IllegalArgumentException("Token is not valid", ex);
    }
  }

  private static byte[] decode(byte[] encoded) {
    return Base64.getDecoder().decode(encoded);
  }
}
