package io.dkakunsi.lab.javalin.endpoint;

import io.dkakunsi.bitapp.common.Authorizer;
import io.dkakunsi.bitapp.common.usecase.Input;
import io.dkakunsi.bitapp.common.usecase.UseCase;
import io.dkakunsi.bitapp.user.dto.GetUserInput;
import io.dkakunsi.bitapp.user.dto.GetUserResult;
import io.dkakunsi.bitapp.user.dto.RegisterUserResult;
import io.dkakunsi.lab.javalin.JavalinEndpoint;
import io.javalin.http.Handler;
import jakarta.validation.constraints.NotNull;

public class GetUserJavalinEndpoint extends JavalinEndpoint<GetUserInput, GetUserResult> {

  public GetUserJavalinEndpoint(@NotNull UseCase<GetUserInput, GetUserResult> usecase,
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
      var input = new Input<>(GetUserInput.builder().email(email).build(), context);
      var output = usecase.process(input);
      if (output.isFailed()) {
        var error = output.error().get();
        ctx.status(error.code().getHttpCode()).result(error.message());
      } else if (output.isEmpty()) {
        ctx.status(NOT_FOUND_RC).result("User not found");
      } else {
        ctx.status(SUCCESS_RC).json(output.data().get(), RegisterUserResult.class);
      }
    };
  }
}
