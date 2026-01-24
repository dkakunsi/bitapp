package io.dkakunsi.bitapp.loan.usecase;

import io.dkakunsi.bitapp.common.Context;
import io.dkakunsi.bitapp.common.usecase.Result;
import io.dkakunsi.bitapp.common.usecase.UseCase;
import io.dkakunsi.bitapp.account.repository.AccountRepository;
import io.dkakunsi.bitapp.common.AppError.Code;
import io.dkakunsi.bitapp.loan.dto.CreateLoanInput;
import io.dkakunsi.bitapp.loan.dto.LoanResult;
import io.dkakunsi.bitapp.loan.entity.Loan;
import io.dkakunsi.bitapp.loan.repository.LoanRepository;
import io.dkakunsi.bitapp.transaction.dto.CreateTransactionInput;
import io.dkakunsi.bitapp.transaction.entity.Transaction;
import io.dkakunsi.bitapp.transaction.repository.TransactionRepository;
import io.dkakunsi.bitapp.transaction.usecase.CreateTransaction;

public final class CreateLoan implements UseCase<CreateLoanInput, LoanResult> {

  private final LoanRepository loanRepository;
  private final AccountRepository accountRepository;
  private final CreateTransaction createTransaction;

  public CreateLoan(
      LoanRepository loanRepository,
      AccountRepository accountRepository,
      TransactionRepository transactionRepository) {
    this.loanRepository = loanRepository;
    this.accountRepository = accountRepository;
    this.createTransaction = new CreateTransaction(
        transactionRepository,
        accountRepository,
        loanRepository);
  }

  @Override
  public Result<LoanResult> execute(Context context, CreateLoanInput input) {
    final var loan = Loan.from(input, context.requester());
    if (input.account() == null) {
      var createdLoan = this.loanRepository.create(loan);
      return Result.success(createdLoan.toResult());
    }

    var account = accountRepository.findById(input.account())
        .orElse(null);

    if (account == null) {
      return Result.failure(Code.NOT_FOUND, "Account not found");
    }

    if (!account.isOwner(context.requester())) {
      return Result.failure(Code.FORBIDDEN, "You are not authorized to use this account");
    }

    var createdLoan = this.loanRepository.create(loan);
    var disbursementResult = createDisbursementTransaction(context, createdLoan, account.id().value());
    if (disbursementResult.isFailed()) {
      var error = disbursementResult.error().get();
      return Result.failure(error.code(), error.message());
    }

    var updatedLoan = loanRepository.findById(createdLoan.id().value()).orElse(createdLoan);
    return Result.success(updatedLoan.toResult());
  }

  private Result<Void> createDisbursementTransaction(Context context, Loan loan, String accountId) {
    var isBorrow = loan.type() == Loan.Type.BORROW;
    var type = isBorrow ? Transaction.Type.CREDIT.name() : Transaction.Type.DEBIT.name();
    var title = isBorrow ? "Loan Disbursement" : "Lend Disbursement";
    var description = isBorrow ? "Loan disbursement" : "Lend disbursement";

    var inputBuilder = CreateTransactionInput.builder()
        .title(title)
        .description(description)
        .amount(loan.amount().longValue())
        .currency(loan.currency().getCurrencyCode())
        .category(Transaction.Category.LOAN.name())
        .type(type)
        .loan(loan.id().value());

    if (isBorrow) {
      inputBuilder.destination(accountId);
    } else {
      inputBuilder.source(accountId);
    }

    var transactionInput = inputBuilder.build();
    var result = createTransaction.execute(context, transactionInput);
    if (result.isFailed()) {
      var error = result.error().get();
      return Result.failure(error.code(), error.message());
    }

    return Result.success();
  }
}
