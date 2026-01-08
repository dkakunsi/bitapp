package io.dkakunsi.bitapp.common;

public interface Authorizer {
  AuthorizedPrincipal verify(String token);
}
