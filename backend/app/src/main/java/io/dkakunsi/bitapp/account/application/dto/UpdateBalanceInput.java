package io.dkakunsi.bitapp.account.application.dto;

import java.math.BigDecimal;

import lombok.Builder;

@Builder
public record UpdateBalanceInput(
    String accountId,
    BigDecimal balance,
    Boolean isCredit) {
}
