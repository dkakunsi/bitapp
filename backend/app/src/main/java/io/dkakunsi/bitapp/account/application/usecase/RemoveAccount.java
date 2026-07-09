package io.dkakunsi.bitapp.account.application.usecase;

import io.dkakunsi.bitapp.AppError.Code;
import io.dkakunsi.bitapp.Id;
import io.dkakunsi.bitapp.Result;
import io.dkakunsi.bitapp.SessionManager;
import io.dkakunsi.bitapp.UseCase;
import io.dkakunsi.bitapp.account.application.dto.AccountResult;
import io.dkakunsi.bitapp.account.application.port.AccountLoanPort;
import io.dkakunsi.bitapp.account.application.port.AccountTransactionPort;
import io.dkakunsi.bitapp.account.domain.entity.Account;
import io.dkakunsi.bitapp.account.domain.repository.AccountRepository;

public final class RemoveAccount implements UseCase<String, AccountResult> {

  private final AccountRepository accountRepository;
  private final AccountTransactionPort accountTransactionAdapter;
  private final AccountLoanPort accountLoanAdapter;
  private final SessionManager sessionManager;

  public RemoveAccount(
      AccountRepository accountRepository,
      AccountTransactionPort accountTransactionAdapter,
      AccountLoanPort accountLoanAdapter,
      SessionManager sessionManager) {
    this.accountRepository = accountRepository;
    this.accountTransactionAdapter = accountTransactionAdapter;
    this.accountLoanAdapter = accountLoanAdapter;
    this.sessionManager = sessionManager;
  }

  @Override
  public Result<AccountResult> execute(String accountId) {
    return accountRepository.findById(Id.of(accountId))
        .map(account -> onAccount(account))
        .orElse(Result.failure(Code.NOT_FOUND, "Account not found"));
  }

  private Result<AccountResult> onAccount(Account account) {
    var requester = getRequester();
    if (!account.isOwner(requester)) {
      return Result.failure(Code.FORBIDDEN, "You are not authorized to delete this account");
    }

    return sessionManager.executeInSession(() -> {
      accountLoanAdapter.removeByAccountId(account.id());

      // this must be done after removing loans to maintain data integrity
      // as we are updating transactions related to loans
      accountTransactionAdapter.removeOrUpdateByAccountId(account.id());

      accountRepository.deleteById(account.id());
      return Result.success(AccountResult.from(account));
    });
  }
}
