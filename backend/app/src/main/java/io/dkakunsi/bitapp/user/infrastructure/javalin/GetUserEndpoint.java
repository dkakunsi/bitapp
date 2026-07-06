package io.dkakunsi.bitapp.user.infrastructure.javalin;

import java.lang.reflect.Type;

import io.dkakunsi.bitapp.javalin.JavalinEndpoint;
import io.dkakunsi.bitapp.user.application.dto.UserResult;
import io.dkakunsi.bitapp.user.application.usecase.GetUser;
import io.javalin.http.Context;

public final class GetUserEndpoint extends JavalinEndpoint<String, UserResult> {

  public GetUserEndpoint(GetUser usecase) {
    super(usecase);
  }

  @Override
  public Method getMethod() {
    return Method.GET;
  }

  @Override
  public String getPath() {
    return "/v1/users/{email}";
  }

  @Override
  protected Type getOutputClass() {
    return UserResult.class;
  }

  @Override
  protected String buildInput(Context ctx) {
    return ctx.pathParam("email");
  }
}
