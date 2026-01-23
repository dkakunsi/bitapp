package io.dkakunsi.bitapp.user.endpoint;

import java.lang.reflect.Type;

import io.dkakunsi.bitapp.javalin.JavalinEndpoint;
import io.dkakunsi.bitapp.user.dto.UpdateUserInput;
import io.dkakunsi.bitapp.user.dto.UserResult;
import io.dkakunsi.bitapp.user.usecase.UpdateUser;
import io.javalin.http.Context;

public final class UpdateUserEndpoint
    extends JavalinEndpoint<UpdateUserInput, UserResult> {

  public UpdateUserEndpoint(UpdateUser usecase) {
    super(usecase);
  }

  @Override
  public Method getMethod() {
    return Method.PUT;
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
  protected UpdateUserInput buildInput(Context ctx) {
    var email = ctx.pathParam("email");
    var input = ctx.bodyAsClass(UpdateUserRequest.class);

    return UpdateUserInput.builder()
        .email(email)
        .language(input.language())
        .build();
  }
}

final record UpdateUserRequest(
    String language) {
}