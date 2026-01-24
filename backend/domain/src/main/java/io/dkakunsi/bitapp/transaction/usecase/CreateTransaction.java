package io.dkakunsi.bitapp.transaction.usecase;

import java.math.BigDecimal;

import io.dkakunsi.bitapp.account.repository.AccountRepository;
import io.dkakunsi.bitapp.common.Context;
import io.dkakunsi.bitapp.domain.usecase.Result;
import io.dkakunsi.bitapp.domain.usecase.UseCase;
import io.dkakunsi.bitapp.loan.repository.LoanRepository;
import io.dkakunsi.bitapp.transaction.dto.CreateTransactionInput;
import io.dkakunsi.bitapp.transaction.dto.TransactionResult;
import io.dkakunsi.bitapp.transaction.entity.Transaction;
import io.dkakunsi.bitapp.transaction.repository.TransactionRepository;

public final class CreateTransaction implements UseCase<CreateTransactionInput, TransactionResult> {

  private final TransactionRepository transactionRepository;
  private final AccountRepository accountRepository;
  private final LoanRepository loanRepository;

  public CreateTransaction(
      TransactionRepository transactionRepository,
      AccountRepository accountRepository,
      LoanRepository loanRepository) {
    this.transactionRepository = transactionRepository;
    this.accountRepository = accountRepository;
    this.loanRepository = loanRepository;
  }

  @Override
  public Result<TransactionResult> execute(Context context, CreateTransactionInput input) {
    var transaction = Transaction.from(input, context.requester());

    try {
      validateAndProcessTransaction(transaction);
    } catch (IllegalArgumentException e) {
      // Check if it's a "not found" error
      if (e.getMessage() != null && e.getMessage().contains("not found")) {
        return Result.failure(io.dkakunsi.bitapp.common.AppError.Code.NOT_FOUND, e.getMessage());
      }
      throw e; // Re-throw other validation errors
    }

    var createdTransaction = this.transactionRepository.create(transaction);
    return Result.success(createdTransaction.toResult());
  }

  private void validateAndProcessTransaction(Transaction transaction) {
    switch (transaction.type()) {
      case DEBIT:
        processDebit(transaction);
        break;
      case CREDIT:
        processCredit(transaction);
        break;
      case TRANSFER:
        processTransfer(transaction);
        break;
    }

    if (transaction.loan() != null) {
      processLoan(transaction);
    }
  }

  private void processDebit(Transaction transaction) {
    var sourceAccount = accountRepository.findById(transaction.source().value())
        .orElseThrow(() -> new IllegalArgumentException("source account not found"));

    var newBalance = sourceAccount.balance().subtract(BigDecimal.valueOf(transaction.amount()));
    accountRepository.update(sourceAccount.updateBalance(newBalance));
  }

  private void processCredit(Transaction transaction) {
    var destinationAccount = accountRepository.findById(transaction.destination().value())
        .orElseThrow(() -> new IllegalArgumentException("destination account not found"));

    var newBalance = destinationAccount.balance().add(BigDecimal.valueOf(transaction.amount()));
    accountRepository.update(destinationAccount.updateBalance(newBalance));
  }

  private void processTransfer(Transaction transaction) {
    var sourceAccount = accountRepository.findById(transaction.source().value())
        .orElseThrow(() -> new IllegalArgumentException("source account not found"));

    var destinationAccount = accountRepository.findById(transaction.destination().value())
        .orElseThrow(() -> new IllegalArgumentException("destination account not found"));

    var sourceNewBalance = sourceAccount.balance().subtract(BigDecimal.valueOf(transaction.amount()));
    var destNewBalance = destinationAccount.balance().add(BigDecimal.valueOf(transaction.amount()));

    accountRepository.update(sourceAccount.updateBalance(sourceNewBalance));
    accountRepository.update(destinationAccount.updateBalance(destNewBalance));
  }

  private void processLoan(Transaction transaction) {
    var loan = loanRepository.findById(transaction.loan().value())
        .orElseThrow(() -> new IllegalArgumentException("loan not found"));

    var newRemainingAmount = loan.remainingAmount().subtract(BigDecimal.valueOf(transaction.amount()));
    loanRepository.update(loan.updateRemainingAmount(newRemainingAmount));
  }
}
