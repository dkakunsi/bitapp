package io.dkakunsi.bitapp.transaction.domain.processor;

import io.dkakunsi.bitapp.account.domain.repository.AccountRepository;
import io.dkakunsi.bitapp.loan.domain.repository.LoanRepository;
import io.dkakunsi.bitapp.transaction.domain.entity.Transaction;
import io.dkakunsi.bitapp.transaction.domain.repository.TransactionRepository;

public final class UserTransactionProcessor implements TransactionProcessor {

  protected final TransactionRepository transactionRepository;
  protected final AccountRepository accountRepository;
  protected final LoanRepository loanRepository;

  public UserTransactionProcessor(
      TransactionRepository transactionRepository,
      AccountRepository accountRepository,
      LoanRepository loanRepository) {
    this.transactionRepository = transactionRepository;
    this.accountRepository = accountRepository;
    this.loanRepository = loanRepository;
  }

  @Override
  public Transaction process(Transaction transaction) {
    var createdTransaction = this.transactionRepository.create(transaction);

    switch (createdTransaction.type()) {
      case DEBIT -> {
        accountRepository.debitBalance(
            createdTransaction.source(),
            createdTransaction.amount());
      }
      case CREDIT -> {
        accountRepository.creditBalance(
            createdTransaction.destination(),
            createdTransaction.amount());
      }
      case TRANSFER -> {
        accountRepository.debitBalance(
            createdTransaction.source(),
            createdTransaction.amount());
        accountRepository.creditBalance(
            createdTransaction.destination(),
            createdTransaction.amount());
      }
    }

    if (transaction.loan() != null) {
      /*
       * For loan repayment, it is only substracting the remaining amount.
       * There are no chances for it to increase the remaining amount through
       * transaction.
       */
      loanRepository.decreaseRemainingAmount(createdTransaction.loan(), createdTransaction.amount());
    }

    return createdTransaction;
  }
}
