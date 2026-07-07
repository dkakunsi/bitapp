package io.dkakunsi.bitapp.loan.application.usecase;

import io.dkakunsi.bitapp.AppError;
import io.dkakunsi.bitapp.Id;
import io.dkakunsi.bitapp.Result;
import io.dkakunsi.bitapp.UseCase;
import io.dkakunsi.bitapp.loan.application.dto.LoanResult;
import io.dkakunsi.bitapp.loan.application.dto.UpdateRemainingAmountInput;
import io.dkakunsi.bitapp.loan.domain.entity.Loan;
import io.dkakunsi.bitapp.loan.domain.repository.LoanRepository;

public class UpdateRemainingAmount implements UseCase<UpdateRemainingAmountInput, LoanResult> {

  private final LoanRepository loanRepository;

  public UpdateRemainingAmount(LoanRepository loanRepository) {
    this.loanRepository = loanRepository;
  }

  @Override
  public Result<LoanResult> execute(UpdateRemainingAmountInput input) {
    return loanRepository.findById(Id.of(input.loanId()))
        .map(loan -> onLoan(loan, input))
        .orElse(Result.failure(AppError.Code.NOT_FOUND, "Loan not found"));
  }

  private Result<LoanResult> onLoan(Loan loan, UpdateRemainingAmountInput input) {
    var requester = getRequester();
    if (!loan.isOwner(requester)) {
      return Result.failure(AppError.Code.FORBIDDEN, "You are not authorized to update this loan");
    }

    var loanId = Id.of(input.loanId());
    var amount = input.amount();

    if (input.isIncrease()) {
      loanRepository.increaseRemainingAmount(loanId, amount);
    } else {
      loanRepository.decreaseRemainingAmount(loanId, amount);
    }
    return Result.success();
  }
}
