package io.dkakunsi.bitapp.mongo.repository;

import dev.morphia.Datastore;
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
}
