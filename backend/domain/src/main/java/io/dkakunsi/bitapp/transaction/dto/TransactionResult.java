package io.dkakunsi.bitapp.transaction.dto;

import java.math.BigDecimal;

import lombok.Builder;

@Builder
public final record TransactionResult(
    String id,
    String user,
    String title,
    String description,
    Long date,
    Integer time,
    String source,
    String destination,
    String loan,
    BigDecimal amount,
    String currency,
    String category,
    String type) {
}
