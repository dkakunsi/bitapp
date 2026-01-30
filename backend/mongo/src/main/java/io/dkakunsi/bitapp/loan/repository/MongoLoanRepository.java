package io.dkakunsi.bitapp.loan.repository;

import java.util.List;
import java.util.Optional;

import dev.morphia.Datastore;
import dev.morphia.query.filters.Filters;
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
  public void deleteById(String id) {
    pickDatastore().find(LoanModel.class)
        .filter(Filters.eq("_id", id))
        .delete();
  }

  @Override
  public Optional<Loan> findById(String id) {
    var entity = datastore.find(LoanModel.class)
        .filter(Filters.eq("_id", id))
        .first();
    return Optional.ofNullable(entity).map(LoanModel::toLoan);
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
