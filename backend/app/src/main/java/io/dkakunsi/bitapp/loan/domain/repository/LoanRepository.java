package io.dkakunsi.bitapp.loan.domain.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import io.dkakunsi.bitapp.Id;
import io.dkakunsi.bitapp.loan.domain.entity.Loan;

public interface LoanRepository {
  Loan create(Loan loan);

  Loan update(Loan loan);

  void increaseRemainingAmount(Id loan, BigDecimal amount);

  void decreaseRemainingAmount(Id id, BigDecimal amount);

  void deleteById(Id id);

  Optional<Loan> findById(Id id);

  List<Loan> findByUserId(Id userId);

  List<Loan> findByAccountId(Id id);

  default boolean isExistingLoan(Id loan) {
    return findById(loan).isPresent();
  }

  default boolean isNotExistingLoan(Id loan) {
    return !isExistingLoan(loan);
  }
}
