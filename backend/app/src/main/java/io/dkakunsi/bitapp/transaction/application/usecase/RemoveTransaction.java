package io.dkakunsi.bitapp.transaction.application.usecase;

import io.dkakunsi.bitapp.Id;
import io.dkakunsi.bitapp.Result;
import io.dkakunsi.bitapp.Session.SessionManager;
import io.dkakunsi.bitapp.UseCase;
import io.dkakunsi.bitapp.transaction.application.dto.TransactionResult;
import io.dkakunsi.bitapp.transaction.application.port.TransactionAccountPort;
import io.dkakunsi.bitapp.transaction.application.port.TransactionLoanPort;
import io.dkakunsi.bitapp.transaction.domain.entity.Transaction;
import io.dkakunsi.bitapp.transaction.domain.repository.TransactionRepository;

public final class RemoveTransaction implements UseCase<String, TransactionResult> {

  private final TransactionRepository transactionRepository;
  private final TransactionAccountPort transactionAccountPort;
  private final TransactionLoanPort transactionLoanPort;

  private final SessionManager sessionManager;

  public RemoveTransaction(
      TransactionRepository transactionRepository,
      TransactionAccountPort transactionAccountPort,
      TransactionLoanPort transactionLoanPort,
      SessionManager sessionManager) {
    this.transactionRepository = transactionRepository;
    this.transactionAccountPort = transactionAccountPort;
    this.transactionLoanPort = transactionLoanPort;
    this.sessionManager = sessionManager;
  }

  @Override
  public Result<TransactionResult> execute(String transactionId) {
    return transactionRepository.findById(Id.of(transactionId))
        .filter(transaction -> transaction.user().value().equals(getRequester()))
        .map(transaction -> sessionManager.executeInSession(() -> removeTransaction(transaction)))
        .orElse(Result.notFound("Transaction not found"));
  }

  private Result<TransactionResult> removeTransaction(Transaction transaction) {
    if (transaction.source() != null) {
      transactionAccountPort.creditBalance(transaction.source(), transaction.amount());
    }

    if (transaction.destination() != null) {
      transactionAccountPort.debitBalance(transaction.destination(), transaction.amount());
    }

    if (transaction.loan() != null) {
      transactionLoanPort.increaseRemainingAmount(transaction.loan(), transaction.amount());
    }

    transactionRepository.deleteById(transaction.id());
    return Result.success(TransactionResult.from(transaction));
  }
}
