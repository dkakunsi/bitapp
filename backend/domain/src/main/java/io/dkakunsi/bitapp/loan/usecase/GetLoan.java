package io.dkakunsi.bitapp.loan.usecase;

import io.dkakunsi.bitapp.common.Context;
import io.dkakunsi.bitapp.common.usecase.Result;
import io.dkakunsi.bitapp.common.usecase.UseCase;
import io.dkakunsi.bitapp.loan.dto.LoanResult;
import io.dkakunsi.bitapp.loan.repository.LoanRepository;

public final class GetLoan implements UseCase<String, LoanResult> {

  private final LoanRepository loanRepository;

  public GetLoan(LoanRepository loanRepository) {
    this.loanRepository = loanRepository;
  }

  @Override
  public Result<LoanResult> execute(Context context, String loanId) {
    var loan = loanRepository.findById(loanId)
        .orElseThrow(() -> new IllegalArgumentException("Loan not found"));
    return Result.success(loan.toResult());
  }
}
