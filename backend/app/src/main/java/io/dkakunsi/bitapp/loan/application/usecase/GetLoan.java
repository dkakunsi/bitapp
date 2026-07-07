package io.dkakunsi.bitapp.loan.application.usecase;

import io.dkakunsi.bitapp.AppError.Code;
import io.dkakunsi.bitapp.Id;
import io.dkakunsi.bitapp.Result;
import io.dkakunsi.bitapp.UseCase;
import io.dkakunsi.bitapp.loan.application.dto.LoanResult;
import io.dkakunsi.bitapp.loan.domain.repository.LoanRepository;

public final class GetLoan implements UseCase<String, LoanResult> {

  private final LoanRepository loanRepository;

  public GetLoan(LoanRepository loanRepository) {
    this.loanRepository = loanRepository;
  }

  @Override
  public Result<LoanResult> execute(String loanId) {
    return loanRepository.findById(Id.of(loanId))
        .map(loan -> Result.success(LoanResult.from(loan)))
        .orElse(Result.failure(Code.NOT_FOUND, "Loan not found"));
  }
}
