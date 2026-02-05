package io.dkakunsi.bitapp.loan.usecase;

import io.dkakunsi.bitapp.common.AppError.Code;
import io.dkakunsi.bitapp.common.Context;
import io.dkakunsi.bitapp.database.SessionManager;
import io.dkakunsi.bitapp.domain.entity.Id;
import io.dkakunsi.bitapp.domain.usecase.Result;
import io.dkakunsi.bitapp.domain.usecase.UseCase;
import io.dkakunsi.bitapp.loan.dto.LoanResult;
import io.dkakunsi.bitapp.loan.entity.Loan;
import io.dkakunsi.bitapp.loan.repository.LoanRepository;
import io.dkakunsi.bitapp.transaction.repository.TransactionRepository;
import io.dkakunsi.bitapp.transaction.util.TransactionUpdateHelper;

public final class RemoveLoan implements UseCase<String, LoanResult> {

  private final LoanRepository loanRepository;
  private final TransactionRepository transactionRepository;
  private final SessionManager sessionManager;

  public RemoveLoan(LoanRepository loanRepository, TransactionRepository transactionRepository,
      SessionManager sessionManager) {
    this.loanRepository = loanRepository;
    this.transactionRepository = transactionRepository;
    this.sessionManager = sessionManager;
  }

  @Override
  public Result<LoanResult> execute(Context context, String loanId) {
    return loanRepository.findById(Id.of(loanId))
        .map(loan -> onLoan(context, loan))
        .orElse(Result.failure(Code.NOT_FOUND, "Loan not found"));
  }

  private Result<LoanResult> onLoan(Context context, Loan loan) {
    if (!loan.isOwner(context.requester())) {
      return Result.failure(Code.FORBIDDEN, "You are not authorized to delete this loan");
    }

    return sessionManager.executeInSession(() -> {
      transactionRepository.findByLoanId(loan.id()).forEach(t -> {
        var ut = TransactionUpdateHelper.removeLoanReference(t, context.requester());
        transactionRepository.update(ut);
      });
      loanRepository.deleteById(loan.id());
      return Result.success(loan.toResult());
    });
  }
}
