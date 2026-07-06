package io.dkakunsi.bitapp.transaction.infrastructure.javalin;

import java.lang.reflect.Type;
import java.util.List;

import io.dkakunsi.bitapp.javalin.JavalinEndpoint;
import io.dkakunsi.bitapp.transaction.application.dto.TransactionResult;
import io.dkakunsi.bitapp.transaction.application.usecase.GetLoanTransactions;
import io.javalin.http.Context;

public final class GetLoanTransactionsEndpoint
    extends JavalinEndpoint<String, List<TransactionResult>> {

  public GetLoanTransactionsEndpoint(GetLoanTransactions usecase) {
    super(usecase);
  }

  @Override
  public Method getMethod() {
    return Method.GET;
  }

  @Override
  public String getPath() {
    return "/v1/loans/{loanId}/transactions";
  }

  @Override
  protected Type getOutputClass() {
    return TransactionResult.class;
  }

  @Override
  protected String buildInput(Context ctx) {
    return ctx.pathParam("loanId");
  }
}
