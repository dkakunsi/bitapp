package io.dkakunsi.bitapp.account.usecase;

import io.dkakunsi.bitapp.account.dto.AccountResult;
import io.dkakunsi.bitapp.account.dto.UpdateAccountInput;
import io.dkakunsi.bitapp.account.entity.Account;
import io.dkakunsi.bitapp.account.repository.AccountRepository;
import io.dkakunsi.bitapp.common.AppError.Code;
import io.dkakunsi.bitapp.common.Context;
import io.dkakunsi.bitapp.common.usecase.Result;
import io.dkakunsi.bitapp.common.usecase.UseCase;

public final class UpdateAccount implements UseCase<UpdateAccountInput, AccountResult> {

  private final AccountRepository accountRepository;

  public UpdateAccount(AccountRepository accountRepository) {
    this.accountRepository = accountRepository;
  }

  @Override
  public Result<AccountResult> execute(Context context, UpdateAccountInput input) {
    return accountRepository.findById(input.id())
        .map(account -> onAccount(account, input, context.requester()))
        .orElse(Result.failure(Code.NOT_FOUND, "Account not found"));
  }

  private Result<AccountResult> onAccount(Account account, UpdateAccountInput input, String requester) {
    if (!account.isOwner(requester)) {
      return Result.failure(Code.UNAUTHORIZED, "User can only update their own account");
    }

    var updatedAccount = accountRepository.update(account.updateDetails(input, requester));
    return Result.success(updatedAccount.toResult());
  }
}
