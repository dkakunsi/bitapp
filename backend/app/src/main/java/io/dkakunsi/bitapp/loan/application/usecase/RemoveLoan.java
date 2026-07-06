package io.dkakunsi.bitapp.loan.application.usecase;

import io.dkakunsi.bitapp.AppError.Code;
import io.dkakunsi.bitapp.Context;
import io.dkakunsi.bitapp.Id;
import io.dkakunsi.bitapp.Result;
import io.dkakunsi.bitapp.SessionManager;
import io.dkakunsi.bitapp.UseCase;
import io.dkakunsi.bitapp.loan.application.dto.LoanResult;
import io.dkakunsi.bitapp.loan.domain.entity.Loan;
import io.dkakunsi.bitapp.loan.domain.repository.LoanRepository;
import io.dkakunsi.bitapp.transaction.domain.repository.TransactionRepository;
import io.dkakunsi.bitapp.transaction.util.TransactionUpdateHelper;

public class RemoveLoan implements UseCase<String, LoanResult> {

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
  public Result<LoanResult> execute(String loanId) {
    return loanRepository.findById(Id.of(loanId))
        .map(loan -> onLoan(loan))
        .orElse(Result.failure(Code.NOT_FOUND, "Loan not found"));
  }

  private Result<LoanResult> onLoan(Loan loan) {
    var context = Context.current();
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
