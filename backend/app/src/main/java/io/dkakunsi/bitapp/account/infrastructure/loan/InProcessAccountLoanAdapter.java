package io.dkakunsi.bitapp.account.infrastructure.loan;

import io.dkakunsi.bitapp.Id;
import io.dkakunsi.bitapp.account.application.port.AccountLoanPort;
import io.dkakunsi.bitapp.loan.application.usecase.RemoveLoanByAccount;
import io.dkakunsi.bitapp.loan.domain.repository.LoanRepository;

public class InProcessAccountLoanAdapter implements AccountLoanPort {

  private final RemoveLoanByAccount removeLoanByAccount;

  public InProcessAccountLoanAdapter(RemoveLoanByAccount removeLoanByAccount, LoanRepository loanRepository) {
    this.removeLoanByAccount = removeLoanByAccount;
  }

  @Override
  public void removeByAccountId(Id accountId) {
    removeLoanByAccount.execute(accountId.value());
  }
}
