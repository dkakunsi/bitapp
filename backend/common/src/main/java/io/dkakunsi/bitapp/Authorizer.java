package io.dkakunsi.bitapp;

import org.apache.commons.lang3.StringUtils;

public interface Authorizer {

  String EMAIL_CLAIM = "email";

  String BEARER = "Bearer "; // the trailing space is intentional

  AuthorizedPrincipal verify(String token);

  default String cleanToken(String token) {
    if (StringUtils.isBlank(token)) {
      throw new IllegalArgumentException("Token is empty");
    }
    return token.replace(BEARER, "");
  }

  public static final record AuthorizedPrincipal(String email) {
  }
}
