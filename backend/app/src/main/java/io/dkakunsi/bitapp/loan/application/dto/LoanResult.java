package io.dkakunsi.bitapp.loan.application.dto;

import java.math.BigDecimal;

import io.dkakunsi.bitapp.loan.domain.entity.Loan;
import lombok.Builder;

@Builder
public final record LoanResult(
    String id,
    String user,
    String account,
    String type,
    Long date,
    Integer time,
    String partyName,
    String title,
    String description,
    BigDecimal amount,
    BigDecimal remainingAmount,
    String currency,
    double interestRate) {

  public static LoanResult from(Loan loan) {
    return LoanResult.builder()
        .id(loan.id().value())
        .user(loan.user().value())
        .account(loan.account().value())
        .type(loan.type().name())
        .date(loan.dateInEpochMillis())
        .time(loan.timeInMinutesSinceMidnight())
        .partyName(loan.partyName())
        .title(loan.title())
        .description(loan.description())
        .amount(loan.amount())
        .remainingAmount(loan.remainingAmount())
        .currency(loan.currency().getCurrencyCode())
        .interestRate(loan.interestRate())
        .build();
  }
}
