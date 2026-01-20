package io.dkakunsi.bitapp.account.usecase;

import io.dkakunsi.bitapp.account.dto.AccountResult;
import io.dkakunsi.bitapp.account.dto.CreateAccountInput;
import io.dkakunsi.bitapp.account.entity.Account;
import io.dkakunsi.bitapp.account.repository.AccountRepository;
import io.dkakunsi.bitapp.common.Context;
import io.dkakunsi.bitapp.common.usecase.Result;
import io.dkakunsi.bitapp.common.usecase.UseCase;

public final class CreateAccount implements UseCase<CreateAccountInput, AccountResult> {

  private final AccountRepository accountRepository;

  public CreateAccount(AccountRepository accountRepository) {
    this.accountRepository = accountRepository;
  }

  @Override
  public Result<AccountResult> execute(Context context, CreateAccountInput input) {
    final var account = Account.from(input, context.requester());
    var createdAccount = this.accountRepository.create(account);
    return Result.success(createdAccount.toResult());
  }
}
