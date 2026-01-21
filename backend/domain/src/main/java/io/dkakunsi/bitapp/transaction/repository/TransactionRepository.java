package io.dkakunsi.bitapp.transaction.repository;

import io.dkakunsi.bitapp.transaction.entity.Transaction;

public interface TransactionRepository {
  Transaction create(Transaction transaction);
}
