package io.dkakunsi.bitapp.loan.infrastructure.javalin;

import java.lang.reflect.Type;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.dkakunsi.bitapp.javalin.JavalinEndpoint;
import io.dkakunsi.bitapp.loan.application.dto.LoanResult;
import io.dkakunsi.bitapp.loan.application.dto.UpdateLoanInput;
import io.dkakunsi.bitapp.loan.application.usecase.UpdateLoan;
import io.javalin.http.Context;

public final class UpdateLoanEndpoint extends JavalinEndpoint<UpdateLoanInput, LoanResult> {

  public UpdateLoanEndpoint(UpdateLoan usecase) {
    super(usecase);
  }

  @Override
  public Method getMethod() {
    return Method.PUT;
  }

  @Override
  public String getPath() {
    return "/v1/loans/{id}";
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

@JsonIgnoreProperties(ignoreUnknown = true)
final record UpdateLoanRequest(
    String partyName,
    String title,
    String description,
    BigDecimal amount,
    String currency,
    Double interestRate,
    Long date,
    Integer time) {
}
