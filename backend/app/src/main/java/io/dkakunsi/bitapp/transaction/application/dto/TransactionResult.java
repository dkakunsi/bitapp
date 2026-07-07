package io.dkakunsi.bitapp.transaction.application.dto;

import java.math.BigDecimal;

import io.dkakunsi.bitapp.transaction.domain.entity.Transaction;
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

  public static TransactionResult from(Transaction transaction) {
    return TransactionResult.builder()
        .id(transaction.id().value())
        .user(transaction.user().value())
        .title(transaction.title())
        .description(transaction.description())
        .date(transaction.toEpochMilli(transaction.date()))
        .time(transaction.toMinutesSinceMidnight(transaction.time()))
        .source(transaction.source() != null ? transaction.source().value() : null)
        .destination(transaction.destination() != null ? transaction.destination().value() : null)
        .loan(transaction.loan() != null ? transaction.loan().value() : null)
        .amount(transaction.amount())
        .currency(transaction.currency().getCurrencyCode())
        .category(transaction.category() != null ? transaction.category().name() : null)
        .type(transaction.type().name())
        .build();
  }
}
