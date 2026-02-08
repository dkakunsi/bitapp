package io.dkakunsi.bitapp.transaction.usecase;

import io.dkakunsi.bitapp.account.repository.AccountRepository;
import io.dkakunsi.bitapp.common.AppError.Code;
import io.dkakunsi.bitapp.database.SessionManager;
import io.dkakunsi.bitapp.domain.entity.Id;
import io.dkakunsi.bitapp.domain.usecase.Result;
import io.dkakunsi.bitapp.domain.usecase.UseCase;
import io.dkakunsi.bitapp.loan.repository.LoanRepository;
import io.dkakunsi.bitapp.transaction.dto.TransactionResult;
import io.dkakunsi.bitapp.transaction.entity.Transaction;
import io.dkakunsi.bitapp.transaction.repository.TransactionRepository;

public final class RemoveTransaction implements UseCase<String, TransactionResult> {

  private final TransactionRepository transactionRepository;
  private final AccountRepository accountRepository;
  private final LoanRepository loanRepository;

  private final SessionManager sessionManager;

  public RemoveTransaction(
      TransactionRepository transactionRepository,
      AccountRepository accountRepository,
      LoanRepository loanRepository,
      SessionManager sessionManager) {
    this.transactionRepository = transactionRepository;
    this.accountRepository = accountRepository;
    this.loanRepository = loanRepository;
    this.sessionManager = sessionManager;
  }

  @Override
  public Result<TransactionResult> execute(String transactionId) {
    return transactionRepository.findById(Id.of(transactionId))
        .filter(transaction -> transaction.user().value().equals(getRequester()))
        .map(transaction -> sessionManager.executeInSession(() -> removeTransaction(transaction)))
        .orElse(Result.failure(Code.NOT_FOUND, "Transaction not found"));
  }

  private Result<TransactionResult> removeTransaction(Transaction transaction) {
    if (transaction.source() != null) {
      accountRepository.creditBalance(transaction.source(), transaction.amount());
    }

    if (transaction.destination() != null) {
      accountRepository.debitBalance(transaction.destination(), transaction.amount());
    }

    if (transaction.loan() != null) {
      loanRepository.increaseRemainingAmount(transaction.loan(), transaction.amount());
    }

    transactionRepository.deleteById(transaction.id());
    return Result.success(transaction.toResult());
  }
}
