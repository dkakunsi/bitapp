package io.dkakunsi.bitapp.user.endpoint;

import java.lang.reflect.Type;

import io.dkakunsi.bitapp.javalin.JavalinEndpoint;
import io.dkakunsi.bitapp.user.dto.UpdateUserLanguageInput;
import io.dkakunsi.bitapp.user.dto.UserResult;
import io.dkakunsi.bitapp.user.usecase.UpdateUserLanguage;
import io.javalin.http.Context;

public final class UpdateUserLanguageJavalinEndpoint
    extends JavalinEndpoint<UpdateUserLanguageInput, UserResult> {

  public UpdateUserLanguageJavalinEndpoint(UpdateUserLanguage usecase) {
    super(usecase);
  }

  @Override
  public Method getMethod() {
    return Method.PATCH;
  }

  @Override
  public String getPath() {
    return "/users/{email}/language/{language}";
  }

  @Override
  protected Type getOutputClass() {
    return UserResult.class;
  }

  @Override
  protected UpdateUserLanguageInput buildInput(Context ctx) {
    var email = ctx.pathParam("email");
    var language = ctx.pathParam("language");

    return UpdateUserLanguageInput.builder()
        .email(email)
        .language(language)
        .build();
  }
}
