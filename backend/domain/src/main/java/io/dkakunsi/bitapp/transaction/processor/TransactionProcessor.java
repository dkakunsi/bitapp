package io.dkakunsi.bitapp.transaction.processor;

import io.dkakunsi.bitapp.account.repository.AccountRepository;
import io.dkakunsi.bitapp.loan.repository.LoanRepository;
import io.dkakunsi.bitapp.transaction.dto.CreateLoanDisbursementTransactionInput;
import io.dkakunsi.bitapp.transaction.dto.CreateTransactionInput;
import io.dkakunsi.bitapp.transaction.dto.CreateUserTransactionInput;
import io.dkakunsi.bitapp.transaction.entity.Transaction;
import io.dkakunsi.bitapp.transaction.repository.TransactionRepository;

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
