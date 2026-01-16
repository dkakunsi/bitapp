package io.dkakunsi.bitapp.mongo.repository;

import dev.morphia.Datastore;
import io.dkakunsi.bitapp.loan.model.Loan;
import io.dkakunsi.bitapp.loan.repository.LoanRepository;
import io.dkakunsi.bitapp.mongo.entity.LoanEntity;

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
