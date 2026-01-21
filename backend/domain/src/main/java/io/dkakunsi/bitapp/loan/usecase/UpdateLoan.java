package io.dkakunsi.bitapp.loan.usecase;

import io.dkakunsi.bitapp.common.AppError;
import io.dkakunsi.bitapp.common.Context;
import io.dkakunsi.bitapp.common.usecase.Result;
import io.dkakunsi.bitapp.common.usecase.UseCase;
import io.dkakunsi.bitapp.loan.dto.LoanResult;
import io.dkakunsi.bitapp.loan.dto.UpdateLoanInput;
import io.dkakunsi.bitapp.loan.repository.LoanRepository;

public final class UpdateLoan implements UseCase<UpdateLoanInput, LoanResult> {

  private final LoanRepository loanRepository;

  public UpdateLoan(LoanRepository loanRepository) {
    this.loanRepository = loanRepository;
  }

  @Override
  public Result<LoanResult> execute(Context context, UpdateLoanInput input) {
    var existingLoanOpt = loanRepository.findById(input.id());

    if (existingLoanOpt.isEmpty()) {
      return Result.failure(AppError.Code.NOT_FOUND, "Loan not found");
    }

    var existingLoan = existingLoanOpt.get();

    // Check if the requester owns the loan
    if (!existingLoan.user().value().equals(context.requester())) {
      return Result.failure(AppError.Code.FORBIDDEN, "You are not authorized to update this loan");
    }

    var updatedLoan = existingLoan.update(input, context.requester());
    var savedLoan = loanRepository.update(updatedLoan);

    return Result.success(savedLoan.toResult());
  }
}
