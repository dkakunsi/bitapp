package io.dkakunsi.lab.javalin.endpoint;

import java.lang.reflect.Type;

import io.dkakunsi.bitapp.account.dto.GetUserAccountsInput;
import io.dkakunsi.bitapp.account.dto.GetUserAccountsResult;
import io.dkakunsi.bitapp.common.Authorizer;
import io.dkakunsi.bitapp.common.usecase.UseCase;
import io.dkakunsi.lab.javalin.JavalinEndpoint;
import io.javalin.http.Context;
import jakarta.validation.constraints.NotNull;

public class GetUserAccountsJavalinEndpoint
    extends JavalinEndpoint<GetUserAccountsInput, GetUserAccountsResult> {

  public GetUserAccountsJavalinEndpoint(@NotNull UseCase<GetUserAccountsInput, GetUserAccountsResult> usecase,
      Authorizer authorizer) {
    super(usecase, authorizer);
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
