package io.dkakunsi.bitapp.javalin.endpoint.account;

import java.lang.reflect.Type;

import io.dkakunsi.bitapp.account.dto.AccountResult;
import io.dkakunsi.bitapp.account.dto.CreateAccountInput;
import io.dkakunsi.bitapp.account.usecase.CreateAccount;
import io.dkakunsi.bitapp.javalin.JavalinEndpoint;
import io.javalin.http.Context;

public final class CreateAccountJavalinEndpoint extends JavalinEndpoint<CreateAccountInput, AccountResult> {

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
    return AccountResult.class;
  }

  @Override
  protected CreateAccountInput buildInput(Context ctx) {
    var body = ctx.bodyAsClass(CreateAccountRequest.class);
    return CreateAccountInput.builder()
        .name(body.name())
        .type(body.type())
        .themeColor(body.themeColor())
        .build();
  }
}

final record CreateAccountRequest(
    String name,
    String type,
    String themeColor) {
}
