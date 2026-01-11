package io.dkakunsi.lab.javalin.endpoint;

import java.lang.reflect.Type;

import io.dkakunsi.bitapp.common.Authorizer;
import io.dkakunsi.bitapp.common.usecase.UseCase;
import io.dkakunsi.bitapp.user.dto.UpdateUserLanguageInput;
import io.dkakunsi.bitapp.user.dto.UpdateUserLanguageResult;
import io.dkakunsi.lab.javalin.JavalinEndpoint;
import io.javalin.http.Context;
import jakarta.validation.constraints.NotNull;

public class UpdateUserLanguageJavalinEndpoint
    extends JavalinEndpoint<UpdateUserLanguageInput, UpdateUserLanguageResult> {

  public UpdateUserLanguageJavalinEndpoint(@NotNull UseCase<UpdateUserLanguageInput, UpdateUserLanguageResult> usecase,
      Authorizer authorizer) {
    super(usecase, authorizer);
  }

  @Override
  public Method getMethod() {
    return Method.PATCH;
  }

  @Override
  public String getPath() {
    return "/users/{email}/language";
  }

  @Override
  protected Type getOutputClass() {
    return UpdateUserLanguageResult.class;
  }

  @Override
  protected UpdateUserLanguageInput buildInput(Context ctx) {
    var email = ctx.pathParam("email");
    var bodyInput = ctx.bodyAsClass(UpdateUserLanguageInput.class);
    return UpdateUserLanguageInput.builder()
        .email(email)
        .language(bodyInput.language())
        .build();
  }
}
