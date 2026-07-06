package io.dkakunsi.bitapp.jwt;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;

import io.dkakunsi.bitapp.AuthorizedPrincipal;
import io.dkakunsi.bitapp.Authorizer;
import io.dkakunsi.bitapp.Configuration;

public class JWTAuthorizer implements Authorizer {

  public static final String JWT_PUBLIC_KEY = "JWT_PUBLIC_KEY";

  private PublicKey publicKey;

  protected JWTAuthorizer() {
  }

  protected JWTAuthorizer(PublicKey publicKey) {
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

  private static PublicKey toRSAPublicKey(byte[] byteKey) {
    try {
      var keySpec = new X509EncodedKeySpec(decode(byteKey));
      var keyFactory = KeyFactory.getInstance("RSA");
      return keyFactory.generatePublic(keySpec);
    } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
      throw new RuntimeException("Cannot create RSA key for authentication", ex);
    }
  }

  private static byte[] decode(byte[] encoded) {
    return Base64.getDecoder().decode(encoded);
  }

  @Override
  public AuthorizedPrincipal verify(String key) {
    var token = cleanToken(key);
    var rsaPublicKey = (RSAPublicKey) publicKey;
    return verify(rsaPublicKey, token);
  }

  protected AuthorizedPrincipal verify(RSAPublicKey rsaPublicKey, String token) {
    var algorithm = Algorithm.RSA256(rsaPublicKey, null);
    var verifier = JWT.require(algorithm).build();

    try {
      var jwt = verifier.verify(token);
      var email = jwt.getClaim(Authorizer.EMAIL_CLAIM).asString();
      return new AuthorizedPrincipal(email);
    } catch (JWTVerificationException ex) {
      throw new IllegalArgumentException("Token is not valid", ex);
    }
  }
}
