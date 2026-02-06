package io.dkakunsi.bitapp.account.repository;

import java.math.BigDecimal;
import java.util.List;

import dev.morphia.Datastore;
import dev.morphia.UpdateOptions;
import dev.morphia.query.filters.Filters;
import dev.morphia.query.updates.UpdateOperators;
import io.dkakunsi.bitapp.account.entity.Account;
import io.dkakunsi.bitapp.account.model.AccountModel;
import io.dkakunsi.bitapp.domain.entity.Id;
import io.dkakunsi.bitapp.mongo.MongoRepository;

public class MongoAccountRepository extends MongoRepository<AccountModel, Account> implements AccountRepository {

  public MongoAccountRepository(Datastore datastore) {
    super(datastore);
  }

  @Override
  protected AccountModel fromEntity(Account entity) {
    return AccountModel.fromAccount(entity);
  }

  @Override
  protected Account toEntity(AccountModel model) {
    return model.toAccount();
  }

  @Override
  public void debitBalance(Id id, BigDecimal amount) {
    pickDatastore().find(AccountModel.class)
        .filter(Filters.eq(MONGO_ID, id.value()))
        .update(new UpdateOptions(), UpdateOperators.dec("balance", amount.doubleValue()));
  }

  @Override
  public void creditBalance(Id id, BigDecimal amount) {
    pickDatastore().find(AccountModel.class)
        .filter(Filters.eq(MONGO_ID, id.value()))
        .update(new UpdateOptions(), UpdateOperators.inc("balance", amount.doubleValue()));
  }

  @Override
  public List<Account> findByUserId(Id userId) {
    return pickDatastore().find(AccountModel.class)
        .filter(Filters.eq("userId", userId.value()))
        .stream()
        .map(AccountModel::toAccount)
        .toList();
  }
}
