package io.dkakunsi.bitapp.account.process;

import io.dkakunsi.bitapp.account.dto.CreateAccountInput;
import io.dkakunsi.bitapp.account.model.Account;
import io.dkakunsi.bitapp.account.repository.AccountRepository;
import io.dkakunsi.bitapp.common.Input;
import io.dkakunsi.bitapp.common.Result;
import io.dkakunsi.bitapp.common.AppError.Code;

public final class CreateAccount {

  private final AccountRepository accountRepository;

  public CreateAccount(AccountRepository accountRepository) {
    this.accountRepository = accountRepository;
  }

  public Result<Account> process(Input<CreateAccountInput> input) {
    final var account = Account.from(input.data(), input.context().requester());
    try {
      var result = this.accountRepository.create(account);
      return Result.success(result);
    } catch (Exception e) {
      return Result.failure(Code.SERVER_ERROR, e.getMessage());
    }
  }
}
