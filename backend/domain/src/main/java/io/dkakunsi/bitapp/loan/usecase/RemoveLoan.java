package io.dkakunsi.bitapp.loan.usecase;

import io.dkakunsi.bitapp.common.AppError.Code;
import io.dkakunsi.bitapp.common.Context;
import io.dkakunsi.bitapp.common.usecase.Result;
import io.dkakunsi.bitapp.common.usecase.UseCase;
import io.dkakunsi.bitapp.loan.dto.LoanResult;
import io.dkakunsi.bitapp.loan.entity.Loan;
import io.dkakunsi.bitapp.loan.repository.LoanRepository;
import io.dkakunsi.bitapp.transaction.repository.TransactionRepository;
import io.dkakunsi.bitapp.transaction.util.TransactionUpdateHelper;

public final class RemoveLoan implements UseCase<String, LoanResult> {

  private final LoanRepository loanRepository;
  private final TransactionRepository transactionRepository;

  public RemoveLoan(LoanRepository loanRepository, TransactionRepository transactionRepository) {
    this.loanRepository = loanRepository;
    this.transactionRepository = transactionRepository;
  }

  @Override
  public Result<LoanResult> execute(Context context, String loanId) {
    return loanRepository.findById(loanId)
        .map(loan -> onLoan(context, loan))
        .orElse(Result.failure(Code.NOT_FOUND, "Loan not found"));
  }

  private Result<LoanResult> onLoan(Context context, Loan loan) {
    if (!loan.isOwner(context.requester())) {
      return Result.failure(Code.FORBIDDEN, "You are not authorized to delete this loan");
    }

    var transactions = transactionRepository.findByLoanId(loan.id().value());
    for (var transaction : transactions) {
      var updatedTransaction = TransactionUpdateHelper.removeLoanReference(transaction, context.requester());
      transactionRepository.update(updatedTransaction);
    }

    loanRepository.deleteById(loan.id().value());
    return Result.success(loan.toResult());
  }

}
