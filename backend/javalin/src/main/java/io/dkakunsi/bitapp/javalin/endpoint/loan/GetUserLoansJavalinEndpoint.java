package io.dkakunsi.bitapp.javalin.endpoint.loan;

import java.lang.reflect.Type;
import java.util.List;

import io.dkakunsi.bitapp.javalin.JavalinEndpoint;
import io.dkakunsi.bitapp.loan.dto.GetUserLoansInput;
import io.dkakunsi.bitapp.loan.dto.GetUserLoansResult;
import io.dkakunsi.bitapp.loan.usecase.GetUserLoans;
import io.javalin.http.Context;

public class GetUserLoansJavalinEndpoint
    extends JavalinEndpoint<GetUserLoansInput, List<GetUserLoansResult>> {

  public GetUserLoansJavalinEndpoint(GetUserLoans usecase) {
    super(usecase);
  }

  @Override
  public Method getMethod() {
    return Method.GET;
  }

  @Override
  public String getPath() {
    return "/users/{userId}/loans";
  }

  @Override
  protected Type getOutputClass() {
    return GetUserLoansResult.class;
  }

  @Override
  protected GetUserLoansInput buildInput(Context ctx) {
    var userId = ctx.pathParam("userId");
    return GetUserLoansInput.builder().userId(userId).build();
  }
}
