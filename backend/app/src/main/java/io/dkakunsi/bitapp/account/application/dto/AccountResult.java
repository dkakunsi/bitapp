package io.dkakunsi.bitapp.account.application.dto;

import java.math.BigDecimal;

import lombok.Builder;

@Builder
public final record AccountResult(
    String id,
    String name,
    String type,
    String themeColor,
    BigDecimal balance,
    String user) {
}
