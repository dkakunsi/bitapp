package io.dkakunsi.bitapp.loan.domain.port;

import io.dkakunsi.bitapp.Id;
import io.dkakunsi.bitapp.Result;
import io.dkakunsi.bitapp.loan.domain.entity.Loan;

public interface LoanTransactionPort {
  
  void updateTransactionByLoanRemoval(Id loanId);

  Result<Void> disburseTransaction(Loan loan);
}
