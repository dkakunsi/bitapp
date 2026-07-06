package io.dkakunsi.bitapp.transaction.infrastructure.javalin;

import java.lang.reflect.Type;

import io.dkakunsi.bitapp.javalin.JavalinEndpoint;
import io.dkakunsi.bitapp.transaction.application.dto.TransactionResult;
import io.dkakunsi.bitapp.transaction.application.usecase.GetTransaction;
import io.javalin.http.Context;

public final class GetTransactionEndpoint extends JavalinEndpoint<String, TransactionResult> {

  public GetTransactionEndpoint(GetTransaction usecase) {
    super(usecase);
  }

  @Override
  public Method getMethod() {
    return Method.GET;
  }

  @Override
  public String getPath() {
    return "/v1/transactions/{id}";
  }

  @Override
  protected Type getOutputClass() {
    return TransactionResult.class;
  }

  @Override
  protected String buildInput(Context ctx) {
    return ctx.pathParam("id");
  }
}
