package io.dkakunsi.bitapp.mongo.repository;

import dev.morphia.Datastore;
import io.dkakunsi.bitapp.loan.entity.Loan;
import io.dkakunsi.bitapp.loan.repository.LoanRepository;
import io.dkakunsi.bitapp.mongo.model.LoanEntity;

public class MongoLoanRepository implements LoanRepository {

  private final Datastore datastore;

  public MongoLoanRepository(Datastore datastore) {
    this.datastore = datastore;
  }

  @Override
  public Loan create(Loan loan) {
    var entity = LoanEntity.fromLoan(loan);
    datastore.save(entity);
    return loan;
  }
}
