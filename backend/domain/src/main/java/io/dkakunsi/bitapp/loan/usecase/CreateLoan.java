package io.dkakunsi.bitapp.loan.usecase;

import io.dkakunsi.bitapp.common.Context;
import io.dkakunsi.bitapp.common.usecase.Result;
import io.dkakunsi.bitapp.common.usecase.UseCase;
import io.dkakunsi.bitapp.loan.dto.CreateLoanInput;
import io.dkakunsi.bitapp.loan.dto.CreateLoanResult;
import io.dkakunsi.bitapp.loan.model.Loan;
import io.dkakunsi.bitapp.loan.repository.LoanRepository;

public final class CreateLoan implements UseCase<CreateLoanInput, CreateLoanResult> {

  private final LoanRepository loanRepository;

  public CreateLoan(LoanRepository loanRepository) {
    this.loanRepository = loanRepository;
  }

  @Override
  public Result<CreateLoanResult> execute(Context context, CreateLoanInput input) {
    final var loan = Loan.from(input, context.requester());
    var result = this.loanRepository.create(loan);
    return Result.success(CreateLoanResult.from(result));
  }
}
