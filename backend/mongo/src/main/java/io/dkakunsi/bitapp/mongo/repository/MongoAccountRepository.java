package io.dkakunsi.bitapp.mongo.repository;

import java.util.List;
import java.util.Optional;

import dev.morphia.Datastore;
import dev.morphia.query.filters.Filters;
import io.dkakunsi.bitapp.account.entity.Account;
import io.dkakunsi.bitapp.account.repository.AccountRepository;
import io.dkakunsi.bitapp.mongo.model.AccountEntity;

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
  public Account update(Account account) {
    var entity = AccountEntity.fromAccount(account);
    var updatedEntity = datastore.save(entity);
    return updatedEntity.toAccount();
  }

  @Override
  public Optional<Account> findById(String id) {
    var entity = datastore.find(AccountEntity.class)
        .filter(Filters.eq("_id", id))
        .first();
    return entity != null ? Optional.of(entity.toAccount()) : Optional.empty();
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
