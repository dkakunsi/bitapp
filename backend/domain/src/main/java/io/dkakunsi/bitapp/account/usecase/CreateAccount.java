package io.dkakunsi.bitapp.account.usecase;

import io.dkakunsi.bitapp.account.dto.CreateAccountInput;
import io.dkakunsi.bitapp.account.dto.CreateAccountOutput;
import io.dkakunsi.bitapp.account.repository.AccountRepository;
import io.dkakunsi.bitapp.common.AppError.Code;
import io.dkakunsi.bitapp.common.usecase.Input;
import io.dkakunsi.bitapp.common.usecase.Result;
import io.dkakunsi.bitapp.common.usecase.UseCase;

public final class CreateAccount implements UseCase<CreateAccountInput, CreateAccountOutput> {

  private final AccountRepository accountRepository;

  public CreateAccount(AccountRepository accountRepository) {
    this.accountRepository = accountRepository;
  }

  @Override
  public Result<CreateAccountOutput> process(Input<CreateAccountInput> input) {
    final var account = input.data().toAccount(input.context().requester());
    try {
      var result = this.accountRepository.create(account);
      return Result.success(CreateAccountOutput.from(result));
    } catch (Exception e) {
      return Result.failure(Code.SERVER_ERROR, e.getMessage());
    }
  }
}
