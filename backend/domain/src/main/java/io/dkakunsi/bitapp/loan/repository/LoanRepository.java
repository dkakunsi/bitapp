package io.dkakunsi.bitapp.loan.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import io.dkakunsi.bitapp.domain.entity.Id;
import io.dkakunsi.bitapp.loan.entity.Loan;

public interface LoanRepository {
  Loan create(Loan loan);

  Loan update(Loan loan);

  void decreaseRemainingAmount(Id id, BigDecimal amount);

  void deleteById(Id id);

  Optional<Loan> findById(Id id);

  List<Loan> findByUserId(Id userId);
}
