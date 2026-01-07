package io.dkakunsi.bitapp.account.process;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.dkakunsi.bitapp.account.dto.CreateAccountInput;
import io.dkakunsi.bitapp.account.model.Account;
import io.dkakunsi.bitapp.account.repository.AccountRepository;
import io.dkakunsi.bitapp.common.Id;
import io.dkakunsi.bitapp.common.Input;
import io.dkakunsi.bitapp.common.Result;
import io.dkakunsi.bitapp.common.AppError.Code;
import io.dkakunsi.bitapp.user.model.User;

public final class CreateAccountProcess {

  private final AccountRepository accountRepository;

  public CreateAccountProcess(AccountRepository accountRepository) {
    this.accountRepository = accountRepository;
  }

  public Result<Account> process(Input<CreateAccountInput> input) {
    final var account = toModel(input.data(), input.context().requester());
    try {
      var result = this.accountRepository.create(account);
      return Result.success(result);
    } catch (Exception e) {
      return Result.failure(Code.SERVER_ERROR, e.getMessage());
    }
  }

  private static Account toModel(CreateAccountInput input, String requester) {
    final var user = User.builder().id(Id.of(requester)).build();
    final var now = LocalDateTime.now();
    final var executor = requester;
    return Account.builder()
        .id(Id.generate())
        .name(input.name())
        .type(input.type())
        .themeColor(input.themeColor())
        .user(user)
        .balance(BigDecimal.ZERO)
        .createdAt(now)
        .updatedAt(now)
        .createdBy(executor)
        .updatedBy(executor)
        .build();
  }
}
