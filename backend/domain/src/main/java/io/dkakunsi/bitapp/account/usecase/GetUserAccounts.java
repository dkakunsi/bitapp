package io.dkakunsi.bitapp.account.usecase;

import java.util.List;

import io.dkakunsi.bitapp.account.dto.GetUserAccountsInput;
import io.dkakunsi.bitapp.account.dto.GetUserAccountsResult;
import io.dkakunsi.bitapp.account.repository.AccountRepository;
import io.dkakunsi.bitapp.common.Context;
import io.dkakunsi.bitapp.common.usecase.Result;
import io.dkakunsi.bitapp.common.usecase.UseCase;

public final class GetUserAccounts implements UseCase<GetUserAccountsInput, List<GetUserAccountsResult>> {

  private final AccountRepository accountRepository;

  public GetUserAccounts(AccountRepository accountRepository) {
    this.accountRepository = accountRepository;
  }

  @Override
  public Result<List<GetUserAccountsResult>> execute(Context context, GetUserAccountsInput input) {
    var accounts = accountRepository.findByUserId(input.userId());
    var results = accounts.stream()
        .map(GetUserAccountsResult::from)
        .toList();
    return Result.success(results);
  }
}
