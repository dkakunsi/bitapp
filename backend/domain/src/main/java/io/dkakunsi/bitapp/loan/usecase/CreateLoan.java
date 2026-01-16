package io.dkakunsi.bitapp.loan.usecase;

import io.dkakunsi.bitapp.common.AppError.Code;
import io.dkakunsi.bitapp.common.Context;
import io.dkakunsi.bitapp.common.usecase.Result;
import io.dkakunsi.bitapp.common.usecase.UseCase;
import io.dkakunsi.bitapp.loan.dto.CreateLoanInput;
import io.dkakunsi.bitapp.loan.dto.CreateLoanResult;
import io.dkakunsi.bitapp.loan.repository.LoanRepository;

public final class CreateLoan implements UseCase<CreateLoanInput, CreateLoanResult> {

  private final LoanRepository loanRepository;

  public CreateLoan(LoanRepository loanRepository) {
    this.loanRepository = loanRepository;
  }

  @Override
  public Result<CreateLoanResult> process(Context context, CreateLoanInput input) {
    final var loan = input.toLoan(context.requester());
    try {
      var result = this.loanRepository.create(loan);
      return Result.success(CreateLoanResult.from(result));
    } catch (Exception e) {
      return Result.failure(Code.SERVER_ERROR, e.getMessage());
    }
  }
}
