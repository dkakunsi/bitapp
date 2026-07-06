package io.dkakunsi.bitapp.transaction.domain.processor;

import io.dkakunsi.bitapp.account.domain.repository.AccountRepository;
import io.dkakunsi.bitapp.transaction.domain.entity.Transaction;
import io.dkakunsi.bitapp.transaction.domain.repository.TransactionRepository;

public final class LoanDisbursementTransactionProcessor implements TransactionProcessor {

  private final TransactionRepository transactionRepository;
  private final AccountRepository accountRepository;

  public LoanDisbursementTransactionProcessor(TransactionRepository transactionRepository,
      AccountRepository accountRepository) {
    this.transactionRepository = transactionRepository;
    this.accountRepository = accountRepository;
  }

  @Override
  public Transaction process(Transaction transaction) {
    var createdTransaction = this.transactionRepository.create(transaction);

    switch (createdTransaction.type()) {
      case CREDIT -> {
        accountRepository.creditBalance(
            createdTransaction.destination(),
            createdTransaction.amount());
      }
      case DEBIT -> {
        accountRepository.debitBalance(
            createdTransaction.source(),
            createdTransaction.amount());
      }
      default -> throw new IllegalArgumentException("transaction type is not supported");
    }

    return createdTransaction;
  }
}
