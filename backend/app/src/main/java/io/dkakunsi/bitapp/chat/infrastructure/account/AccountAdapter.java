package io.dkakunsi.bitapp.chat.infrastructure.account;

import java.util.List;

import io.dkakunsi.bitapp.Id;
import io.dkakunsi.bitapp.account.domain.repository.AccountRepository;
import io.dkakunsi.bitapp.chat.application.port.AccountPort;

public class AccountAdapter implements AccountPort {

  private final AccountRepository accountRepository;

  public AccountAdapter(AccountRepository accountRepository) {
    this.accountRepository = accountRepository;
  }

  @Override
  public List<ChatAccount> getUserAccounts(Id userId) {
    return accountRepository.findByUserId(userId).stream()
        .map(account -> new ChatAccount(account.id(), account.name()))
        .toList();
  }

}
