package io.dkakunsi.bitapp.account.usecase;

import io.dkakunsi.bitapp.account.dto.AccountResult;
import io.dkakunsi.bitapp.account.entity.Account;
import io.dkakunsi.bitapp.account.repository.AccountRepository;
import io.dkakunsi.bitapp.common.AppError.Code;
import io.dkakunsi.bitapp.common.Context;
import io.dkakunsi.bitapp.common.Logger;
import io.dkakunsi.bitapp.common.SystemLogger;
import io.dkakunsi.bitapp.database.SessionManager;
import io.dkakunsi.bitapp.domain.entity.Id;
import io.dkakunsi.bitapp.domain.usecase.Result;
import io.dkakunsi.bitapp.domain.usecase.UseCase;
import io.dkakunsi.bitapp.loan.repository.LoanRepository;
import io.dkakunsi.bitapp.loan.usecase.RemoveLoan;
import io.dkakunsi.bitapp.transaction.repository.TransactionRepository;

public final class RemoveAccount implements UseCase<String, AccountResult> {

  private static final Logger LOGGER = SystemLogger.getLogger(RemoveAccount.class);

  private final AccountRepository accountRepository;
  private final TransactionRepository transactionRepository;
  private final LoanRepository loanRepository;
  private final RemoveLoan removeLoan;
  private final SessionManager sessionManager;

  public RemoveAccount(
      AccountRepository accountRepository,
      TransactionRepository transactionRepository,
      LoanRepository loanRepository,
      RemoveLoan removeLoan,
      SessionManager sessionManager) {
    this.accountRepository = accountRepository;
    this.transactionRepository = transactionRepository;
    this.loanRepository = loanRepository;
    this.removeLoan = removeLoan;
    this.sessionManager = sessionManager;
  }

  @Override
  public Result<AccountResult> execute(Context context, String accountId) {
    return accountRepository.findById(Id.of(accountId))
        .map(account -> onAccount(context, account))
        .orElse(Result.failure(Code.NOT_FOUND, "Account not found"));
  }

  private Result<AccountResult> onAccount(Context context, Account account) {
    if (!account.isOwner(context.requester())) {
      return Result.failure(Code.FORBIDDEN, "You are not authorized to delete this account");
    }

    return sessionManager.executeInSession(() -> {
      loanRepository.findByAccountId(account.id()).forEach(loan -> {
        var result = removeLoan.execute(context, loan.id().value());
        if (result.isFailed()) {
          LOGGER.warn("Failed to remove loan with id {}: {}", loan.id(), result.error().get().message());
        }
      });

      // this must be done after removing loans to maintain data integrity
      // as we are updating transactions related to loans
      transactionRepository.findByAccountId(account.id()).forEach(t -> {
        switch (t.type()) {
          case DEBIT, CREDIT -> transactionRepository.deleteById(t.id());
          case TRANSFER -> transactionRepository.update(t.convertFromTransfer(account.id(), context.requester()));
        }
      });

      accountRepository.deleteById(account.id());
      return Result.success(account.toResult());
    });
  }
}
