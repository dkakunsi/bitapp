package io.dkakunsi.bitapp.account.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import dev.morphia.Datastore;
import dev.morphia.UpdateOptions;
import dev.morphia.query.filters.Filters;
import dev.morphia.query.updates.UpdateOperators;
import io.dkakunsi.bitapp.account.entity.Account;
import io.dkakunsi.bitapp.account.model.AccountModel;
import io.dkakunsi.bitapp.domain.entity.Id;
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
  public void debitBalance(Id id, BigDecimal amount) {
    pickDatastore().find(AccountModel.class)
        .filter(Filters.eq(MONGO_ID, id.value()))
        .update(new UpdateOptions(), UpdateOperators.dec("balance", amount));
  }

  @Override
  public void creditBalance(Id id, BigDecimal amount) {
    pickDatastore().find(AccountModel.class)
        .filter(Filters.eq(MONGO_ID, id.value()))
        .update(new UpdateOptions(), UpdateOperators.inc("balance", amount));
  }

  @Override
  public void deleteById(Id id) {
    pickDatastore().find(AccountModel.class)
        .filter(Filters.eq(MONGO_ID, id.value()))
        .delete();
  }

  @Override
  public Optional<Account> findById(Id id) {
    var entity = datastore.find(AccountModel.class)
        .filter(Filters.eq(MONGO_ID, id.value()))
        .first();
    return entity != null ? Optional.of(entity.toAccount()) : Optional.empty();
  }

  @Override
  public List<Account> findByUserId(Id userId) {
    return datastore.find(AccountModel.class)
        .filter(Filters.eq("userId", userId.value()))
        .stream()
        .map(AccountModel::toAccount)
        .toList();
  }
}
