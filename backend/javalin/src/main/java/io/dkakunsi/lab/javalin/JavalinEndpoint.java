package io.dkakunsi.lab.javalin;

import io.dkakunsi.bitapp.common.AuthorizedPrincipal;
import io.dkakunsi.bitapp.common.Authorizer;
import io.dkakunsi.bitapp.common.Context;
import io.dkakunsi.bitapp.common.Endpoint;
import io.dkakunsi.bitapp.common.usecase.UseCase;
import io.javalin.http.Handler;
import io.javalin.http.HandlerType;
import io.javalin.http.UnauthorizedResponse;
import jakarta.validation.constraints.NotNull;

public abstract class JavalinEndpoint<S, T> extends Endpoint<S, T> {

  public JavalinEndpoint(@NotNull UseCase<S, T> usecase,
      Authorizer authorizer) {
    super(usecase, authorizer);
  }

  public HandlerType getHandlerType() {
    return switch (getMethod()) {
      case POST -> HandlerType.POST;
      case PUT -> HandlerType.PUT;
      case PATCH -> HandlerType.PATCH;
      case GET -> HandlerType.GET;
      case DELETE -> HandlerType.DELETE;
      default -> throw new IllegalArgumentException("Not supported method: " + getMethod());
    };
  }

  protected abstract Handler getHandler();

  protected AuthorizedPrincipal authorizeRequest(io.javalin.http.Context ctx) {
    if (authorizer == null || isPreflightRequest(ctx.method().toString())) {
      // No authentication provider means this endpoint is open to all
      return null;
    }
    var sessionKey = ctx.header(Header.AUTH.getName());
    try {
      return authorizeRequest(sessionKey);
    } catch (IllegalArgumentException e) {
      throw new UnauthorizedResponse();
    }
  }

  protected Context initiateContext(io.javalin.http.Context ctx, AuthorizedPrincipal principal) {
    var contextBuilder = JavalinContextBuilder.builder()
        .context(ctx)
        .requester(principal)
        .build();
    var context = contextBuilder.build();
    Context.set(context);
    return context;
  }
}
