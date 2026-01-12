package io.dkakunsi.lab.javalin.endpoint;

import java.lang.reflect.Type;

import io.dkakunsi.bitapp.user.dto.GetUserInput;
import io.dkakunsi.bitapp.user.dto.GetUserResult;
import io.dkakunsi.bitapp.user.usecase.GetUser;
import io.dkakunsi.lab.javalin.JavalinEndpoint;
import io.javalin.http.Context;

public class GetUserJavalinEndpoint extends JavalinEndpoint<GetUserInput, GetUserResult> {

  public GetUserJavalinEndpoint(GetUser usecase) {
    super(usecase);
  }

  @Override
  public Method getMethod() {
    return Method.GET;
  }

  @Override
  public String getPath() {
    return "/users/{email}";
  }

  @Override
  protected Type getOutputClass() {
    return GetUserResult.class;
  }

  @Override
  protected GetUserInput buildInput(Context ctx) {
    var email = ctx.pathParam("email");
    return GetUserInput.builder().email(email).build();
  }
}
