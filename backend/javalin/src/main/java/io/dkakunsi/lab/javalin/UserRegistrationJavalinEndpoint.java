package io.dkakunsi.lab.javalin;

import io.dkakunsi.bitapp.common.Authorizer;
import io.dkakunsi.bitapp.common.usecase.Input;
import io.dkakunsi.bitapp.common.usecase.UseCase;
import io.dkakunsi.bitapp.user.dto.UserRegistrationInput;
import io.dkakunsi.bitapp.user.dto.UserRegistrationOutput;
import io.dkakunsi.bitapp.user.model.User;
import io.javalin.http.Handler;
import jakarta.validation.constraints.NotNull;

public class UserRegistrationJavalinEndpoint extends JavalinEndpoint<UserRegistrationInput, UserRegistrationOutput> {

  public UserRegistrationJavalinEndpoint(@NotNull UseCase<UserRegistrationInput, UserRegistrationOutput> usecase,
      Authorizer authorizer) {
    super(usecase, authorizer);
  }

  @Override
  public Method getMethod() {
    return Method.POST;
  }

  @Override
  public String getPath() {
    return "/users";
  }

  @Override
  protected Handler getHandler() {
    return ctx -> {
      var principal = authorizeRequest(ctx);
      var context = initiateContext(ctx, principal);
      var input = new Input<>(ctx.bodyAsClass(UserRegistrationInput.class), context);
      var output = usecase.process(input);
      if (output.isFailed()) {
        var error = output.error().get();
        ctx.status(error.code().getHttpCode()).result(error.message());
      } else if (output.isEmpty()) {
        ctx.status(SUCCESS_RC);
      } else {
        ctx.status(SUCCESS_RC).json(output.data().get(), User.class);
      }
    };

  }
}
