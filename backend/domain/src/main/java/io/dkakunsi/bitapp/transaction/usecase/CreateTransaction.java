package io.dkakunsi.bitapp.transaction.usecase;

import java.util.Optional;

import io.dkakunsi.bitapp.account.repository.AccountRepository;
import io.dkakunsi.bitapp.common.AppError;
import io.dkakunsi.bitapp.common.AppError.Code;
import io.dkakunsi.bitapp.common.Context;
import io.dkakunsi.bitapp.database.SessionManager;
import io.dkakunsi.bitapp.domain.usecase.Result;
import io.dkakunsi.bitapp.domain.usecase.UseCase;
import io.dkakunsi.bitapp.loan.repository.LoanRepository;
import io.dkakunsi.bitapp.transaction.dto.CreateTransactionInput;
import io.dkakunsi.bitapp.transaction.dto.TransactionResult;
import io.dkakunsi.bitapp.transaction.entity.Transaction;
import io.dkakunsi.bitapp.transaction.processor.TransactionProcessor;
import io.dkakunsi.bitapp.transaction.repository.TransactionRepository;

public final class CreateTransaction implements UseCase<CreateTransactionInput, TransactionResult> {

  private final TransactionRepository transactionRepository;
  private final AccountRepository accountRepository;
  private final LoanRepository loanRepository;

  private final SessionManager sessionManager;

  public CreateTransaction(
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
  public Result<TransactionResult> execute(Context context, CreateTransactionInput input) {
    var transaction = input.toTransaction(context.requester());
    return validateRequest(transaction)
        .map(error -> Result.<TransactionResult>failure(error))
        .orElseGet(() -> sessionManager.executeInSession(() -> execute(input, transaction)));
  }

  private Result<TransactionResult> execute(CreateTransactionInput input, Transaction transaction) {
    var transactionProcessor = TransactionProcessor.getTransactionProcessor(input.getClass(),
        transactionRepository, accountRepository, loanRepository);
    var processedTransaction = transactionProcessor.process(transaction);
    return Result.success(processedTransaction.toResult());
  }

  private Optional<AppError> validateRequest(Transaction transaction) {
    if (transaction.source() != null && accountRepository.isNotExistingAccount(transaction.source())) {
      return Optional.of(new AppError(Code.BAD_REQUEST, "source account not found"));
    }

    if (transaction.destination() != null && accountRepository.isNotExistingAccount(transaction.destination())) {
      return Optional.of(new AppError(Code.BAD_REQUEST, "destination account not found"));
    }

    if (transaction.loan() != null && loanRepository.isNotExistingLoan(transaction.loan())) {
      return Optional.of(new AppError(Code.BAD_REQUEST, "loan not found"));
    }
    return Optional.empty();
  }
}
