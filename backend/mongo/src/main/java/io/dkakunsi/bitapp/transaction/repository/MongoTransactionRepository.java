package io.dkakunsi.bitapp.transaction.repository;

import java.util.List;
import java.util.Optional;

import dev.morphia.Datastore;
import dev.morphia.query.filters.Filters;
import dev.morphia.transactions.MorphiaSession;
import io.dkakunsi.bitapp.database.Session;
import io.dkakunsi.bitapp.mongo.MongoSession;
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

  @Override
  public Transaction update(Transaction transaction) {
    return update(datastore, transaction);
  }

  @Override
  public Transaction update(Session session, Transaction transaction) {
    MorphiaSession morphiaSession = ((MongoSession) session).getSession();
    return update(morphiaSession, transaction);
  }

  private Transaction update(Datastore ds, Transaction transaction) {
    var entity = TransactionModel.fromTransaction(transaction);
    ds.save(entity);
    return transaction;
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
    ds.find(TransactionModel.class)
        .filter(Filters.eq("_id", id))
        .delete();
  }
}
