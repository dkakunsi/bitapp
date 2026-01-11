package io.dkakunsi.lab.javalin.endpoint;

import java.lang.reflect.Type;

import io.dkakunsi.bitapp.common.Authorizer;
import io.dkakunsi.bitapp.common.usecase.UseCase;
import io.dkakunsi.bitapp.user.dto.RegisterUserInput;
import io.dkakunsi.bitapp.user.dto.RegisterUserResult;
import io.dkakunsi.lab.javalin.JavalinEndpoint;
import io.javalin.http.Context;
import jakarta.validation.constraints.NotNull;

public class RegisterUserJavalinEndpoint extends JavalinEndpoint<RegisterUserInput, RegisterUserResult> {

  public RegisterUserJavalinEndpoint(@NotNull UseCase<RegisterUserInput, RegisterUserResult> usecase,
      Authorizer authorizer) {
    super(usecase, authorizer);
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
