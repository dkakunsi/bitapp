package io.dkakunsi.bitapp.loan.usecase;

import io.dkakunsi.bitapp.common.Context;
import io.dkakunsi.bitapp.common.usecase.Result;
import io.dkakunsi.bitapp.common.usecase.UseCase;
import io.dkakunsi.bitapp.loan.dto.CreateLoanInput;
import io.dkakunsi.bitapp.loan.dto.LoanResult;
import io.dkakunsi.bitapp.loan.entity.Loan;
import io.dkakunsi.bitapp.loan.repository.LoanRepository;

public final class CreateLoan implements UseCase<CreateLoanInput, LoanResult> {

  private final LoanRepository loanRepository;

  public CreateLoan(LoanRepository loanRepository) {
    this.loanRepository = loanRepository;
  }

  @Override
  public Result<LoanResult> execute(Context context, CreateLoanInput input) {
    final var loan = Loan.from(input, context.requester());
    var createdLoan = this.loanRepository.create(loan);
    return Result.success(createdLoan.toResult());
  }
}
