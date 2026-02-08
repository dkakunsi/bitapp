package io.dkakunsi.bitapp.loan.usecase;

import io.dkakunsi.bitapp.account.repository.AccountRepository;
import io.dkakunsi.bitapp.common.AppError.Code;
import io.dkakunsi.bitapp.database.SessionManager;
import io.dkakunsi.bitapp.domain.entity.Id;
import io.dkakunsi.bitapp.domain.usecase.Result;
import io.dkakunsi.bitapp.domain.usecase.UseCase;
import io.dkakunsi.bitapp.loan.dto.CreateLoanInput;
import io.dkakunsi.bitapp.loan.dto.LoanResult;
import io.dkakunsi.bitapp.loan.entity.Loan;
import io.dkakunsi.bitapp.loan.repository.LoanRepository;
import io.dkakunsi.bitapp.transaction.dto.CreateLoanDisbursementTransactionInput;
import io.dkakunsi.bitapp.transaction.usecase.CreateTransaction;

public final class CreateLoan implements UseCase<CreateLoanInput, LoanResult> {

  private final LoanRepository loanRepository;
  private final AccountRepository accountRepository;
  private final SessionManager sessionManager;
  private final CreateTransaction createTransaction;

  public CreateLoan(
      LoanRepository loanRepository,
      AccountRepository accountRepository,
      SessionManager sessionManager,
      CreateTransaction createTransaction) {
    this.loanRepository = loanRepository;
    this.accountRepository = accountRepository;
    this.sessionManager = sessionManager;
    this.createTransaction = createTransaction;
  }

  @Override
  public Result<LoanResult> execute(CreateLoanInput input) {
    var requester = getRequester();
    final var loan = Loan.from(input, requester);
    if (input.account() == null) {
      var createdLoan = this.loanRepository.create(loan);
      return Result.success(createdLoan.toResult());
    }

    var account = accountRepository.findById(Id.of(input.account()))
        .orElse(null);

    if (account == null) {
      return Result.failure(Code.NOT_FOUND, "Account not found");
    }

    if (!account.isOwner(requester)) {
      return Result.failure(Code.FORBIDDEN, "You are not authorized to use this account");
    }

    return sessionManager.executeInSession(() -> {
      var createdLoan = this.loanRepository.create(loan);
      var disbursementResult = createDisbursementTransaction(createdLoan, account.id().value());
      if (disbursementResult.isFailed()) {
        var error = disbursementResult.error().get();
        return Result.failure(error.code(), error.message());
      }
      return Result.success(createdLoan.toResult());
    });
  }

  private Result<Void> createDisbursementTransaction(Loan loan, String accountId) {
    var transactionInput = CreateLoanDisbursementTransactionInput.builder()
        .loan(loan)
        .build();

    var result = createTransaction.execute(transactionInput);
    if (result.isFailed()) {
      var error = result.error().get();
      return Result.failure(error.code(), error.message());
    }
    return Result.success();
  }
}
