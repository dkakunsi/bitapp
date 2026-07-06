package io.dkakunsi.bitapp.transaction.infrastructure.mongo.repository;

import java.util.List;

import dev.morphia.Datastore;
import dev.morphia.query.filters.Filters;
import io.dkakunsi.bitapp.Id;
import io.dkakunsi.bitapp.mongo.MongoRepository;
import io.dkakunsi.bitapp.transaction.domain.entity.Transaction;
import io.dkakunsi.bitapp.transaction.domain.repository.TransactionRepository;
import io.dkakunsi.bitapp.transaction.infrastructure.mongo.model.TransactionModel;

public class MongoTransactionRepository extends MongoRepository<TransactionModel, Transaction>
    implements TransactionRepository {

  public MongoTransactionRepository(Datastore datastore) {
    super(datastore);
  }

  @Override
  protected TransactionModel fromEntity(Transaction entity) {
    return TransactionModel.fromTransaction(entity);
  }

  @Override
  protected Transaction toEntity(TransactionModel model) {
    return model.toTransaction();
  }

  @Override
  public List<Transaction> findByUserId(Id userId) {
    return pickDatastore().find(TransactionModel.class)
        .filter(Filters.eq("userId", userId.value()))
        .stream()
        .map(TransactionModel::toTransaction)
        .toList();
  }

  @Override
  public List<Transaction> findByAccountId(Id accountId) {
    return pickDatastore().find(TransactionModel.class)
        .filter(Filters.or(
            Filters.eq("source", accountId.value()),
            Filters.eq("destination", accountId.value())))
        .stream()
        .map(TransactionModel::toTransaction)
        .toList();
  }

  @Override
  public List<Transaction> findByLoanId(Id loanId) {
    return pickDatastore().find(TransactionModel.class)
        .filter(Filters.eq("loan", loanId.value()))
        .stream()
        .map(TransactionModel::toTransaction)
        .toList();
  }
}
