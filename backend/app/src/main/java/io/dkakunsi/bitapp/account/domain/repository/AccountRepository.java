package io.dkakunsi.bitapp.account.domain.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import io.dkakunsi.bitapp.Id;
import io.dkakunsi.bitapp.account.domain.entity.Account;

public interface AccountRepository {

  Account create(Account account);

  Account update(Account account);

  void debitBalance(Id id, BigDecimal amount);

  void creditBalance(Id id, BigDecimal amount);

  void deleteById(Id id);

  Optional<Account> findById(Id id);

  List<Account> findByUserId(Id userId);

  default boolean isExistingAccount(Id id) {
    return findById(id).isPresent();
  }

  default boolean isNotExistingAccount(Id id) {
    return !isExistingAccount(id);
  }
}
