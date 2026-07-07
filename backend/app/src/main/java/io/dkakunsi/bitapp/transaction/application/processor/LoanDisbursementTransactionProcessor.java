package io.dkakunsi.bitapp.transaction.application.processor;

import io.dkakunsi.bitapp.transaction.domain.entity.Transaction;
import io.dkakunsi.bitapp.transaction.domain.port.TransactionAccountPort;
import io.dkakunsi.bitapp.transaction.domain.repository.TransactionRepository;

public final class LoanDisbursementTransactionProcessor implements TransactionProcessor {

  private final TransactionRepository transactionRepository;
  private final TransactionAccountPort transactionAccountService;

  public LoanDisbursementTransactionProcessor(TransactionRepository transactionRepository,
      TransactionAccountPort transactionAccountService) {
    this.transactionRepository = transactionRepository;
    this.transactionAccountService = transactionAccountService;
  }

  @Override
  public Transaction process(Transaction transaction) {
    var createdTransaction = this.transactionRepository.create(transaction);

    switch (createdTransaction.type()) {
      case CREDIT -> {
        transactionAccountService.creditBalance(
            createdTransaction.destination(),
            createdTransaction.amount());
      }
      case DEBIT -> {
        transactionAccountService.debitBalance(
            createdTransaction.source(),
            createdTransaction.amount());
      }
      default -> throw new IllegalArgumentException("transaction type is not supported");
    }

    return createdTransaction;
  }
}
