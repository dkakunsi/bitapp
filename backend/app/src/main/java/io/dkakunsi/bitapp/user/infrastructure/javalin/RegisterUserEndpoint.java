package io.dkakunsi.bitapp.user.infrastructure.javalin;

import java.lang.reflect.Type;

import io.dkakunsi.bitapp.javalin.JavalinEndpoint;
import io.dkakunsi.bitapp.user.application.dto.RegisterUserInput;
import io.dkakunsi.bitapp.user.application.dto.UserResult;
import io.dkakunsi.bitapp.user.application.usecase.RegisterUser;
import io.javalin.http.Context;

public final class RegisterUserEndpoint extends JavalinEndpoint<RegisterUserInput, UserResult> {

  public RegisterUserEndpoint(RegisterUser usecase) {
    super(usecase);
  }

  @Override
  public Method getMethod() {
    return Method.POST;
  }

  @Override
  public String getPath() {
    return "/v1/users";
  }

  @Override
  protected Type getOutputClass() {
    return UserResult.class;
  }

  @Override
  protected RegisterUserInput buildInput(Context ctx) {
    return ctx.bodyAsClass(RegisterUserInput.class);
  }
}
