package io.dkakunsi.money.account.process;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.dkakunsi.lab.common.Id;
import io.dkakunsi.lab.common.process.Process;
import io.dkakunsi.lab.common.process.ProcessError;
import io.dkakunsi.lab.common.process.ProcessInput;
import io.dkakunsi.lab.common.process.ProcessResult;
import io.dkakunsi.money.account.model.Account;
import io.dkakunsi.money.account.port.AccountPort;
import io.dkakunsi.money.user.model.User;

public final class CreateAccountProcess implements Process<CreateAccountInput, Account> {

  private final AccountPort accountRepository;

  public CreateAccountProcess(AccountPort accountRepository) {
    this.accountRepository = accountRepository;
  }

  @Override
  public Result<Account> process(ProcessInput<CreateAccountInput> input) {
    final var account = toModel(input.data(), input.requester());
    try {
      var result = this.accountRepository.create(account);
      return Result.success(result);
    } catch (Exception e) {
      return Result.failure(Error.Code.SERVER_ERROR, e.getMessage());
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
