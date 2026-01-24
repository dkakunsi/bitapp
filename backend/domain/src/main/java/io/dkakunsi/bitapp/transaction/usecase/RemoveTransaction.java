package io.dkakunsi.bitapp.transaction.usecase;

import java.math.BigDecimal;

import io.dkakunsi.bitapp.account.repository.AccountRepository;
import io.dkakunsi.bitapp.common.AppError.Code;
import io.dkakunsi.bitapp.common.Context;
import io.dkakunsi.bitapp.domain.usecase.Result;
import io.dkakunsi.bitapp.domain.usecase.UseCase;
import io.dkakunsi.bitapp.loan.repository.LoanRepository;
import io.dkakunsi.bitapp.transaction.dto.TransactionResult;
import io.dkakunsi.bitapp.transaction.entity.Transaction;
import io.dkakunsi.bitapp.transaction.repository.TransactionRepository;

public final class RemoveTransaction implements UseCase<String, TransactionResult> {

  private final TransactionRepository transactionRepository;
  private final AccountRepository accountRepository;
  private final LoanRepository loanRepository;

  public RemoveTransaction(
      TransactionRepository transactionRepository,
      AccountRepository accountRepository,
      LoanRepository loanRepository) {
    this.transactionRepository = transactionRepository;
    this.accountRepository = accountRepository;
    this.loanRepository = loanRepository;
  }

  @Override
  public Result<TransactionResult> execute(Context context, String transactionId) {
    return transactionRepository.findById(transactionId)
        .filter(transaction -> transaction.user().value().equals(context.requester()))
        .map(transaction -> removeTransaction(transaction))
        .orElse(Result.failure(Code.NOT_FOUND, "Transaction not found"));
  }

  private Result<TransactionResult> removeTransaction(Transaction transaction) {
    try {
      revertTransactionImpact(transaction);
    } catch (IllegalArgumentException e) {
      if (e.getMessage() != null && e.getMessage().contains("not found")) {
        return Result.failure(Code.NOT_FOUND, e.getMessage());
      }
      throw e;
    }

    transactionRepository.deleteById(transaction.id().value());
    return Result.success(transaction.toResult());
  }

  private void revertTransactionImpact(Transaction transaction) {
    switch (transaction.type()) {
      case DEBIT:
        revertDebit(transaction);
        break;
      case CREDIT:
        revertCredit(transaction);
        break;
      case TRANSFER:
        revertTransfer(transaction);
        break;
    }

    if (transaction.loan() != null) {
      revertLoan(transaction);
    }
  }

  private void revertDebit(Transaction transaction) {
    var sourceAccount = accountRepository.findById(transaction.source().value())
        .orElseThrow(() -> new IllegalArgumentException("source account not found"));

    var newBalance = sourceAccount.balance().add(BigDecimal.valueOf(transaction.amount()));
    accountRepository.update(sourceAccount.updateBalance(newBalance));
  }

  private void revertCredit(Transaction transaction) {
    var destinationAccount = accountRepository.findById(transaction.destination().value())
        .orElseThrow(() -> new IllegalArgumentException("destination account not found"));

    var newBalance = destinationAccount.balance().subtract(BigDecimal.valueOf(transaction.amount()));
    accountRepository.update(destinationAccount.updateBalance(newBalance));
  }

  private void revertTransfer(Transaction transaction) {
    var sourceAccount = accountRepository.findById(transaction.source().value())
        .orElseThrow(() -> new IllegalArgumentException("source account not found"));

    var destinationAccount = accountRepository.findById(transaction.destination().value())
        .orElseThrow(() -> new IllegalArgumentException("destination account not found"));

    var sourceNewBalance = sourceAccount.balance().add(BigDecimal.valueOf(transaction.amount()));
    var destNewBalance = destinationAccount.balance().subtract(BigDecimal.valueOf(transaction.amount()));

    accountRepository.update(sourceAccount.updateBalance(sourceNewBalance));
    accountRepository.update(destinationAccount.updateBalance(destNewBalance));
  }

  private void revertLoan(Transaction transaction) {
    var loan = loanRepository.findById(transaction.loan().value())
        .orElseThrow(() -> new IllegalArgumentException("loan not found"));

    var newRemainingAmount = loan.remainingAmount().add(BigDecimal.valueOf(transaction.amount()));
    loanRepository.update(loan.updateRemainingAmount(newRemainingAmount));
  }
}
