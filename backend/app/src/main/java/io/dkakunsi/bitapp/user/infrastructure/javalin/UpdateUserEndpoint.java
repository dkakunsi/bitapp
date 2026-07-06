package io.dkakunsi.bitapp.user.infrastructure.javalin;

import java.lang.reflect.Type;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.dkakunsi.bitapp.javalin.JavalinEndpoint;
import io.dkakunsi.bitapp.user.application.dto.UpdateUserInput;
import io.dkakunsi.bitapp.user.application.dto.UserResult;
import io.dkakunsi.bitapp.user.application.usecase.UpdateUser;
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
    return "/v1/users/{email}";
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

@JsonIgnoreProperties(ignoreUnknown = true)
final record UpdateUserRequest(
    String language) {
}