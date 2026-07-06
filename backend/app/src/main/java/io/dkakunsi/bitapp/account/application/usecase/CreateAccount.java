package io.dkakunsi.bitapp.account.application.usecase;

import io.dkakunsi.bitapp.Result;
import io.dkakunsi.bitapp.UseCase;
import io.dkakunsi.bitapp.account.application.dto.AccountResult;
import io.dkakunsi.bitapp.account.application.dto.CreateAccountInput;
import io.dkakunsi.bitapp.account.domain.entity.Account;
import io.dkakunsi.bitapp.account.domain.repository.AccountRepository;

public final class CreateAccount implements UseCase<CreateAccountInput, AccountResult> {

  private final AccountRepository accountRepository;

  public CreateAccount(AccountRepository accountRepository) {
    this.accountRepository = accountRepository;
  }

  @Override
  public Result<AccountResult> execute(CreateAccountInput input) {
    final var account = Account.from(input, getRequester());
    var createdAccount = this.accountRepository.create(account);
    return Result.success(createdAccount.toResult());
  }
}
