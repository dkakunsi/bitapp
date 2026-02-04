package io.dkakunsi.bitapp.loan.usecase;

import io.dkakunsi.bitapp.common.AppError.Code;
import io.dkakunsi.bitapp.common.Context;
import io.dkakunsi.bitapp.domain.entity.Id;
import io.dkakunsi.bitapp.domain.usecase.Result;
import io.dkakunsi.bitapp.domain.usecase.UseCase;
import io.dkakunsi.bitapp.loan.dto.LoanResult;
import io.dkakunsi.bitapp.loan.repository.LoanRepository;

public final class GetLoan implements UseCase<String, LoanResult> {

  private final LoanRepository loanRepository;

  public GetLoan(LoanRepository loanRepository) {
    this.loanRepository = loanRepository;
  }

  @Override
  public Result<LoanResult> execute(Context context, String loanId) {
    return loanRepository.findById(Id.of(loanId))
        .map(loan -> Result.success(loan.toResult()))
        .orElse(Result.failure(Code.NOT_FOUND, "Loan not found"));
  }
}
