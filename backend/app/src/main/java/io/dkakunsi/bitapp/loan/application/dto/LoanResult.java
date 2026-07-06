package io.dkakunsi.bitapp.loan.application.dto;

import java.math.BigDecimal;

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
}
