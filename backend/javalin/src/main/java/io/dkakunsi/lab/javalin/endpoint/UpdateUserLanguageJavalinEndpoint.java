package io.dkakunsi.lab.javalin.endpoint;

import java.lang.reflect.Type;

import io.dkakunsi.bitapp.user.dto.UpdateUserLanguageInput;
import io.dkakunsi.bitapp.user.dto.UpdateUserLanguageResult;
import io.dkakunsi.bitapp.user.model.User.Language;
import io.dkakunsi.bitapp.user.usecase.UpdateUserLanguage;
import io.dkakunsi.lab.javalin.JavalinEndpoint;
import io.javalin.http.Context;

public class UpdateUserLanguageJavalinEndpoint
    extends JavalinEndpoint<UpdateUserLanguageInput, UpdateUserLanguageResult> {

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
    return UpdateUserLanguageResult.class;
  }

  @Override
  protected UpdateUserLanguageInput buildInput(Context ctx) {
    var email = ctx.pathParam("email");
    var language = ctx.pathParam("language");

    try {
      return UpdateUserLanguageInput.builder()
          .email(email)
          .language(Language.valueOf(language))
          .build();
    } catch (IllegalArgumentException ex) {
      throw new IllegalArgumentException("Invalid language: " + language);
    }
  }
}
