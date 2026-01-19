package io.dkakunsi.bitapp.loan.repository;

import java.util.List;

import io.dkakunsi.bitapp.loan.entity.Loan;

public interface LoanRepository {
  Loan create(Loan loan);

  List<Loan> findByUserId(String userId);
}
