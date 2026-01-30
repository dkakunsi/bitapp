package io.dkakunsi.bitapp.account.repository;

import java.util.List;
import java.util.Optional;

import dev.morphia.Datastore;
import dev.morphia.query.filters.Filters;
import io.dkakunsi.bitapp.account.entity.Account;
import io.dkakunsi.bitapp.account.model.AccountModel;
import io.dkakunsi.bitapp.mongo.MongoRepository;

public class MongoAccountRepository extends MongoRepository implements AccountRepository {

  public MongoAccountRepository(Datastore datastore) {
    super(datastore);
  }

  @Override
  public Account create(Account account) {
    var entity = AccountModel.fromAccount(account);
    pickDatastore().save(entity);
    return account;
  }

  @Override
  public Account update(Account account) {
    var entity = AccountModel.fromAccount(account);
    var updatedEntity = pickDatastore().save(entity);
    return updatedEntity.toAccount();
  }

  @Override
  public void deleteById(String id) {
    pickDatastore().find(AccountModel.class)
        .filter(Filters.eq("_id", id))
        .delete();
  }

  @Override
  public Optional<Account> findById(String id) {
    var entity = datastore.find(AccountModel.class)
        .filter(Filters.eq("_id", id))
        .first();
    return entity != null ? Optional.of(entity.toAccount()) : Optional.empty();
  }

  @Override
  public List<Account> findByUserId(String userId) {
    return datastore.find(AccountModel.class)
        .filter(Filters.eq("userId", userId))
        .stream()
        .map(AccountModel::toAccount)
        .toList();
  }
}
