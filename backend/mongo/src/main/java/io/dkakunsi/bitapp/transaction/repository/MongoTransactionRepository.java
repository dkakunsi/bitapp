package io.dkakunsi.bitapp.transaction.repository;

import java.util.List;

import dev.morphia.Datastore;
import dev.morphia.query.filters.Filters;
import io.dkakunsi.bitapp.transaction.entity.Transaction;
import io.dkakunsi.bitapp.transaction.model.TransactionModel;

public class MongoTransactionRepository implements TransactionRepository {

  private final Datastore datastore;

  public MongoTransactionRepository(Datastore datastore) {
    this.datastore = datastore;
  }

  @Override
  public Transaction create(Transaction transaction) {
    var entity = TransactionModel.fromTransaction(transaction);
    datastore.save(entity);
    return transaction;
  }

  @Override
  public List<Transaction> findByUserId(String userId) {
    return datastore.find(TransactionModel.class)
        .filter(Filters.eq("userId", userId))
        .stream()
        .map(TransactionModel::toTransaction)
        .toList();
  }
}
