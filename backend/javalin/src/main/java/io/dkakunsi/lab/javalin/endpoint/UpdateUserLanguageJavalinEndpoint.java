package io.dkakunsi.lab.javalin.endpoint;

import io.dkakunsi.bitapp.common.Authorizer;
import io.dkakunsi.bitapp.common.usecase.Input;
import io.dkakunsi.bitapp.common.usecase.UseCase;
import io.dkakunsi.bitapp.user.dto.UpdateUserLanguageInput;
import io.dkakunsi.bitapp.user.dto.UpdateUserLanguageResult;
import io.dkakunsi.lab.javalin.JavalinEndpoint;
import io.javalin.http.Handler;
import jakarta.validation.constraints.NotNull;

public class UpdateUserLanguageJavalinEndpoint extends JavalinEndpoint<UpdateUserLanguageInput, UpdateUserLanguageResult> {

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
  protected Handler getHandler() {
    return ctx -> {
      var principal = authorizeRequest(ctx);
      var context = initiateContext(ctx, principal);
      var email = ctx.pathParam("email");
      var input = new Input<>(ctx.bodyAsClass(UpdateUserLanguageInput.class), context);
      var output = usecase.process(input);
      if (output.isFailed()) {
        var error = output.error().get();
        ctx.status(error.code().getHttpCode()).result(error.message());
      } else if (output.isEmpty()) {
        ctx.status(NOT_FOUND_RC).result("User not found");
      } else {
        ctx.status(SUCCESS_RC).json(output.data().get(), UpdateUserLanguageResult.class);
      }
    };
  }
}
