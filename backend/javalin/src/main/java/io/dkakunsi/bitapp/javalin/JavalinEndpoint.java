package io.dkakunsi.bitapp.javalin;

import java.lang.reflect.Type;
import java.util.List;

import io.dkakunsi.bitapp.common.AuthorizedPrincipal;
import io.dkakunsi.bitapp.common.Authorizer;
import io.dkakunsi.bitapp.common.Context;
import io.dkakunsi.bitapp.common.Endpoint;
import io.dkakunsi.bitapp.common.Validator;
import io.dkakunsi.bitapp.common.Validator.Violation;
import io.dkakunsi.bitapp.common.usecase.Result;
import io.dkakunsi.bitapp.common.usecase.UseCase;
import io.dkakunsi.bitapp.javalin.validation.JakartaValidation;
import io.javalin.http.Handler;
import io.javalin.http.HandlerType;
import io.javalin.http.UnauthorizedResponse;
import jakarta.validation.constraints.NotNull;

public abstract class JavalinEndpoint<S, T> extends Endpoint<S, T> {

  public JavalinEndpoint(@NotNull UseCase<S, T> usecase) {
    super(usecase);
  }

  @Override
  public JavalinEndpoint<S, T> setAuthorizer(Authorizer authorizer) {
    return (JavalinEndpoint<S, T>) super.setAuthorizer(authorizer);
  }

  public JavalinEndpoint<S, T> withValidator(Validator validator) {
    return (JavalinEndpoint<S, T>) super.setValidator(validator);
  }

  public JavalinEndpoint<S, T> withValidator() {
    return withValidator(new JakartaValidation());
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
      var input = buildInput(ctx);

      if (validator != null && !validateAndRespond(ctx, input)) {
        return;
      }

      var result = usecase.process(context, input);
      response(ctx, result);
    };
  }

  private boolean validateAndRespond(io.javalin.http.Context ctx, S input) {
    var violations = validateInput(input);
    if (!violations.isEmpty()) {
      failureResponse(ctx, violations);
      return false;
    }
    return true;
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
    var context = contextBuilder.build();
    Context.set(context);
    return context;
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

  private void failureResponse(io.javalin.http.Context ctx, List<Violation> violations) {
    var messages = violations.stream().map(Violation::toString).toList();
    ctx.status(400).result(String.join(", ", messages));
  }

  protected abstract Type getOutputClass();
}
