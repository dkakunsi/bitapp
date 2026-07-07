package io.dkakunsi.bitapp.loan.application.usecase;

import io.dkakunsi.bitapp.AppError.Code;
import io.dkakunsi.bitapp.Id;
import io.dkakunsi.bitapp.Result;
import io.dkakunsi.bitapp.SessionManager;
import io.dkakunsi.bitapp.UseCase;
import io.dkakunsi.bitapp.loan.application.dto.CreateLoanInput;
import io.dkakunsi.bitapp.loan.application.dto.LoanResult;
import io.dkakunsi.bitapp.loan.domain.port.LoanAccountPort;
import io.dkakunsi.bitapp.loan.domain.port.LoanTransactionPort;
import io.dkakunsi.bitapp.loan.domain.repository.LoanRepository;

public final class CreateLoan implements UseCase<CreateLoanInput, LoanResult> {

  private final SessionManager sessionManager;
  private final LoanRepository loanRepository;
  private final LoanAccountPort loanAccountAdapter;
  private final LoanTransactionPort loanTransactionAdapter;

  public CreateLoan(
      SessionManager sessionManager,
      LoanRepository loanRepository,
      LoanAccountPort loanAccountAdapter,
      LoanTransactionPort loanTransactionAdapter) {
    this.loanRepository = loanRepository;
    this.loanAccountAdapter = loanAccountAdapter;
    this.loanTransactionAdapter = loanTransactionAdapter;
    this.sessionManager = sessionManager;
  }

  @Override
  public Result<LoanResult> execute(CreateLoanInput input) {
    var requester = getRequester();
    final var loan = input.toLoan(requester);
    if (input.account() == null) {
      var createdLoan = this.loanRepository.create(loan);
      return Result.success(LoanResult.from(createdLoan));
    }

    var account = loanAccountAdapter.findById(Id.of(input.account()))
        .orElse(null);

    if (account == null) {
      return Result.failure(Code.NOT_FOUND, "Account not found");
    }

    if (!account.isOwner(requester)) {
      return Result.failure(Code.FORBIDDEN, "You are not authorized to use this account");
    }

    return sessionManager.executeInSession(() -> {
      var createdLoan = this.loanRepository.create(loan);
      var disbursementResult = loanTransactionAdapter.disburseTransaction(createdLoan);
      if (disbursementResult.isFailed()) {
        var error = disbursementResult.error().get();
        return Result.failure(error.code(), error.message());
      }
      return Result.success(LoanResult.from(createdLoan));
    });
  }
}
