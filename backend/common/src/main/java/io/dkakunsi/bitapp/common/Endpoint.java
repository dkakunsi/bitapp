package io.dkakunsi.bitapp.common;

import io.dkakunsi.bitapp.domain.usecase.UseCase;
import lombok.Getter;

public abstract class Endpoint<S, T> {

  protected static final int SUCCESS_RC = 200;

  protected static final int CREATED_RC = 201;

  protected static final String APPLICATION_JSON = "application/json";

  public static enum Method {
    POST, PUT, PATCH, GET, DELETE, OPTIONS
  }

  @Getter
  public static enum Header {
    AUTH("Authorization"),
    REQUEST_ID("Request-Id");

    private String name;

    private Header(String name) {
      this.name = name;
    }
  }

  protected UseCase<S, T> usecase;

  protected Authorizer authorizer;

  protected Endpoint(UseCase<S, T> usecase) {
    this.usecase = usecase;
  }

  public abstract Method getMethod();

  public abstract String getPath();

  protected UseCase<S, T> getUsecase() {
    return usecase;
  }

  protected boolean isPreflightRequest(String method) {
    return Method.OPTIONS.name().equalsIgnoreCase(method);
  }

  public Endpoint<S, T> setAuthorizer(Authorizer authorizer) {
    this.authorizer = authorizer;
    return this;
  }

  protected AuthorizedPrincipal authorizeRequest(String sessionKey) {
    if (authorizer == null) {
      throw new RuntimeException("Authentication provider is not configured");
    }
    return authorizer.verify(sessionKey);
  }
}
