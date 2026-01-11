package io.dkakunsi.bitapp.mongo.repository;

import java.util.List;

import dev.morphia.Datastore;
import dev.morphia.query.filters.Filters;
import io.dkakunsi.bitapp.account.model.Account;
import io.dkakunsi.bitapp.account.repository.AccountRepository;
import io.dkakunsi.bitapp.mongo.entity.AccountEntity;

public class MongoAccountRepository implements AccountRepository {

  private final Datastore datastore;

  public MongoAccountRepository(Datastore datastore) {
    this.datastore = datastore;
  }

  @Override
  public Account create(Account account) {
    var entity = AccountEntity.fromAccount(account);
    datastore.save(entity);
    return account;
  }

  @Override
  public List<Account> findByUserId(String userId) {
    return datastore.find(AccountEntity.class)
        .filter(Filters.eq("userId", userId))
        .stream()
        .map(AccountEntity::toAccount)
        .toList();
  }
}
