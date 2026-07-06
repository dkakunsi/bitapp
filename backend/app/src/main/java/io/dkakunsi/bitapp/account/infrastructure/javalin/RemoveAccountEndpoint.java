package io.dkakunsi.bitapp.account.infrastructure.javalin;

import java.lang.reflect.Type;

import io.dkakunsi.bitapp.account.application.dto.AccountResult;
import io.dkakunsi.bitapp.account.application.usecase.RemoveAccount;
import io.dkakunsi.bitapp.javalin.JavalinEndpoint;
import io.javalin.http.Context;

public final class RemoveAccountEndpoint extends JavalinEndpoint<String, AccountResult> {

  public RemoveAccountEndpoint(RemoveAccount usecase) {
    super(usecase);
  }

  @Override
  public Method getMethod() {
    return Method.DELETE;
  }

  @Override
  public String getPath() {
    return "/v1/accounts/{id}";
  }

  @Override
  protected Type getOutputClass() {
    return AccountResult.class;
  }

  @Override
  protected String buildInput(Context ctx) {
    return ctx.pathParam("id");
  }
}
