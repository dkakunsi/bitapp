package io.dkakunsi.bitapp.account.application.usecase;

import io.dkakunsi.bitapp.AppError.Code;
import io.dkakunsi.bitapp.Id;
import io.dkakunsi.bitapp.Result;
import io.dkakunsi.bitapp.UseCase;
import io.dkakunsi.bitapp.account.application.dto.AccountResult;
import io.dkakunsi.bitapp.account.application.dto.UpdateAccountInput;
import io.dkakunsi.bitapp.account.domain.entity.Account;
import io.dkakunsi.bitapp.account.domain.repository.AccountRepository;

public final class UpdateAccount implements UseCase<UpdateAccountInput, AccountResult> {

  private final AccountRepository accountRepository;

  public UpdateAccount(AccountRepository accountRepository) {
    this.accountRepository = accountRepository;
  }

  @Override
  public Result<AccountResult> execute(UpdateAccountInput input) {
    return accountRepository.findById(Id.of(input.id()))
        .map(account -> onAccount(account, input))
        .orElse(Result.failure(Code.NOT_FOUND, "Account not found"));
  }

  private Result<AccountResult> onAccount(Account account, UpdateAccountInput input) {
    var requester = getRequester();
    if (!account.isOwner(requester)) {
      return Result.failure(Code.UNAUTHORIZED, "User can only update their own account");
    }

    var updatedAccount = accountRepository.update(account.updateDetails(input, requester));
    return Result.success(updatedAccount.toResult());
  }
}
