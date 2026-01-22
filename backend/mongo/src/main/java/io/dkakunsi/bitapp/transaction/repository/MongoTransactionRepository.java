package io.dkakunsi.bitapp.transaction.repository;

import dev.morphia.Datastore;
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
}
