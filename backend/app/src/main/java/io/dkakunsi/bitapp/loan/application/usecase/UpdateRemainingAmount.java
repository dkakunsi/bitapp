package io.dkakunsi.bitapp.loan.application.usecase;

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
        .orElse(Result.notFound("Loan not found"));
  }

  private Result<LoanResult> onLoan(Loan loan, UpdateRemainingAmountInput input) {
    var requester = getRequester();
    if (!loan.isOwner(requester)) {
      return Result.forbidden("You are not authorized to update this loan");
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
