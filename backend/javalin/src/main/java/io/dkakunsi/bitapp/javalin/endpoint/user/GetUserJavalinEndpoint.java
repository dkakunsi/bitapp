package io.dkakunsi.bitapp.javalin.endpoint.user;

import java.lang.reflect.Type;

import io.dkakunsi.bitapp.javalin.JavalinEndpoint;
import io.dkakunsi.bitapp.user.dto.GetUserInput;
import io.dkakunsi.bitapp.user.dto.UserResult;
import io.dkakunsi.bitapp.user.usecase.GetUser;
import io.javalin.http.Context;

public class GetUserJavalinEndpoint extends JavalinEndpoint<GetUserInput, UserResult> {

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
    return UserResult.class;
  }

  @Override
  protected GetUserInput buildInput(Context ctx) {
    var email = ctx.pathParam("email");
    return GetUserInput.builder().email(email).build();
  }
}
