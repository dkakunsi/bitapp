package io.dkakunsi.bitapp.javalin.endpoint.loan;

import java.lang.reflect.Type;

import io.dkakunsi.bitapp.javalin.JavalinEndpoint;
import io.dkakunsi.bitapp.loan.dto.LoanResult;
import io.dkakunsi.bitapp.loan.usecase.GetLoan;
import io.javalin.http.Context;

public final class GetLoanJavalinEndpoint extends JavalinEndpoint<String, LoanResult> {

  public GetLoanJavalinEndpoint(GetLoan usecase) {
    super(usecase);
  }

  @Override
  public Method getMethod() {
    return Method.GET;
  }

  @Override
  public String getPath() {
    return "/loans/{id}";
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
