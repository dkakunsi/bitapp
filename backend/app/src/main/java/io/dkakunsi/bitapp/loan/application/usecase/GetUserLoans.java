package io.dkakunsi.bitapp.loan.application.usecase;

import java.util.List;

import io.dkakunsi.bitapp.Id;
import io.dkakunsi.bitapp.Result;
import io.dkakunsi.bitapp.UseCase;
import io.dkakunsi.bitapp.loan.application.dto.LoanResult;
import io.dkakunsi.bitapp.loan.domain.entity.Loan;
import io.dkakunsi.bitapp.loan.domain.repository.LoanRepository;

public final class GetUserLoans implements UseCase<String, List<LoanResult>> {

  private final LoanRepository loanRepository;

  public GetUserLoans(LoanRepository loanRepository) {
    this.loanRepository = loanRepository;
  }

  @Override
  public Result<List<LoanResult>> execute(String userId) {
    var loans = loanRepository.findByUserId(Id.of(userId));
    var results = loans.stream()
        .map(Loan::toResult)
        .toList();
    return Result.success(results);
  }
}
