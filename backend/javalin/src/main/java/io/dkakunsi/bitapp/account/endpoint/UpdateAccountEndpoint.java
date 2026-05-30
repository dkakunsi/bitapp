package io.dkakunsi.bitapp.account.endpoint;

import java.lang.reflect.Type;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.dkakunsi.bitapp.account.dto.AccountResult;
import io.dkakunsi.bitapp.account.dto.UpdateAccountInput;
import io.dkakunsi.bitapp.account.usecase.UpdateAccount;
import io.dkakunsi.bitapp.javalin.JavalinEndpoint;
import io.javalin.http.Context;

public final class UpdateAccountEndpoint extends JavalinEndpoint<UpdateAccountInput, AccountResult> {

  public UpdateAccountEndpoint(UpdateAccount usecase) {
    super(usecase);
  }

  @Override
  public Method getMethod() {
    return Method.PUT;
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
  protected UpdateAccountInput buildInput(Context ctx) {
    var body = parseRequestBody(ctx);
    var id = ctx.pathParam("id");

    return UpdateAccountInput.builder()
        .id(id)
        .name(body.name())
        .type(body.type())
        .themeColor(body.themeColor())
        .build();
  }

  private static UpdateAccountRequest parseRequestBody(Context ctx) {
    try {
      return ctx.bodyAsClass(UpdateAccountRequest.class);
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid request body", e);
    }
  }
}

@JsonIgnoreProperties(ignoreUnknown = true)
final record UpdateAccountRequest(
    String name,
    String type,
    String themeColor) {
}