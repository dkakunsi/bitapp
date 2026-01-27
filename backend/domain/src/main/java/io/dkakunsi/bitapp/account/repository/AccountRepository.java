package io.dkakunsi.bitapp.account.repository;

import java.util.List;
import java.util.Optional;

import io.dkakunsi.bitapp.account.entity.Account;
import io.dkakunsi.bitapp.database.Session;

public interface AccountRepository {
  Account create(Account account);

  Account update(Account account);

  Optional<Account> findById(String id);

  List<Account> findByUserId(String userId);

  void deleteById(String id);

  void deleteById(Session session, String id);
}
