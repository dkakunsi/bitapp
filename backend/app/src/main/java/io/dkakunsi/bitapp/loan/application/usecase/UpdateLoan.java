package io.dkakunsi.bitapp.loan.application.usecase;

import io.dkakunsi.bitapp.Id;
import io.dkakunsi.bitapp.Result;
import io.dkakunsi.bitapp.UseCase;
import io.dkakunsi.bitapp.loan.application.dto.LoanResult;
import io.dkakunsi.bitapp.loan.application.dto.UpdateLoanInput;
import io.dkakunsi.bitapp.loan.domain.entity.Loan;
import io.dkakunsi.bitapp.loan.domain.repository.LoanRepository;

public final class UpdateLoan implements UseCase<UpdateLoanInput, LoanResult> {

  private final LoanRepository loanRepository;

  public UpdateLoan(LoanRepository loanRepository) {
    this.loanRepository = loanRepository;
  }

  @Override
  public Result<LoanResult> execute(UpdateLoanInput input) {
    return loanRepository.findById(Id.of(input.id()))
        .map(loan -> onLoan(loan, input))
        .orElse(Result.notFound("Loan not found"));
  }

  private Result<LoanResult> onLoan(Loan loan, UpdateLoanInput input) {
    var requester = getRequester();
    if (!loan.isOwner(requester)) {
      return Result.forbidden("You are not authorized to update this loan");
    }

    var updatingLoan = input.toLoan();
    var updatedLoan = loanRepository.update(loan.update(updatingLoan, requester));
    return Result.success(LoanResult.from(updatedLoan));
  }
}
