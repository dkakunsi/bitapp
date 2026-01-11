package io.dkakunsi.lab.javalin.endpoint;

import java.lang.reflect.Type;

import io.dkakunsi.bitapp.account.dto.CreateAccountInput;
import io.dkakunsi.bitapp.account.dto.CreateAccountResult;
import io.dkakunsi.bitapp.common.Authorizer;
import io.dkakunsi.bitapp.common.usecase.UseCase;
import io.dkakunsi.lab.javalin.JavalinEndpoint;
import io.javalin.http.Context;
import jakarta.validation.constraints.NotNull;

public class CreateAccountJavalinEndpoint extends JavalinEndpoint<CreateAccountInput, CreateAccountResult> {

  public CreateAccountJavalinEndpoint(@NotNull UseCase<CreateAccountInput, CreateAccountResult> usecase,
      Authorizer authorizer) {
    super(usecase, authorizer);
  }

  @Override
  public Method getMethod() {
    return Method.POST;
  }

  @Override
  public String getPath() {
    return "/accounts";
  }

  @Override
  protected Type getOutputClass() {
    return CreateAccountResult.class;
  }

  @Override
  protected CreateAccountInput buildInput(Context ctx) {
    return ctx.bodyAsClass(CreateAccountInput.class);
  }
}
