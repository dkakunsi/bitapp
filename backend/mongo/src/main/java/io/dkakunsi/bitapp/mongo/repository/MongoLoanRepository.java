package io.dkakunsi.bitapp.mongo.repository;

import java.util.List;

import dev.morphia.Datastore;
import dev.morphia.query.filters.Filters;
import io.dkakunsi.bitapp.loan.entity.Loan;
import io.dkakunsi.bitapp.loan.repository.LoanRepository;
import io.dkakunsi.bitapp.mongo.model.LoanModel;

public class MongoLoanRepository implements LoanRepository {

  private final Datastore datastore;

  public MongoLoanRepository(Datastore datastore) {
    this.datastore = datastore;
  }

  @Override
  public Loan create(Loan loan) {
    var entity = LoanModel.fromLoan(loan);
    datastore.save(entity);
    return loan;
  }

  @Override
  public List<Loan> findByUserId(String userId) {
    return datastore.find(LoanModel.class)
        .filter(Filters.eq("userId", userId))
        .stream()
        .map(LoanModel::toLoan)
        .toList();
  }
}
