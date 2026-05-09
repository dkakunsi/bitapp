package io.dkakunsi.bitapp.transaction.endpoint;

import java.lang.reflect.Type;
import java.util.List;

import io.dkakunsi.bitapp.javalin.JavalinEndpoint;
import io.dkakunsi.bitapp.transaction.dto.TransactionResult;
import io.dkakunsi.bitapp.transaction.usecase.GetAccountTransactions;
import io.javalin.http.Context;

public final class GetAccountTransactionsEndpoint
    extends JavalinEndpoint<String, List<TransactionResult>> {

  public GetAccountTransactionsEndpoint(GetAccountTransactions usecase) {
    super(usecase);
  }

  @Override
  public Method getMethod() {
    return Method.GET;
  }

  @Override
  public String getPath() {
    return "/v1/accounts/{accountId}/transactions";
  }

  @Override
  protected Type getOutputClass() {
    return TransactionResult.class;
  }

  @Override
  protected String buildInput(Context ctx) {
    return ctx.pathParam("accountId");
  }
}
