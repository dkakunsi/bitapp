package io.dkakunsi.bitapp.account.endpoint;

import java.lang.reflect.Type;

import io.dkakunsi.bitapp.account.dto.AccountResult;
import io.dkakunsi.bitapp.account.usecase.GetAccount;
import io.dkakunsi.bitapp.javalin.JavalinEndpoint;
import io.javalin.http.Context;

public final class GetAccountJavalinEndpoint extends JavalinEndpoint<String, AccountResult> {

  public GetAccountJavalinEndpoint(GetAccount usecase) {
    super(usecase);
  }

  @Override
  public Method getMethod() {
    return Method.GET;
  }

  @Override
  public String getPath() {
    return "/accounts/{id}";
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
