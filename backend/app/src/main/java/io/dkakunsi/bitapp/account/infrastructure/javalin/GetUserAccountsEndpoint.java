package io.dkakunsi.bitapp.account.infrastructure.javalin;

import java.lang.reflect.Type;
import java.util.List;

import io.dkakunsi.bitapp.account.application.dto.AccountResult;
import io.dkakunsi.bitapp.account.application.usecase.GetUserAccounts;
import io.dkakunsi.bitapp.javalin.JavalinEndpoint;
import io.javalin.http.Context;

public final class GetUserAccountsEndpoint
    extends JavalinEndpoint<String, List<AccountResult>> {

  public GetUserAccountsEndpoint(GetUserAccounts usecase) {
    super(usecase);
  }

  @Override
  public Method getMethod() {
    return Method.GET;
  }

  @Override
  public String getPath() {
    return "/v1/users/{userId}/accounts";
  }

  @Override
  protected Type getOutputClass() {
    return AccountResult.class;
  }

  @Override
  protected String buildInput(Context ctx) {
    return ctx.pathParam("userId");
  }
}
