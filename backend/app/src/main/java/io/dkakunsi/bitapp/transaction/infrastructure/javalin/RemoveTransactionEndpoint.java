package io.dkakunsi.bitapp.transaction.infrastructure.javalin;

import java.lang.reflect.Type;

import io.dkakunsi.bitapp.javalin.JavalinEndpoint;
import io.dkakunsi.bitapp.transaction.application.dto.TransactionResult;
import io.dkakunsi.bitapp.transaction.application.usecase.RemoveTransaction;
import io.javalin.http.Context;

public final class RemoveTransactionEndpoint extends JavalinEndpoint<String, TransactionResult> {

  public RemoveTransactionEndpoint(RemoveTransaction usecase) {
    super(usecase);
  }

  @Override
  public Method getMethod() {
    return Method.DELETE;
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
