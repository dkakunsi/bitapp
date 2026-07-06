package io.dkakunsi.bitapp.transaction.infrastructure.javalin;

import java.lang.reflect.Type;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.dkakunsi.bitapp.Context;
import io.dkakunsi.bitapp.javalin.JavalinEndpoint;
import io.dkakunsi.bitapp.transaction.application.dto.CreateTransactionInput;
import io.dkakunsi.bitapp.transaction.application.dto.CreateUserTransactionInput;
import io.dkakunsi.bitapp.transaction.application.dto.TransactionResult;
import io.dkakunsi.bitapp.transaction.application.usecase.CreateTransaction;
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
    return "/v1/transactions";
  }

  @Override
  protected Type getOutputClass() {
    return TransactionResult.class;
  }

  @Override
  protected Handler getHandler() {
    return ctx -> {
      var principal = authorizeRequest(ctx);
      var context = initiateContext(ctx, principal);
      try {
        var result = Context.executeInContext(context, () -> {
          var input = buildInput(ctx);
          return usecase.process(input);
        });
        response(ctx, result);
      } catch (IllegalArgumentException e) {
        // Validation errors during input building
        ctx.status(400).result(e.getMessage());
      }
    };
  }

  @Override
  protected CreateTransactionInput buildInput(io.javalin.http.Context ctx) {
    var body = ctx.bodyAsClass(CreateTransactionRequest.class);
    return CreateUserTransactionInput.builder()
        .title(body.title())
        .description(body.description())
        .date(body.date())
        .time(body.time())
        .source(body.source())
        .destination(body.destination())
        .loan(body.loan())
        .amount(body.amount())
        .currency(body.currency())
        .category(body.category())
        .type(body.type())
        .build();
  }
}

@JsonIgnoreProperties(ignoreUnknown = true)
record CreateTransactionRequest(
    String title,
    String description,
    Long date,
    Integer time,
    String source,
    String destination,
    String loan,
    BigDecimal amount,
    String currency,
    String category,
    String type) {
}
