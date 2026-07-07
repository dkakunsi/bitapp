package io.dkakunsi.bitapp.transaction.application.usecase;

import java.util.Optional;

import io.dkakunsi.bitapp.AppError;
import io.dkakunsi.bitapp.AppError.Code;
import io.dkakunsi.bitapp.Id;
import io.dkakunsi.bitapp.Result;
import io.dkakunsi.bitapp.SessionManager;
import io.dkakunsi.bitapp.UseCase;
import io.dkakunsi.bitapp.transaction.application.dto.CreateTransactionInput;
import io.dkakunsi.bitapp.transaction.application.dto.TransactionResult;
import io.dkakunsi.bitapp.transaction.application.processor.TransactionProcessor;
import io.dkakunsi.bitapp.transaction.domain.entity.Transaction;
import io.dkakunsi.bitapp.transaction.domain.port.TransactionAccountPort;
import io.dkakunsi.bitapp.transaction.domain.port.TransactionLoanPort;
import io.dkakunsi.bitapp.transaction.domain.repository.TransactionRepository;

public final class CreateTransaction implements UseCase<CreateTransactionInput, TransactionResult> {

  private final TransactionRepository transactionRepository;
  private final TransactionAccountPort transactionAccountService;
  private final TransactionLoanPort transactionLoanService;

  private final SessionManager sessionManager;

  public CreateTransaction(
      TransactionRepository transactionRepository,
      TransactionAccountPort transactionAccountService,
      TransactionLoanPort transactionLoanService,
      SessionManager sessionManager) {
    this.transactionRepository = transactionRepository;
    this.transactionAccountService = transactionAccountService;
    this.transactionLoanService = transactionLoanService;
    this.sessionManager = sessionManager;
  }

  @Override
  public Result<TransactionResult> execute(CreateTransactionInput input) {
    var transaction = input.toTransaction(getRequester());
    return validateRequest(transaction)
        .map(error -> Result.<TransactionResult>failure(error))
        .orElseGet(() -> sessionManager.executeInSession(() -> execute(input, transaction)));
  }

  private Result<TransactionResult> execute(CreateTransactionInput input, Transaction transaction) {
    var transactionProcessor = TransactionProcessor.getTransactionProcessor(input.getClass(),
        transactionRepository, transactionAccountService, transactionLoanService);
    var processedTransaction = transactionProcessor.process(transaction);
    return Result.success( TransactionResult.from(processedTransaction));
  }

  private Optional<AppError> validateRequest(Transaction transaction) {
    if (isSameSourceAndDestinationAccount(transaction)) {
      return Optional.of(new AppError(Code.BAD_REQUEST, "source and destination accounts cannot be the same"));
    }

    if (isAccountNotExists(transaction.source())) {
      return Optional.of(new AppError(Code.BAD_REQUEST, "source account not found"));
    }

    if (isAccountNotExists(transaction.destination())) {
      return Optional.of(new AppError(Code.BAD_REQUEST, "destination account not found"));
    }

    if (isLoanNotExists(transaction.loan())) {
      return Optional.of(new AppError(Code.BAD_REQUEST, "loan not found"));
    }
    return Optional.empty();
  }

  private static boolean isSameSourceAndDestinationAccount(Transaction transaction) {
    return transaction.source() != null
        && transaction.destination() != null
        && transaction.source().equals(transaction.destination());
  }

  private boolean isAccountNotExists(Id accountId) {
    return accountId != null && !transactionAccountService.isExistingAccount(accountId);
  }

  private boolean isLoanNotExists(Id loanId) {
    return loanId != null && !transactionLoanService.isExistingLoan(loanId);
  }
}
