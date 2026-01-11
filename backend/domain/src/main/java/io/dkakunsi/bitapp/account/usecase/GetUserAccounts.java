package io.dkakunsi.bitapp.account.usecase;

import io.dkakunsi.bitapp.account.dto.GetUserAccountsInput;
import io.dkakunsi.bitapp.account.dto.GetUserAccountsResult;
import io.dkakunsi.bitapp.account.repository.AccountRepository;
import io.dkakunsi.bitapp.common.AppError.Code;
import io.dkakunsi.bitapp.common.Context;
import io.dkakunsi.bitapp.common.usecase.Result;
import io.dkakunsi.bitapp.common.usecase.UseCase;

public final class GetUserAccounts implements UseCase<GetUserAccountsInput, GetUserAccountsResult> {

  private final AccountRepository accountRepository;

  public GetUserAccounts(AccountRepository accountRepository) {
    this.accountRepository = accountRepository;
  }

  @Override
  public Result<GetUserAccountsResult> process(Context context, GetUserAccountsInput input) {
    try {
      var accounts = accountRepository.findByUserId(input.userId());
      return Result.success(GetUserAccountsResult.from(accounts));
    } catch (Exception e) {
      return Result.failure(Code.SERVER_ERROR, e.getMessage());
    }
  }
}
