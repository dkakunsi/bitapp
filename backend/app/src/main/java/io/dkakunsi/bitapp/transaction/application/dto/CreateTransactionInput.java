package io.dkakunsi.bitapp.transaction.application.dto;

import io.dkakunsi.bitapp.transaction.domain.entity.Transaction;

public interface CreateTransactionInput {
  Transaction toTransaction(String requester);
}
