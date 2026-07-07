package io.dkakunsi.bitapp.account.application.usecase;

import io.dkakunsi.bitapp.Id;
import io.dkakunsi.bitapp.Result;
import io.dkakunsi.bitapp.UseCase;
import io.dkakunsi.bitapp.account.application.dto.UpdateBalanceInput;
import io.dkakunsi.bitapp.account.domain.repository.AccountRepository;

public class UpdateBalance implements UseCase<UpdateBalanceInput, Void> {

  private final AccountRepository accountRepository;

  public UpdateBalance(AccountRepository accountRepository) {
    this.accountRepository = accountRepository;
  }

  @Override
  public Result<Void> execute(UpdateBalanceInput input) {
    var id = Id.of(input.accountId());
    var balance = input.balance();

    if (input.isCredit()) {
      accountRepository.creditBalance(id, balance);
    } else {
      accountRepository.debitBalance(id, balance);
    }

    return Result.success();
  }
}
