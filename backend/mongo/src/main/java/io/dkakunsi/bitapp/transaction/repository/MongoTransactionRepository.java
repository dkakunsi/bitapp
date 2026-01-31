package io.dkakunsi.bitapp.transaction.repository;

import java.util.List;
import java.util.Optional;

import dev.morphia.Datastore;
import dev.morphia.query.filters.Filters;
import io.dkakunsi.bitapp.domain.entity.Id;
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
  public void deleteById(Id id) {
    pickDatastore().find(TransactionModel.class)
        .filter(Filters.eq(MONGO_ID, id.value()))
        .delete();
  }

  @Override
  public Optional<Transaction> findById(Id id) {
    var entity = datastore.find(TransactionModel.class)
        .filter(Filters.eq(MONGO_ID, id.value()))
        .first();
    return Optional.ofNullable(entity).map(TransactionModel::toTransaction);
  }

  @Override
  public List<Transaction> findByUserId(Id userId) {
    return datastore.find(TransactionModel.class)
        .filter(Filters.eq("userId", userId.value()))
        .stream()
        .map(TransactionModel::toTransaction)
        .toList();
  }

  @Override
  public List<Transaction> findByAccountId(Id accountId) {
    return datastore.find(TransactionModel.class)
        .filter(Filters.or(
            Filters.eq("source", accountId.value()),
            Filters.eq("destination", accountId.value())))
        .stream()
        .map(TransactionModel::toTransaction)
        .toList();
  }

  @Override
  public List<Transaction> findByLoanId(Id loanId) {
    return datastore.find(TransactionModel.class)
        .filter(Filters.eq("loan", loanId.value()))
        .stream()
        .map(TransactionModel::toTransaction)
        .toList();
  }
}
