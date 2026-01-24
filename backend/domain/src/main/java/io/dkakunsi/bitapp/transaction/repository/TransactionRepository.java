package io.dkakunsi.bitapp.transaction.repository;

import java.util.List;
import java.util.Optional;

import io.dkakunsi.bitapp.transaction.entity.Transaction;

public interface TransactionRepository {
  Transaction create(Transaction transaction);

  Optional<Transaction> findById(String id);

  List<Transaction> findByUserId(String userId);

  List<Transaction> findByLoanId(String loanId);

  Transaction update(Transaction transaction);

  void deleteById(String id);
}
