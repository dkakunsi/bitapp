package io.dkakunsi.bitapp.loan.infrastructure.mongo.repository;

import java.math.BigDecimal;
import java.util.List;

import dev.morphia.Datastore;
import dev.morphia.UpdateOptions;
import dev.morphia.query.filters.Filters;
import dev.morphia.query.updates.UpdateOperators;
import io.dkakunsi.bitapp.Id;
import io.dkakunsi.bitapp.loan.domain.entity.Loan;
import io.dkakunsi.bitapp.loan.domain.repository.LoanRepository;
import io.dkakunsi.bitapp.loan.infrastructure.mongo.model.LoanModel;
import io.dkakunsi.bitapp.mongo.MongoRepository;

public class MongoLoanRepository extends MongoRepository<LoanModel, Loan> implements LoanRepository {

  public MongoLoanRepository(Datastore datastore) {
    super(datastore);
  }

  @Override
  protected LoanModel fromEntity(Loan loan) {
    return LoanModel.fromLoan(loan);
  }

  @Override
  protected Loan toEntity(LoanModel model) {
    return model.toLoan();
  }

  @Override
  public void increaseRemainingAmount(Id loan, BigDecimal amount) {
    pickDatastore().find(LoanModel.class)
        .filter(Filters.eq(MONGO_ID, loan.value()))
        .update(new UpdateOptions(), UpdateOperators.inc("remainingAmount", amount.doubleValue()));
  }

  @Override
  public void decreaseRemainingAmount(Id id, BigDecimal amount) {
    pickDatastore().find(LoanModel.class)
        .filter(Filters.eq(MONGO_ID, id.value()))
        .update(new UpdateOptions(), UpdateOperators.dec("remainingAmount", amount.doubleValue()));
  }

  @Override
  public List<Loan> findByUserId(Id userId) {
    return pickDatastore().find(LoanModel.class)
        .filter(Filters.eq("userId", userId.value()))
        .stream()
        .map(LoanModel::toLoan)
        .toList();
  }

  @Override
  public List<Loan> findByAccountId(Id id) {
    return pickDatastore().find(LoanModel.class)
        .filter(Filters.eq("accountId", id.value()))
        .stream()
        .map(LoanModel::toLoan)
        .toList();
  }
}
