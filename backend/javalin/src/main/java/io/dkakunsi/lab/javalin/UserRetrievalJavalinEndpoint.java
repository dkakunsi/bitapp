package io.dkakunsi.lab.javalin;

import io.dkakunsi.bitapp.common.Authorizer;
import io.dkakunsi.bitapp.common.usecase.Input;
import io.dkakunsi.bitapp.common.usecase.UseCase;
import io.dkakunsi.bitapp.user.dto.UserRegistrationOutput;
import io.dkakunsi.bitapp.user.dto.UserRetrievalInput;
import io.dkakunsi.bitapp.user.model.User;
import io.javalin.http.Handler;
import jakarta.validation.constraints.NotNull;

public class UserRetrievalJavalinEndpoint extends JavalinEndpoint<UserRetrievalInput, User> {

  public UserRetrievalJavalinEndpoint(@NotNull UseCase<UserRetrievalInput, User> usecase,
      Authorizer authorizer) {
    super(usecase, authorizer);
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
  protected Handler getHandler() {
    return ctx -> {
      var principal = authorizeRequest(ctx);
      var context = initiateContext(ctx, principal);
      var email = ctx.pathParam("email");
      var input = new Input<>(UserRetrievalInput.builder().email(email).build(), context);
      var output = usecase.process(input);
      if (output.isFailed()) {
        var error = output.error().get();
        ctx.status(error.code().getHttpCode()).result(error.message());
      } else if (output.isEmpty()) {
        ctx.status(NOT_FOUND_RC).result("User not found");
      } else {
        ctx.status(SUCCESS_RC).json(output.data().get(), UserRegistrationOutput.class);
      }
    };
  }
}
