package io.dkakunsi.bitapp.transaction.repository;

import java.util.List;

import io.dkakunsi.bitapp.transaction.entity.Transaction;

public interface TransactionRepository {
  Transaction create(Transaction transaction);

  List<Transaction> findByUserId(String userId);
}
