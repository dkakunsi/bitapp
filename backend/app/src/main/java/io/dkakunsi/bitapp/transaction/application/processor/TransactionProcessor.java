package io.dkakunsi.bitapp.transaction.application.processor;

import io.dkakunsi.bitapp.transaction.application.dto.CreateLoanDisbursementTransactionInput;
import io.dkakunsi.bitapp.transaction.application.dto.CreateTransactionInput;
import io.dkakunsi.bitapp.transaction.application.dto.CreateUserTransactionInput;
import io.dkakunsi.bitapp.transaction.application.port.TransactionAccountPort;
import io.dkakunsi.bitapp.transaction.application.port.TransactionLoanPort;
import io.dkakunsi.bitapp.transaction.domain.entity.Transaction;
import io.dkakunsi.bitapp.transaction.domain.repository.TransactionRepository;

public interface TransactionProcessor {

  static <T extends CreateTransactionInput> TransactionProcessor getTransactionProcessor(Class<T> type,
      TransactionRepository transactionRepository, TransactionAccountPort transactionAccountService,
      TransactionLoanPort transactionLoanService) {
    if (type == null) {
      throw new IllegalArgumentException("type: invalid value");
    }

    if (type == CreateUserTransactionInput.class) {
      return new UserTransactionProcessor(
          transactionRepository,
          transactionAccountService,
          transactionLoanService);
    } else if (type == CreateLoanDisbursementTransactionInput.class) {
      return new LoanDisbursementTransactionProcessor(transactionRepository, transactionAccountService);
    } else {
      throw new IllegalArgumentException("type: not supported");
    }
  }

  Transaction process(Transaction transaction);
}
