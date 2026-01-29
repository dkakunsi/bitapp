package io.dkakunsi.bitapp.loan.repository;

import java.util.List;
import java.util.Optional;

import io.dkakunsi.bitapp.loan.entity.Loan;

public interface LoanRepository {
  Loan create(Loan loan);

  Optional<Loan> findById(String id);

  List<Loan> findByUserId(String userId);

  Loan update(Loan loan);

  void deleteById(String id);
}
