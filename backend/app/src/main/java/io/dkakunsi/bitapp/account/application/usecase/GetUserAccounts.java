package io.dkakunsi.bitapp.account.application.usecase;

import java.util.List;

import io.dkakunsi.bitapp.Id;
import io.dkakunsi.bitapp.Result;
import io.dkakunsi.bitapp.UseCase;
import io.dkakunsi.bitapp.account.application.dto.AccountResult;
import io.dkakunsi.bitapp.account.domain.entity.Account;
import io.dkakunsi.bitapp.account.domain.repository.AccountRepository;

public final class GetUserAccounts implements UseCase<String, List<AccountResult>> {

  private final AccountRepository accountRepository;

  public GetUserAccounts(AccountRepository accountRepository) {
    this.accountRepository = accountRepository;
  }

  @Override
  public Result<List<AccountResult>> execute(String userId) {
    var accounts = accountRepository.findByUserId(Id.of(userId));
    var results = accounts.stream()
        .map(Account::toResult)
        .toList();
    return Result.success(results);
  }
}
