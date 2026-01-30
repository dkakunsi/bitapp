package io.dkakunsi.bitapp.transaction.repository;

import java.util.List;
import java.util.Optional;

import dev.morphia.Datastore;
import dev.morphia.query.filters.Filters;
import io.dkakunsi.bitapp.mongo.MongoRepository;
import io.dkakunsi.bitapp.transaction.entity.Transaction;
import io.dkakunsi.bitapp.transaction.model.TransactionModel;

public class MongoTransactionRepository extends MongoRepository implements TransactionRepository {

  public MongoTransactionRepository(Datastore datastore) {
    super(datastore);
  }

  @Override
  public Transaction create(Transaction transaction) {
    var entity = TransactionModel.fromTransaction(transaction);
    pickDatastore().save(entity);
    return transaction;
  }

  @Override
  public Transaction update(Transaction transaction) {
    var entity = TransactionModel.fromTransaction(transaction);
    pickDatastore().save(entity);
    return transaction;
  }

  @Override
  public void deleteById(String id) {
    pickDatastore().find(TransactionModel.class)
        .filter(Filters.eq("_id", id))
        .delete();
  }

  @Override
  public Optional<Transaction> findById(String id) {
    var entity = datastore.find(TransactionModel.class)
        .filter(Filters.eq("_id", id))
        .first();
    return Optional.ofNullable(entity).map(TransactionModel::toTransaction);
  }

  @Override
  public List<Transaction> findByUserId(String userId) {
    return datastore.find(TransactionModel.class)
        .filter(Filters.eq("userId", userId))
        .stream()
        .map(TransactionModel::toTransaction)
        .toList();
  }

  @Override
  public List<Transaction> findByAccountId(String accountId) {
    return datastore.find(TransactionModel.class)
        .filter(Filters.or(
            Filters.eq("source", accountId),
            Filters.eq("destination", accountId)))
        .stream()
        .map(TransactionModel::toTransaction)
        .toList();
  }

  @Override
  public List<Transaction> findByLoanId(String loanId) {
    return datastore.find(TransactionModel.class)
        .filter(Filters.eq("loan", loanId))
        .stream()
        .map(TransactionModel::toTransaction)
        .toList();
  }
}
