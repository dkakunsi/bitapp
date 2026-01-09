package io.dkakunsi.lab.javalin;

import io.dkakunsi.bitapp.account.dto.CreateAccountInput;
import io.dkakunsi.bitapp.account.dto.CreateAccountOutput;
import io.dkakunsi.bitapp.common.Authorizer;
import io.dkakunsi.bitapp.common.usecase.Input;
import io.dkakunsi.bitapp.common.usecase.UseCase;
import io.javalin.http.Handler;
import jakarta.validation.constraints.NotNull;

public class CreateAccountJavalinEndpoint extends JavalinEndpoint<CreateAccountInput, CreateAccountOutput> {

  public CreateAccountJavalinEndpoint(@NotNull UseCase<CreateAccountInput, CreateAccountOutput> usecase,
      Authorizer authorizer) {
    super(usecase, authorizer);
  }

  @Override
  public Method getMethod() {
    return Method.POST;
  }

  @Override
  public String getPath() {
    return "/accounts";
  }

  @Override
  protected Handler getHandler() {
    return ctx -> {
      var principal = authorizeRequest(ctx);
      var context = initiateContext(ctx, principal);
      var input = new Input<>(ctx.bodyAsClass(CreateAccountInput.class), context);
      var output = usecase.process(input);
      if (output.isFailed()) {
        var error = output.error().get();
        ctx.status(error.code().getHttpCode()).result(error.message());
      } else if (output.isEmpty()) {
        ctx.status(CREATED_RC);
      } else {
        ctx.status(CREATED_RC).json(output.data().get(), CreateAccountOutput.class);
      }
    };
  }
}
