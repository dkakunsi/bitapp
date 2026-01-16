package io.dkakunsi.bitapp.javalin.endpoint;

import java.lang.reflect.Type;
import java.math.BigDecimal;

import io.dkakunsi.bitapp.javalin.JavalinEndpoint;
import io.dkakunsi.bitapp.loan.dto.CreateLoanInput;
import io.dkakunsi.bitapp.loan.dto.CreateLoanResult;
import io.dkakunsi.bitapp.loan.usecase.CreateLoan;
import io.javalin.http.Context;

public class CreateLoanJavalinEndpoint extends JavalinEndpoint<CreateLoanInput, CreateLoanResult> {

  public CreateLoanJavalinEndpoint(CreateLoan usecase) {
    super(usecase);
  }

  @Override
  public Method getMethod() {
    return Method.POST;
  }

  @Override
  public String getPath() {
    return "/loans";
  }

  @Override
  protected Type getOutputClass() {
    return CreateLoanResult.class;
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
        .build();
  }
}

final record CreateLoanRequest(
    String type,
    String date,
    String time,
    String partyName,
    String title,
    String description,
    BigDecimal amount,
    String currency,
    double interestRate) {
}
