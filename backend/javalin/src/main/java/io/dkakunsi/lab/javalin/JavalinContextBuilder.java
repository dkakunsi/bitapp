package io.dkakunsi.lab.javalin;

import io.dkakunsi.bitapp.common.AuthorizedPrincipal;
import io.dkakunsi.bitapp.common.Context;
import io.dkakunsi.bitapp.common.Endpoint.Header;
import lombok.Builder;

@Builder
public final class JavalinContextBuilder {

  private io.javalin.http.Context context;
  private AuthorizedPrincipal requester;

  private static String fromHeader(io.javalin.http.Context context, Header headerKey) {
    var headerValue = context.header(headerKey.getName());
    return headerValue != null ? headerValue : "";
  }

  public Context build() {
    return Context.builder()
        .requester(requester != null ? requester.email() : null)
        .requestId(fromHeader(context, Header.REQUEST_ID))
        .authorizationToken(fromHeader(context, Header.AUTH))
        .build();
  }
}
