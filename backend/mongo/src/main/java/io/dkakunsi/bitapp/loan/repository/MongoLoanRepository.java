package io.dkakunsi.bitapp.loan.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import dev.morphia.Datastore;
import dev.morphia.UpdateOptions;
import dev.morphia.query.filters.Filters;
import dev.morphia.query.updates.UpdateOperators;
import io.dkakunsi.bitapp.domain.entity.Id;
import io.dkakunsi.bitapp.loan.entity.Loan;
import io.dkakunsi.bitapp.loan.model.LoanModel;
import io.dkakunsi.bitapp.mongo.MongoRepository;

public class MongoLoanRepository extends MongoRepository implements LoanRepository {

  public MongoLoanRepository(Datastore datastore) {
    super(datastore);
  }

  @Override
  public Loan create(Loan loan) {
    var entity = LoanModel.fromLoan(loan);
    pickDatastore().save(entity);
    return loan;
  }

  @Override
  public Loan update(Loan loan) {
    var entity = LoanModel.fromLoan(loan);
    pickDatastore().save(entity);
    return loan;
  }

  @Override
  public void decreaseRemainingAmount(Id id, BigDecimal amount) {
    pickDatastore().find(LoanModel.class)
        .filter(Filters.eq(MONGO_ID, id.value()))
        .update(new UpdateOptions(), UpdateOperators.dec("remainingAmount", amount.doubleValue()));
  }

  @Override
  public void deleteById(Id id) {
    pickDatastore().find(LoanModel.class)
        .filter(Filters.eq(MONGO_ID, id.value()))
        .delete();
  }

  @Override
  public Optional<Loan> findById(Id id) {
    var entity = pickDatastore().find(LoanModel.class)
        .filter(Filters.eq(MONGO_ID, id.value()))
        .first();
    return Optional.ofNullable(entity).map(LoanModel::toLoan);
  }

  @Override
  public List<Loan> findByUserId(Id userId) {
    return pickDatastore().find(LoanModel.class)
        .filter(Filters.eq("userId", userId.value()))
        .stream()
        .map(LoanModel::toLoan)
        .toList();
  }
}
