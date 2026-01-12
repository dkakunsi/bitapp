package io.dkakunsi.lab.javalin.endpoint;

import java.lang.reflect.Type;

import io.dkakunsi.bitapp.user.dto.RegisterUserInput;
import io.dkakunsi.bitapp.user.dto.RegisterUserResult;
import io.dkakunsi.bitapp.user.usecase.RegisterUser;
import io.dkakunsi.lab.javalin.JavalinEndpoint;
import io.javalin.http.Context;

public class RegisterUserJavalinEndpoint extends JavalinEndpoint<RegisterUserInput, RegisterUserResult> {

  public RegisterUserJavalinEndpoint(RegisterUser usecase) {
    super(usecase);
  }

  @Override
  public Method getMethod() {
    return Method.POST;
  }

  @Override
  public String getPath() {
    return "/users";
  }

  @Override
  protected Type getOutputClass() {
    return RegisterUserResult.class;
  }

  @Override
  protected RegisterUserInput buildInput(Context ctx) {
    return ctx.bodyAsClass(RegisterUserInput.class);
  }
}
