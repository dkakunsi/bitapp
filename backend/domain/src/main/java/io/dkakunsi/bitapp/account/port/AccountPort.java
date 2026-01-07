package io.dkakunsi.money.account.port;

import io.dkakunsi.money.account.model.Account;

public interface AccountPort {
  Account create(Account account);
}
