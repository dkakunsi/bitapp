package io.dkakunsi.bitapp.transaction.util;

import java.time.LocalDateTime;

import io.dkakunsi.bitapp.transaction.entity.Transaction;

public final class TransactionUpdateHelper {

  private TransactionUpdateHelper() {
  }

  public static Transaction removeLoanReference(Transaction transaction, String requester) {
    return Transaction.builder()
        .id(transaction.id())
        .user(transaction.user())
        .title(transaction.title())
        .description(transaction.description())
        .date(transaction.date())
        .time(transaction.time())
        .source(transaction.source())
        .destination(transaction.destination())
        .loan(null)
        .amount(transaction.amount())
        .currency(transaction.currency())
        .category(transaction.category())
        .type(transaction.type())
        .status(transaction.status())
        .createdAt(transaction.createdAt())
        .updatedAt(LocalDateTime.now())
        .createdBy(transaction.createdBy())
        .updatedBy(requester)
        .build();
  }
}
