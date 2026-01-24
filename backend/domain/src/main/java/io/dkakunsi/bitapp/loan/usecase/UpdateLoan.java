package io.dkakunsi.bitapp.loan.usecase;

import io.dkakunsi.bitapp.common.AppError;
import io.dkakunsi.bitapp.common.Context;
import io.dkakunsi.bitapp.domain.usecase.Result;
import io.dkakunsi.bitapp.domain.usecase.UseCase;
import io.dkakunsi.bitapp.loan.dto.LoanResult;
import io.dkakunsi.bitapp.loan.dto.UpdateLoanInput;
import io.dkakunsi.bitapp.loan.entity.Loan;
import io.dkakunsi.bitapp.loan.repository.LoanRepository;

public final class UpdateLoan implements UseCase<UpdateLoanInput, LoanResult> {

  private final LoanRepository loanRepository;

  public UpdateLoan(LoanRepository loanRepository) {
    this.loanRepository = loanRepository;
  }

  @Override
  public Result<LoanResult> execute(Context context, UpdateLoanInput input) {
    return loanRepository.findById(input.id())
        .map(loan -> onLoan(loan, input, context.requester()))
        .orElse(Result.failure(AppError.Code.NOT_FOUND, "Loan not found"));
  }

  private Result<LoanResult> onLoan(Loan loan, UpdateLoanInput input, String requester) {
    if (!loan.isOwner(requester)) {
      return Result.failure(AppError.Code.FORBIDDEN, "You are not authorized to update this loan");
    }

    var updatedLoan = loanRepository.update(loan.update(input, requester));
    return Result.success(updatedLoan.toResult());
  }
}
