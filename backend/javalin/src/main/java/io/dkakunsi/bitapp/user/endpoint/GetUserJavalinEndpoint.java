package io.dkakunsi.bitapp.user.endpoint;

import java.lang.reflect.Type;

import io.dkakunsi.bitapp.javalin.JavalinEndpoint;
import io.dkakunsi.bitapp.user.dto.UserResult;
import io.dkakunsi.bitapp.user.usecase.GetUser;
import io.javalin.http.Context;

public final class GetUserJavalinEndpoint extends JavalinEndpoint<String, UserResult> {

  public GetUserJavalinEndpoint(GetUser usecase) {
    super(usecase);
  }

  @Override
  public Method getMethod() {
    return Method.GET;
  }

  @Override
  public String getPath() {
    return "/users/{email}";
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
