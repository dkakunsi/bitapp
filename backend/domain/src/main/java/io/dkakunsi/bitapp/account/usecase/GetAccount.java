package io.dkakunsi.bitapp.account.usecase;

import io.dkakunsi.bitapp.account.dto.AccountResult;
import io.dkakunsi.bitapp.account.repository.AccountRepository;
import io.dkakunsi.bitapp.common.AppError.Code;
import io.dkakunsi.bitapp.domain.entity.Id;
import io.dkakunsi.bitapp.domain.usecase.Result;
import io.dkakunsi.bitapp.domain.usecase.UseCase;

public final class GetAccount implements UseCase<String, AccountResult> {

  private final AccountRepository accountRepository;

  public GetAccount(AccountRepository accountRepository) {
    this.accountRepository = accountRepository;
  }

  @Override
  public Result<AccountResult> execute(String accountId) {
    return accountRepository.findById(Id.of(accountId))
        .map(account -> Result.success(account.toResult()))
        .orElse(Result.failure(Code.NOT_FOUND, "Account not found"));
  }
}
