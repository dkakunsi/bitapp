package io.dkakunsi.bitapp.account.repository;

import java.util.List;

import io.dkakunsi.bitapp.account.model.Account;

public interface AccountRepository {
  Account create(Account account);

  List<Account> findByUserId(String userId);
}
