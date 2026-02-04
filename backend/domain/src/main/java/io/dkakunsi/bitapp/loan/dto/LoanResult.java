package io.dkakunsi.bitapp.loan.dto;

import java.math.BigDecimal;

import lombok.Builder;

@Builder
public final record LoanResult(
    String id,
    String user,
    String account,
    String type,
    String date,
    String time,
    String partyName,
    String title,
    String description,
    BigDecimal amount,
    BigDecimal remainingAmount,
    String currency,
    double interestRate) {
}
