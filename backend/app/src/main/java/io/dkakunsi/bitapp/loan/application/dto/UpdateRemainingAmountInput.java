package io.dkakunsi.bitapp.loan.application.dto;

import java.math.BigDecimal;

import lombok.Builder;

@Builder
public record UpdateRemainingAmountInput(
    String loanId,
    BigDecimal amount,
    Boolean isIncrease) {
}
