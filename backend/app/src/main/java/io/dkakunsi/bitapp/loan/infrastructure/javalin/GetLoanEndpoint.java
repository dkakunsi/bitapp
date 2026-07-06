package io.dkakunsi.bitapp.loan.infrastructure.javalin;

import java.lang.reflect.Type;

import io.dkakunsi.bitapp.javalin.JavalinEndpoint;
import io.dkakunsi.bitapp.loan.application.dto.LoanResult;
import io.dkakunsi.bitapp.loan.application.usecase.GetLoan;
import io.javalin.http.Context;

public final class GetLoanEndpoint extends JavalinEndpoint<String, LoanResult> {

  public GetLoanEndpoint(GetLoan usecase) {
    super(usecase);
  }

  @Override
  public Method getMethod() {
    return Method.GET;
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
