package io.dkakunsi.bitapp.account.repository;

import io.dkakunsi.bitapp.account.model.Account;

public interface AccountRepository {
  Account create(Account account);
}
