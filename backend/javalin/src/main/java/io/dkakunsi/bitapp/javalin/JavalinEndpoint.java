package io.dkakunsi.bitapp.javalin;

import java.lang.reflect.Type;

import io.dkakunsi.bitapp.common.AuthorizedPrincipal;
import io.dkakunsi.bitapp.common.Authorizer;
import io.dkakunsi.bitapp.common.Context;
import io.dkakunsi.bitapp.common.Endpoint;
import io.dkakunsi.bitapp.common.Logger;
import io.dkakunsi.bitapp.common.SystemLogger;
import io.dkakunsi.bitapp.domain.usecase.Result;
import io.dkakunsi.bitapp.domain.usecase.UseCase;
import io.javalin.http.Handler;
import io.javalin.http.HandlerType;
import io.javalin.http.UnauthorizedResponse;

public abstract class JavalinEndpoint<S, T> extends Endpoint<S, T> {

  private static final Logger LOGGER = SystemLogger.getLogger(JavalinEndpoint.class);

  public JavalinEndpoint(UseCase<S, T> usecase) {
    super(usecase);
  }

  @Override
  public JavalinEndpoint<S, T> setAuthorizer(Authorizer authorizer) {
    return (JavalinEndpoint<S, T>) super.setAuthorizer(authorizer);
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

  protected Handler getHandler() {
    return ctx -> {
      var principal = authorizeRequest(ctx);
      var context = initiateContext(ctx, principal);
      var result = Context.executeInContext(context, () -> {
        LOGGER.info("Handling request for endpoint: " + getPath());
        var input = buildInput(ctx);
        return usecase.process(input);
      });
      response(ctx, result);
    };
  }

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
    return contextBuilder.build();
  }

  protected abstract S buildInput(io.javalin.http.Context ctx);

  protected void response(io.javalin.http.Context ctx, Result<T> result) {
    if (result.isFailed()) {
      failureResponse(ctx, result);
    } else {
      successResponse(ctx, result);
    }
  }

  private void failureResponse(io.javalin.http.Context ctx, Result<T> result) {
    var error = result.error().get();
    ctx.status(error.code().getHttpCode()).result(error.message());
  }

  private void successResponse(io.javalin.http.Context ctx, Result<T> result) {
    if (result.isEmpty()) {
      ctx.status(SUCCESS_RC);
    } else {
      ctx.status(SUCCESS_RC).json(result.data().get(), getOutputClass());
    }
  }

  protected abstract Type getOutputClass();
}
