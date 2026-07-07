package io.dkakunsi.bitapp.transaction.infrastructure.loan;

import java.math.BigDecimal;

import io.dkakunsi.bitapp.Id;
import io.dkakunsi.bitapp.loan.application.dto.UpdateRemainingAmountInput;
import io.dkakunsi.bitapp.loan.application.usecase.GetLoan;
import io.dkakunsi.bitapp.loan.application.usecase.UpdateRemainingAmount;
import io.dkakunsi.bitapp.transaction.domain.port.TransactionLoanPort;

public class InProcessTransactionLoanAdapter implements TransactionLoanPort {

  private final UpdateRemainingAmount updateRemainingAmount;

  private final GetLoan getLoan;

  public InProcessTransactionLoanAdapter(UpdateRemainingAmount updateRemainingAmount, GetLoan getLoan) {
    this.updateRemainingAmount = updateRemainingAmount;
    this.getLoan = getLoan;
  }

  @Override
  public boolean isExistingLoan(Id loanId) {
    if (loanId == null) {
      return false;
    }

    var result = getLoan.execute(loanId.value());
    if (result.isFailed() || result.isEmpty()) {
      return false;
    }

    return true;
  }

  @Override
  public void decreaseRemainingAmount(Id loan, BigDecimal amount) {
    var input = UpdateRemainingAmountInput.builder()
        .loanId(loan.value())
        .amount(amount)
        .isIncrease(false)
        .build();
    updateRemainingAmount.execute(input);
  }

  @Override
  public void increaseRemainingAmount(Id loan, BigDecimal amount) {
    var input = UpdateRemainingAmountInput.builder()
        .loanId(loan.value())
        .amount(amount)
        .isIncrease(true)
        .build();
    updateRemainingAmount.execute(input);
  }
}
