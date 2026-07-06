package io.dkakunsi.bitapp.loan.infrastructure.javalin;

import java.lang.reflect.Type;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.dkakunsi.bitapp.javalin.JavalinEndpoint;
import io.dkakunsi.bitapp.loan.application.dto.CreateLoanInput;
import io.dkakunsi.bitapp.loan.application.dto.LoanResult;
import io.dkakunsi.bitapp.loan.application.usecase.CreateLoan;
import io.javalin.http.Context;

public final class CreateLoanEndpoint extends JavalinEndpoint<CreateLoanInput, LoanResult> {

  public CreateLoanEndpoint(CreateLoan usecase) {
    super(usecase);
  }

  @Override
  public Method getMethod() {
    return Method.POST;
  }

  @Override
  public String getPath() {
    return "/v1/loans";
  }

  @Override
  protected Type getOutputClass() {
    return LoanResult.class;
  }

  @Override
  protected CreateLoanInput buildInput(Context ctx) {
    var body = ctx.bodyAsClass(CreateLoanRequest.class);
    return CreateLoanInput.builder()
        .type(body.type())
        .date(body.date())
        .time(body.time())
        .partyName(body.partyName())
        .title(body.title())
        .description(body.description())
        .amount(body.amount())
        .currency(body.currency())
        .interestRate(body.interestRate())
        .account(body.account())
        .build();
  }
}

@JsonIgnoreProperties(ignoreUnknown = true)
final record CreateLoanRequest(
    String type,
    Long date,
    Integer time,
    String partyName,
    String title,
    String description,
    BigDecimal amount,
    String currency,
    double interestRate,
    String account) {
}
