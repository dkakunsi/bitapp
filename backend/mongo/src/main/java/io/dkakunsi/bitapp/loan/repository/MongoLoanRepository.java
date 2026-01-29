package io.dkakunsi.bitapp.loan.repository;

import java.util.List;
import java.util.Optional;

import dev.morphia.Datastore;
import dev.morphia.query.filters.Filters;
import dev.morphia.transactions.MorphiaSession;
import io.dkakunsi.bitapp.database.SessionManager;
import io.dkakunsi.bitapp.loan.entity.Loan;
import io.dkakunsi.bitapp.loan.model.LoanModel;
import io.dkakunsi.bitapp.mongo.MongoSession;

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

  @Override
  public Loan update(Loan loan) {
    var entity = LoanModel.fromLoan(loan);
    datastore.save(entity);
    return loan;
  }

  @Override
  public void deleteById(String id) {
    var session = SessionManager.SESSION.get();
    if (session != null) {
      MorphiaSession morphiaSession = ((MongoSession) session).getSession();
      deleteById(morphiaSession, id);
    } else {
      deleteById(datastore, id);
    }
  }

  private static void deleteById(Datastore ds, String id) {
    ds.find(LoanModel.class)
        .filter(Filters.eq("_id", id))
        .delete();
  }
}
