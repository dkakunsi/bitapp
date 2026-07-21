package io.dkakunsi.bitapp.loan.infrastructure.transaction;

import io.dkakunsi.bitapp.Id;
import io.dkakunsi.bitapp.Result;
import io.dkakunsi.bitapp.loan.application.port.LoanTransactionPort;
import io.dkakunsi.bitapp.loan.domain.entity.Loan;
import io.dkakunsi.bitapp.transaction.application.dto.CreateLoanDisbursementTransactionInput;
import io.dkakunsi.bitapp.transaction.application.usecase.CreateTransaction;
import io.dkakunsi.bitapp.transaction.application.usecase.ProcessTransactionByLoanRemoval;

public class InProcessLoanTransactionAdapter implements LoanTransactionPort {

  private final ProcessTransactionByLoanRemoval processTransactionByLoanRemoval;
  private final CreateTransaction createTransaction;

  public InProcessLoanTransactionAdapter(ProcessTransactionByLoanRemoval processTransactionByLoanRemoval,
      CreateTransaction createTransaction) {
    this.processTransactionByLoanRemoval = processTransactionByLoanRemoval;
    this.createTransaction = createTransaction;
  }

  @Override
  public void updateTransactionByLoanRemoval(Id loanId) {
    processTransactionByLoanRemoval.execute(loanId.value());
  }

  @Override
  public Result<Void> disburseTransaction(Loan loan) {
    var transactionInput = CreateLoanDisbursementTransactionInput.builder()
        .loan(loan)
        .build();
    var transactionResult = createTransaction.execute(transactionInput);
    if (transactionResult.isFailed()) {
      return Result.failure(transactionResult);
    }
    return Result.success();
  }
}
