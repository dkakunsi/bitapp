package io.dkakunsi.bitapp.jwt;

import java.net.URI;
import java.security.interfaces.RSAPublicKey;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.JwkProviderBuilder;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;

import io.dkakunsi.bitapp.common.AuthorizedPrincipal;
import io.dkakunsi.bitapp.common.Authorizer;
import io.dkakunsi.bitapp.common.Configuration;

public class GoogleJWTAuthorizer implements Authorizer {

  public static final String GOOGLE_JWKS_URL = "GOOGLE_JWKS_URL";

  private static final String DEFAULT_GOOGLE_JWKS_URL = "https://www.googleapis.com/robot/v1/metadata/x509/securetoken@system.gserviceaccount.com";
  private static final String EMAIL_CLAIM = "email";

  private final JwkProvider jwkProvider;

  protected GoogleJWTAuthorizer(JwkProvider jwkProvider) {
    this.jwkProvider = jwkProvider;
  }

  public static GoogleJWTAuthorizer of(Configuration configuration) {
    var jwksUrl = configuration.get(GOOGLE_JWKS_URL).orElse(DEFAULT_GOOGLE_JWKS_URL);
    try {
      var provider = new JwkProviderBuilder(new URI(jwksUrl).toURL())
          .cached(10, 24, TimeUnit.HOURS)
          .rateLimited(10, 1, TimeUnit.MINUTES)
          .build();
      return new GoogleJWTAuthorizer(provider);
    } catch (Exception ex) {
      throw new RuntimeException("GOOGLE_JWKS_URL is not configured correctly", ex);
    }
  }

  @Override
  public AuthorizedPrincipal verify(String key) {
    if (StringUtils.isBlank(key)) {
      throw new IllegalArgumentException("Token is not valid");
    }

    var token = key.replace("Bearer ", "");
    try {
      var decodedToken = JWT.decode(token);
      var keyId = decodedToken.getKeyId();
      if (StringUtils.isBlank(keyId)) {
        throw new IllegalArgumentException("Token is not valid");
      }

      var jwk = jwkProvider.get(keyId);
      var publicKey = (RSAPublicKey) jwk.getPublicKey();
      var verifier = JWT.require(Algorithm.RSA256(publicKey, null)).build();
      var jwt = verifier.verify(token);
      var email = jwt.getClaim(EMAIL_CLAIM).asString();
      return new AuthorizedPrincipal(email);
    } catch (JWTVerificationException | JwkException ex) {
      throw new IllegalArgumentException("Token is not valid", ex);
    }
  }
}
