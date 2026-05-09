package io.dkakunsi.bitapp.loan.endpoint;

import java.lang.reflect.Type;

import io.dkakunsi.bitapp.javalin.JavalinEndpoint;
import io.dkakunsi.bitapp.loan.dto.LoanResult;
import io.dkakunsi.bitapp.loan.usecase.RemoveLoan;
import io.javalin.http.Context;

public final class RemoveLoanEndpoint extends JavalinEndpoint<String, LoanResult> {

  public RemoveLoanEndpoint(RemoveLoan usecase) {
    super(usecase);
  }

  @Override
  public Method getMethod() {
    return Method.DELETE;
  }

  @Override
  public String getPath() {
    return "/v1/loans/{id}";
  }

  @Override
  protected Type getOutputClass() {
    return LoanResult.class;
  }

  @Override
  protected String buildInput(Context ctx) {
    return ctx.pathParam("id");
  }
}
