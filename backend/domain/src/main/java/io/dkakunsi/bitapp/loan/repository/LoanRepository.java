package io.dkakunsi.bitapp.loan.repository;

import io.dkakunsi.bitapp.loan.entity.Loan;

public interface LoanRepository {
  Loan create(Loan loan);
}
