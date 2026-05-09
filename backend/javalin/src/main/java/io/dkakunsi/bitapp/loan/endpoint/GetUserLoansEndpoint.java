package io.dkakunsi.bitapp.loan.endpoint;

import java.lang.reflect.Type;
import java.util.List;

import io.dkakunsi.bitapp.javalin.JavalinEndpoint;
import io.dkakunsi.bitapp.loan.dto.LoanResult;
import io.dkakunsi.bitapp.loan.usecase.GetUserLoans;
import io.javalin.http.Context;

public final class GetUserLoansEndpoint
    extends JavalinEndpoint<String, List<LoanResult>> {

  public GetUserLoansEndpoint(GetUserLoans usecase) {
    super(usecase);
  }

  @Override
  public Method getMethod() {
    return Method.GET;
  }

  @Override
  public String getPath() {
    return "/v1/users/{userId}/loans";
  }

  @Override
  protected Type getOutputClass() {
    return LoanResult.class;
  }

  @Override
  protected String buildInput(Context ctx) {
    return ctx.pathParam("userId");
  }
}
