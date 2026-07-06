package io.dkakunsi.bitapp.transaction.domain.repository;

import java.util.List;
import java.util.Optional;

import io.dkakunsi.bitapp.Id;
import io.dkakunsi.bitapp.transaction.domain.entity.Transaction;

public interface TransactionRepository {
  Transaction create(Transaction transaction);

  Transaction update(Transaction transaction);

  void deleteById(Id id);

  Optional<Transaction> findById(Id id);

  List<Transaction> findByUserId(Id userId);

  List<Transaction> findByAccountId(Id accountId);

  List<Transaction> findByLoanId(Id loanId);
}
