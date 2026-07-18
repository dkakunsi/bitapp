package io.dkakunsi.bitapp.chat.infrastructure.account;

import java.util.List;

import io.dkakunsi.bitapp.Id;
import io.dkakunsi.bitapp.Result;
import io.dkakunsi.bitapp.account.application.dto.CreateAccountInput;
import io.dkakunsi.bitapp.account.application.usecase.CreateAccount;
import io.dkakunsi.bitapp.account.domain.repository.AccountRepository;
import io.dkakunsi.bitapp.chat.application.port.AccountPort;
import io.dkakunsi.bitapp.chat.domain.entity.Draft;

public class AccountAdapter implements AccountPort {

  private final AccountRepository accountRepository;

  private final CreateAccount createAccountUseCase;

  public AccountAdapter(AccountRepository accountRepository, CreateAccount createAccountUseCase) {
    this.accountRepository = accountRepository;
    this.createAccountUseCase = createAccountUseCase;
  }

  @Override
  public List<ChatAccount> getUserAccounts(Id userId) {
    return accountRepository.findByUserId(userId).stream()
        .map(account -> new ChatAccount(account.id(), account.name()))
        .toList();
  }

  @Override
  public Result<Void> createAccount(Draft draft) {
    var input = toCreateAccountInput(draft);
    var result = createAccountUseCase.execute(input);
    return result.isSuccess() ? Result.success() : Result.failure(result);
  }

  private CreateAccountInput toCreateAccountInput(Draft draft) {
    var jsonData = draft.data();
    var name = jsonData.optString("name");
    var type = jsonData.optString("type");
    var themeColor = jsonData.optString("themeColor", null);

    return CreateAccountInput.builder()
        .name(name)
        .type(type)
        .themeColor(themeColor)
        .build();
  }
}
