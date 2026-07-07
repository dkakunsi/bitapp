package io.dkakunsi.bitapp.transaction.application.processor;

import io.dkakunsi.bitapp.transaction.domain.entity.Transaction;
import io.dkakunsi.bitapp.transaction.domain.port.TransactionAccountPort;
import io.dkakunsi.bitapp.transaction.domain.port.TransactionLoanPort;
import io.dkakunsi.bitapp.transaction.domain.repository.TransactionRepository;

public final class UserTransactionProcessor implements TransactionProcessor {

  protected final TransactionRepository transactionRepository;
  protected final TransactionAccountPort transactionAccountService;
  protected final TransactionLoanPort transactionLoanService;

  public UserTransactionProcessor(
      TransactionRepository transactionRepository,
      TransactionAccountPort transactionAccountService,
      TransactionLoanPort transactionLoanService) {
    this.transactionRepository = transactionRepository;
    this.transactionAccountService = transactionAccountService;
    this.transactionLoanService = transactionLoanService;
  }

  @Override
  public Transaction process(Transaction transaction) {
    var createdTransaction = this.transactionRepository.create(transaction);

    switch (createdTransaction.type()) {
      case DEBIT -> {
        transactionAccountService.debitBalance(
            createdTransaction.source(),
            createdTransaction.amount());
      }
      case CREDIT -> {
        transactionAccountService.creditBalance(
            createdTransaction.destination(),
            createdTransaction.amount());
      }
      case TRANSFER -> {
        transactionAccountService.debitBalance(
            createdTransaction.source(),
            createdTransaction.amount());
        transactionAccountService.creditBalance(
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
      transactionLoanService.decreaseRemainingAmount(createdTransaction.loan(), createdTransaction.amount());
    }

    return createdTransaction;
  }
}
