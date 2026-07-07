package io.dkakunsi.bitapp.loan.application.usecase;

import io.dkakunsi.bitapp.AppError.Code;
import io.dkakunsi.bitapp.Context;
import io.dkakunsi.bitapp.Id;
import io.dkakunsi.bitapp.Result;
import io.dkakunsi.bitapp.SessionManager;
import io.dkakunsi.bitapp.UseCase;
import io.dkakunsi.bitapp.loan.application.dto.LoanResult;
import io.dkakunsi.bitapp.loan.domain.entity.Loan;
import io.dkakunsi.bitapp.loan.domain.port.LoanTransactionPort;
import io.dkakunsi.bitapp.loan.domain.repository.LoanRepository;

public class RemoveLoan implements UseCase<String, LoanResult> {

  private final LoanRepository loanRepository;
  private final LoanTransactionPort loanTransactionAdapter;
  private final SessionManager sessionManager;

  public RemoveLoan(LoanRepository loanRepository, LoanTransactionPort loanTransactionAdapter,
      SessionManager sessionManager) {
    this.loanRepository = loanRepository;
    this.loanTransactionAdapter = loanTransactionAdapter;
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
      loanTransactionAdapter.updateTransactionByLoanRemoval(loan.id());
      loanRepository.deleteById(loan.id());
      return Result.success(LoanResult.from(loan));
    });
  }
}
