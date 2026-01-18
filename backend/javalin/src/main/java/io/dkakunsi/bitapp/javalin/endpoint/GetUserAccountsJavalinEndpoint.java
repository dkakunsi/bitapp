package io.dkakunsi.bitapp.javalin.endpoint;

import java.lang.reflect.Type;
import java.util.List;

import io.dkakunsi.bitapp.account.dto.GetUserAccountsInput;
import io.dkakunsi.bitapp.account.dto.GetUserAccountsResult;
import io.dkakunsi.bitapp.account.usecase.GetUserAccounts;
import io.dkakunsi.bitapp.javalin.JavalinEndpoint;
import io.javalin.http.Context;

public class GetUserAccountsJavalinEndpoint
    extends JavalinEndpoint<GetUserAccountsInput, List<GetUserAccountsResult>> {

  public GetUserAccountsJavalinEndpoint(GetUserAccounts usecase) {
    super(usecase);
  }

  @Override
  public Method getMethod() {
    return Method.GET;
  }

  @Override
  public String getPath() {
    return "/users/{userId}/accounts";
  }

  @Override
  protected Type getOutputClass() {
    return GetUserAccountsResult.class;
  }

  @Override
  protected GetUserAccountsInput buildInput(Context ctx) {
    var userId = ctx.pathParam("userId");
    return GetUserAccountsInput.builder().userId(userId).build();
  }
}
