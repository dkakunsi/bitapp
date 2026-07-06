package io.dkakunsi.bitapp.transaction.domain.processor;

import io.dkakunsi.bitapp.account.domain.repository.AccountRepository;
import io.dkakunsi.bitapp.loan.domain.repository.LoanRepository;
import io.dkakunsi.bitapp.transaction.application.dto.CreateLoanDisbursementTransactionInput;
import io.dkakunsi.bitapp.transaction.application.dto.CreateTransactionInput;
import io.dkakunsi.bitapp.transaction.application.dto.CreateUserTransactionInput;
import io.dkakunsi.bitapp.transaction.domain.entity.Transaction;
import io.dkakunsi.bitapp.transaction.domain.repository.TransactionRepository;

public interface TransactionProcessor {

  static <T extends CreateTransactionInput> TransactionProcessor getTransactionProcessor(Class<T> type,
      TransactionRepository transactionRepository, AccountRepository accountRepository, LoanRepository loanRepository) {
    if (type == null) {
      throw new IllegalArgumentException("type: invalid value");
    }

    if (type == CreateUserTransactionInput.class) {
      return new UserTransactionProcessor(
          transactionRepository,
          accountRepository,
          loanRepository);
    } else if (type == CreateLoanDisbursementTransactionInput.class) {
      return new LoanDisbursementTransactionProcessor(transactionRepository, accountRepository);
    } else {
      throw new IllegalArgumentException("type: not supported");
    }
  }

  Transaction process(Transaction transaction);
}
