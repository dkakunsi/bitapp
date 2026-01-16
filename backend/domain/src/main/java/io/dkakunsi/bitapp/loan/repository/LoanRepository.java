package io.dkakunsi.bitapp.loan.repository;

import io.dkakunsi.bitapp.loan.model.Loan;

public interface LoanRepository {
  Loan create(Loan loan);
}
