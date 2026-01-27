package io.dkakunsi.bitapp.account.repository;

import java.util.List;
import java.util.Optional;

import dev.morphia.Datastore;
import dev.morphia.query.filters.Filters;
import dev.morphia.transactions.MorphiaSession;
import io.dkakunsi.bitapp.account.entity.Account;
import io.dkakunsi.bitapp.account.model.AccountModel;
import io.dkakunsi.bitapp.database.Session;
import io.dkakunsi.bitapp.mongo.MongoSession;

public class MongoAccountRepository implements AccountRepository {

  private final Datastore datastore;

  public MongoAccountRepository(Datastore datastore) {
    this.datastore = datastore;
  }

  @Override
  public Account create(Account account) {
    var entity = AccountModel.fromAccount(account);
    datastore.save(entity);
    return account;
  }

  @Override
  public Account update(Account account) {
    var entity = AccountModel.fromAccount(account);
    var updatedEntity = datastore.save(entity);
    return updatedEntity.toAccount();
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

  @Override
  public void deleteById(String id) {
    deleteById(datastore, id);
  }

  @Override
  public void deleteById(Session session, String id) {
    MorphiaSession morphiaSession = ((MongoSession) session).getSession();
    deleteById(morphiaSession, id);
  }

  private void deleteById(Datastore ds, String id) {
    ds.find(AccountModel.class)
        .filter(Filters.eq("_id", id))
        .delete();
  }
}
