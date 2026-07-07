package io.dkakunsi.bitapp.transaction.domain.port;

import java.math.BigDecimal;

import io.dkakunsi.bitapp.Id;

public interface TransactionLoanPort {

  boolean isExistingLoan(Id loanId);

  void decreaseRemainingAmount(Id loan, BigDecimal amount);

  void increaseRemainingAmount(Id loan, BigDecimal amount);
  
}
