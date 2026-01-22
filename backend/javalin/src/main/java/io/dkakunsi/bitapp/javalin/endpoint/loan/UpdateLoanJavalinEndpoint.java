package io.dkakunsi.bitapp.javalin.endpoint.loan;

import java.lang.reflect.Type;
import java.math.BigDecimal;

import io.dkakunsi.bitapp.javalin.JavalinEndpoint;
import io.dkakunsi.bitapp.loan.dto.LoanResult;
import io.dkakunsi.bitapp.loan.dto.UpdateLoanInput;
import io.dkakunsi.bitapp.loan.usecase.UpdateLoan;
import io.javalin.http.Context;

public final class UpdateLoanJavalinEndpoint extends JavalinEndpoint<UpdateLoanInput, LoanResult> {

  public UpdateLoanJavalinEndpoint(UpdateLoan usecase) {
    super(usecase);
  }

  @Override
  public Method getMethod() {
    return Method.PUT;
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
  protected UpdateLoanInput buildInput(Context ctx) {
    var body = ctx.bodyAsClass(UpdateLoanRequest.class);
    var id = ctx.pathParam("id");

    return UpdateLoanInput.builder()
        .id(id)
        .partyName(body.partyName())
        .title(body.title())
        .description(body.description())
        .amount(body.amount())
        .currency(body.currency())
        .interestRate(body.interestRate())
        .date(body.date())
        .time(body.time())
        .build();
  }
}

final record UpdateLoanRequest(
    String partyName,
    String title,
    String description,
    BigDecimal amount,
    String currency,
    Double interestRate,
    String date,
    String time) {
}
