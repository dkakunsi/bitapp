package io.dkakunsi.bitapp.transaction.dto;

import io.dkakunsi.bitapp.transaction.entity.Transaction;

public interface CreateTransactionInput {
  Transaction toTransaction(String requester);
}
