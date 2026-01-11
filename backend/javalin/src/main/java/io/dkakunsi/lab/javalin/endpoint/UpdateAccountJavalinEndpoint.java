package io.dkakunsi.lab.javalin.endpoint;

import java.lang.reflect.Type;

import io.dkakunsi.bitapp.account.dto.UpdateAccountInput;
import io.dkakunsi.bitapp.account.dto.UpdateAccountResult;
import io.dkakunsi.bitapp.common.Authorizer;
import io.dkakunsi.bitapp.common.usecase.UseCase;
import io.dkakunsi.lab.javalin.JavalinEndpoint;
import io.javalin.http.Context;
import jakarta.validation.constraints.NotNull;

public class UpdateAccountJavalinEndpoint extends JavalinEndpoint<UpdateAccountInput, UpdateAccountResult> {

  public UpdateAccountJavalinEndpoint(@NotNull UseCase<UpdateAccountInput, UpdateAccountResult> usecase,
      Authorizer authorizer) {
    super(usecase, authorizer);
  }

  @Override
  public Method getMethod() {
    return Method.PUT;
  }

  @Override
  public String getPath() {
    return "/accounts/{id}";
  }

  @Override
  protected Type getOutputClass() {
    return UpdateAccountResult.class;
  }

  @Override
  protected UpdateAccountInput buildInput(Context ctx) {
    var body = ctx.bodyAsClass(UpdateAccountInput.class);
    var id = ctx.pathParam("id");

    return UpdateAccountInput.builder()
        .id(id)
        .name(body.name())
        .type(body.type())
        .themeColor(body.themeColor())
        .build();
  }
}
