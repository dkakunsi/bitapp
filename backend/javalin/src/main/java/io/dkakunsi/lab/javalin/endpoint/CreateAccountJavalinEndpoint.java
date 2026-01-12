package io.dkakunsi.lab.javalin.endpoint;

import java.lang.reflect.Type;

import io.dkakunsi.bitapp.account.dto.CreateAccountInput;
import io.dkakunsi.bitapp.account.dto.CreateAccountResult;
import io.dkakunsi.bitapp.account.usecase.CreateAccount;
import io.dkakunsi.lab.javalin.JavalinEndpoint;
import io.javalin.http.Context;

public class CreateAccountJavalinEndpoint extends JavalinEndpoint<CreateAccountInput, CreateAccountResult> {

  public CreateAccountJavalinEndpoint(CreateAccount usecase) {
    super(usecase);
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
