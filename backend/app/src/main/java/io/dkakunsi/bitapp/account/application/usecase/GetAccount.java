package io.dkakunsi.bitapp.account.application.usecase;

import io.dkakunsi.bitapp.AppError.Code;
import io.dkakunsi.bitapp.Id;
import io.dkakunsi.bitapp.Result;
import io.dkakunsi.bitapp.UseCase;
import io.dkakunsi.bitapp.account.application.dto.AccountResult;
import io.dkakunsi.bitapp.account.domain.repository.AccountRepository;

public final class GetAccount implements UseCase<String, AccountResult> {

  private final AccountRepository accountRepository;

  public GetAccount(AccountRepository accountRepository) {
    this.accountRepository = accountRepository;
  }

  @Override
  public Result<AccountResult> execute(String accountId) {
    return accountRepository.findById(Id.of(accountId))
        .map(account -> Result.success(AccountResult.from(account)))
        .orElse(Result.failure(Code.NOT_FOUND, "Account not found"));
  }
}
