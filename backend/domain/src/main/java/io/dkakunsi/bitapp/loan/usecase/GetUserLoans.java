package io.dkakunsi.bitapp.loan.usecase;

import java.util.List;

import io.dkakunsi.bitapp.common.Context;
import io.dkakunsi.bitapp.common.usecase.Result;
import io.dkakunsi.bitapp.common.usecase.UseCase;
import io.dkakunsi.bitapp.loan.dto.LoanResult;
import io.dkakunsi.bitapp.loan.entity.Loan;
import io.dkakunsi.bitapp.loan.repository.LoanRepository;

public final class GetUserLoans implements UseCase<String, List<LoanResult>> {

  private final LoanRepository loanRepository;

  public GetUserLoans(LoanRepository loanRepository) {
    this.loanRepository = loanRepository;
  }

  @Override
  public Result<List<LoanResult>> execute(Context context, String userId) {
    var loans = loanRepository.findByUserId(userId);
    var results = loans.stream()
        .map(Loan::toResult)
        .toList();
    return Result.success(results);
  }
}
