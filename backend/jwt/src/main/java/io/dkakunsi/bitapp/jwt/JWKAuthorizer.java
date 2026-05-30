package io.dkakunsi.bitapp.jwt;

import java.net.URI;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.JwkProviderBuilder;
import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import io.dkakunsi.bitapp.common.AuthorizedPrincipal;
import io.dkakunsi.bitapp.common.Configuration;

public class JWKAuthorizer extends JWTAuthorizer {

  public static final String JWK_URL = "JWK_URL";

  // Google JWK
  private static final String DEFAULT_JWK_URL = "https://www.googleapis.com/oauth2/v3/certs";

  private final JwkProvider jwkProvider;

  protected JWKAuthorizer(JwkProvider jwkProvider) {
    super();
    this.jwkProvider = jwkProvider;
  }

  public static JWKAuthorizer of(Configuration configuration) {
    var jwkUrl = configuration.get(JWK_URL).orElse(DEFAULT_JWK_URL);
    try {
      var provider = new JwkProviderBuilder(new URI(jwkUrl).toURL())
          .cached(10, 24, TimeUnit.HOURS)
          .rateLimited(10, 1, TimeUnit.MINUTES)
          .build();
      return new JWKAuthorizer(provider);
    } catch (Exception ex) {
      throw new IllegalArgumentException("JWK_URL is not configured correctly", ex);
    }
  }

  @Override
  public AuthorizedPrincipal verify(String key) {
    var token = cleanToken(key);
    var rsaPublicKey = (RSAPublicKey) getPublicKey(token);
    return verify(rsaPublicKey, token);
  }

  private PublicKey getPublicKey(String token) {
    try {
      var decodedToken = JWT.decode(token);
      var keyId = decodedToken.getKeyId();
      if (StringUtils.isBlank(keyId)) {
        throw new IllegalArgumentException("Token doesn't have a valid key ID");
      }

      var jwk = jwkProvider.get(keyId);
      return jwk.getPublicKey();
    } catch (JwkException ex) {
      throw new RuntimeException("Cannot retrieve public key for authentication", ex);
    } catch (JWTDecodeException ex) {
      throw new IllegalArgumentException("Token is not valid", ex);
    }
  }
}
