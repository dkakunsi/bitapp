package io.dkakunsi.bitapp.account.endpoint;

import java.lang.reflect.Type;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.dkakunsi.bitapp.account.dto.AccountResult;
import io.dkakunsi.bitapp.account.dto.CreateAccountInput;
import io.dkakunsi.bitapp.account.usecase.CreateAccount;
import io.dkakunsi.bitapp.javalin.JavalinEndpoint;
import io.javalin.http.Context;

public final class CreateAccountEndpoint extends JavalinEndpoint<CreateAccountInput, AccountResult> {

  public CreateAccountEndpoint(CreateAccount usecase) {
    super(usecase);
  }

  @Override
  public Method getMethod() {
    return Method.POST;
  }

  @Override
  public String getPath() {
    return "/v1/accounts";
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

@JsonIgnoreProperties(ignoreUnknown = true)
final record CreateAccountRequest(
    String name,
    String type,
    String themeColor) {
}
