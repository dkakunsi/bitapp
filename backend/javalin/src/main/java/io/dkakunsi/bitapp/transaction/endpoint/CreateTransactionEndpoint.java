package io.dkakunsi.bitapp.transaction.endpoint;

import java.lang.reflect.Type;

import io.dkakunsi.bitapp.javalin.JavalinEndpoint;
import io.dkakunsi.bitapp.transaction.dto.CreateTransactionInput;
import io.dkakunsi.bitapp.transaction.dto.TransactionResult;
import io.dkakunsi.bitapp.transaction.usecase.CreateTransaction;
import io.javalin.http.Context;
import io.javalin.http.Handler;

public final class CreateTransactionEndpoint extends JavalinEndpoint<CreateTransactionInput, TransactionResult> {

  public CreateTransactionEndpoint(CreateTransaction usecase) {
    super(usecase);
  }

  @Override
  public Method getMethod() {
    return Method.POST;
  }

  @Override
  public String getPath() {
    return "/transactions";
  }

  @Override
  protected Type getOutputClass() {
    return TransactionResult.class;
  }

  @Override
  protected Handler getHandler() {
    return ctx -> {
      try {
        var principal = authorizeRequest(ctx);
        var context = initiateContext(ctx, principal);
        var input = buildInput(ctx);
        var result = usecase.process(context, input);
        response(ctx, result);
      } catch (IllegalArgumentException e) {
        // Validation errors during input building
        ctx.status(400).result(e.getMessage());
      }
    };
  }

  @Override
  protected CreateTransactionInput buildInput(Context ctx) {
    var body = ctx.bodyAsClass(CreateTransactionRequest.class);
    return CreateTransactionInput.fromRequest(
        body.title(),
        body.description(),
        body.date(),
        body.time(),
        body.source(),
        body.destination(),
        body.loan(),
        body.amount(),
        body.currency(),
        body.category(),
        body.type());
  }
}

record CreateTransactionRequest(
    String title,
    String description,
    String date,
    String time,
    String source,
    String destination,
    String loan,
    Long amount,
    String currency,
    String category,
    String type) {
}
