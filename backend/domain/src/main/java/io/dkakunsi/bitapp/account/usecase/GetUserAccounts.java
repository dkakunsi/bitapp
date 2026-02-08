package io.dkakunsi.bitapp.account.usecase;

import java.util.List;

import io.dkakunsi.bitapp.account.dto.AccountResult;
import io.dkakunsi.bitapp.account.entity.Account;
import io.dkakunsi.bitapp.account.repository.AccountRepository;
import io.dkakunsi.bitapp.domain.entity.Id;
import io.dkakunsi.bitapp.domain.usecase.Result;
import io.dkakunsi.bitapp.domain.usecase.UseCase;

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
