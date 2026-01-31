package io.dkakunsi.bitapp.transaction.usecase;

import io.dkakunsi.bitapp.account.repository.AccountRepository;
import io.dkakunsi.bitapp.common.Context;
import io.dkakunsi.bitapp.database.SessionManager;
import io.dkakunsi.bitapp.domain.usecase.Result;
import io.dkakunsi.bitapp.domain.usecase.UseCase;
import io.dkakunsi.bitapp.loan.repository.LoanRepository;
import io.dkakunsi.bitapp.transaction.dto.CreateTransactionInput;
import io.dkakunsi.bitapp.transaction.dto.TransactionResult;
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
    return sessionManager.executeInSession(() -> {
      var transactionProcessor = TransactionProcessor.getTransactionProcessor(input.getClass(),
          transactionRepository, accountRepository, loanRepository);
      var processedTransaction = transactionProcessor.process(transaction);
      return Result.success(processedTransaction.toResult());
    });
  }
}
