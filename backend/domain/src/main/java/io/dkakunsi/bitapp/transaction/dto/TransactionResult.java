package io.dkakunsi.bitapp.transaction.dto;

import lombok.Builder;

@Builder
public final record TransactionResult(
    String id,
    String user,
    String title,
    String description,
    String date,
    String time,
    String source,
    String destination,
    String loan,
    Long amount,
    String currency,
    String category,
    String type) {
}
