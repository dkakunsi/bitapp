package io.dkakunsi.bitapp.account.usecase;

import io.dkakunsi.bitapp.account.dto.UpdateAccountInput;
import io.dkakunsi.bitapp.account.dto.UpdateAccountResult;
import io.dkakunsi.bitapp.account.model.Account;
import io.dkakunsi.bitapp.account.repository.AccountRepository;
import io.dkakunsi.bitapp.common.AppError.Code;
import io.dkakunsi.bitapp.common.Context;
import io.dkakunsi.bitapp.common.usecase.Result;
import io.dkakunsi.bitapp.common.usecase.UseCase;

public final class UpdateAccount implements UseCase<UpdateAccountInput, UpdateAccountResult> {

  private final AccountRepository accountRepository;

  public UpdateAccount(AccountRepository accountRepository) {
    this.accountRepository = accountRepository;
  }

  @Override
  public Result<UpdateAccountResult> process(Context context, UpdateAccountInput input) {
    try {
      return accountRepository.findById(input.id())
          .map(account -> onAccount(account, input, context.requester()))
          .orElse(Result.failure(Code.NOT_FOUND, "Account not found"));
    } catch (Exception e) {
      return Result.failure(Code.SERVER_ERROR, e.getMessage());
    }
  }

  private Result<UpdateAccountResult> onAccount(Account account, UpdateAccountInput input, String requester) {
    if (!account.isOwner(requester)) {
      return Result.failure(Code.BAD_REQUEST, "User can only update their own account");
    }

    var updatedAccount = accountRepository.update(account.updateDetails(input, requester));
    return Result.success(UpdateAccountResult.from(updatedAccount));
  }
}
