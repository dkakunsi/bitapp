package io.dkakunsi.bitapp.account.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import io.dkakunsi.bitapp.account.entity.Account;
import io.dkakunsi.bitapp.domain.entity.Id;

public interface AccountRepository {
  Account create(Account account);

  Account update(Account account);

  void debitBalance(Id id, BigDecimal amount);

  void creditBalance(Id id, BigDecimal amount);

  void deleteById(Id id);

  Optional<Account> findById(Id id);

  List<Account> findByUserId(Id userId);
}
