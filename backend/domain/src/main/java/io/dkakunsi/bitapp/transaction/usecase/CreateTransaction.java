package io.dkakunsi.bitapp.transaction.usecase;

import java.math.BigDecimal;

import io.dkakunsi.bitapp.account.entity.Account;
import io.dkakunsi.bitapp.account.repository.AccountRepository;
import io.dkakunsi.bitapp.common.Context;
import io.dkakunsi.bitapp.common.usecase.Result;
import io.dkakunsi.bitapp.common.usecase.UseCase;
import io.dkakunsi.bitapp.loan.entity.Loan;
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
    var updatedAccount = Account.builder()
        .id(sourceAccount.id())
        .user(sourceAccount.user())
        .name(sourceAccount.name())
        .type(sourceAccount.type())
        .themeColor(sourceAccount.themeColor())
        .balance(newBalance)
        .status(sourceAccount.status())
        .createdAt(sourceAccount.createdAt())
        .updatedAt(sourceAccount.updatedAt())
        .createdBy(sourceAccount.createdBy())
        .updatedBy(sourceAccount.updatedBy())
        .build();

    accountRepository.update(updatedAccount);
  }

  private void processCredit(Transaction transaction) {
    var destinationAccount = accountRepository.findById(transaction.destination().value())
        .orElseThrow(() -> new IllegalArgumentException("destination account not found"));

    var newBalance = destinationAccount.balance().add(BigDecimal.valueOf(transaction.amount()));
    var updatedAccount = Account.builder()
        .id(destinationAccount.id())
        .user(destinationAccount.user())
        .name(destinationAccount.name())
        .type(destinationAccount.type())
        .themeColor(destinationAccount.themeColor())
        .balance(newBalance)
        .status(destinationAccount.status())
        .createdAt(destinationAccount.createdAt())
        .updatedAt(destinationAccount.updatedAt())
        .createdBy(destinationAccount.createdBy())
        .updatedBy(destinationAccount.updatedBy())
        .build();

    accountRepository.update(updatedAccount);
  }

  private void processTransfer(Transaction transaction) {
    var sourceAccount = accountRepository.findById(transaction.source().value())
        .orElseThrow(() -> new IllegalArgumentException("source account not found"));

    var destinationAccount = accountRepository.findById(transaction.destination().value())
        .orElseThrow(() -> new IllegalArgumentException("destination account not found"));

    var sourceNewBalance = sourceAccount.balance().subtract(BigDecimal.valueOf(transaction.amount()));
    var updatedSourceAccount = Account.builder()
        .id(sourceAccount.id())
        .user(sourceAccount.user())
        .name(sourceAccount.name())
        .type(sourceAccount.type())
        .themeColor(sourceAccount.themeColor())
        .balance(sourceNewBalance)
        .status(sourceAccount.status())
        .createdAt(sourceAccount.createdAt())
        .updatedAt(sourceAccount.updatedAt())
        .createdBy(sourceAccount.createdBy())
        .updatedBy(sourceAccount.updatedBy())
        .build();

    var destNewBalance = destinationAccount.balance().add(BigDecimal.valueOf(transaction.amount()));
    var updatedDestAccount = Account.builder()
        .id(destinationAccount.id())
        .user(destinationAccount.user())
        .name(destinationAccount.name())
        .type(destinationAccount.type())
        .themeColor(destinationAccount.themeColor())
        .balance(destNewBalance)
        .status(destinationAccount.status())
        .createdAt(destinationAccount.createdAt())
        .updatedAt(destinationAccount.updatedAt())
        .createdBy(destinationAccount.createdBy())
        .updatedBy(destinationAccount.updatedBy())
        .build();

    accountRepository.update(updatedSourceAccount);
    accountRepository.update(updatedDestAccount);
  }

  private void processLoan(Transaction transaction) {
    var loan = loanRepository.findById(transaction.loan().value())
        .orElseThrow(() -> new IllegalArgumentException("loan not found"));

    var newRemainingAmount = loan.remainingAmount().subtract(BigDecimal.valueOf(transaction.amount()));
    var updatedLoan = Loan.builder()
        .id(loan.id())
        .user(loan.user())
        .type(loan.type())
        .partyName(loan.partyName())
        .date(loan.date())
        .time(loan.time())
        .title(loan.title())
        .description(loan.description())
        .amount(loan.amount())
        .remainingAmount(newRemainingAmount)
        .currency(loan.currency())
        .interestRate(loan.interestRate())
        .status(loan.status())
        .createdAt(loan.createdAt())
        .updatedAt(loan.updatedAt())
        .createdBy(loan.createdBy())
        .updatedBy(loan.updatedBy())
        .build();

    loanRepository.update(updatedLoan);
  }
}
