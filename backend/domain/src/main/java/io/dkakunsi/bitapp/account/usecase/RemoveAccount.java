package io.dkakunsi.bitapp.account.usecase;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import io.dkakunsi.bitapp.account.dto.AccountResult;
import io.dkakunsi.bitapp.account.entity.Account;
import io.dkakunsi.bitapp.account.repository.AccountRepository;
import io.dkakunsi.bitapp.common.AppError.Code;
import io.dkakunsi.bitapp.common.Context;
import io.dkakunsi.bitapp.database.SessionManager;
import io.dkakunsi.bitapp.domain.entity.Id;
import io.dkakunsi.bitapp.domain.usecase.Result;
import io.dkakunsi.bitapp.domain.usecase.UseCase;
import io.dkakunsi.bitapp.loan.repository.LoanRepository;
import io.dkakunsi.bitapp.transaction.entity.Transaction;
import io.dkakunsi.bitapp.transaction.repository.TransactionRepository;
import io.dkakunsi.bitapp.transaction.util.TransactionUpdateHelper;

public final class RemoveAccount implements UseCase<String, AccountResult> {

  private final AccountRepository accountRepository;
  private final TransactionRepository transactionRepository;
  private final LoanRepository loanRepository;

  private final SessionManager sessionManager;

  public RemoveAccount(
      AccountRepository accountRepository,
      TransactionRepository transactionRepository,
      LoanRepository loanRepository,
      SessionManager sessionManager) {
    this.accountRepository = accountRepository;
    this.transactionRepository = transactionRepository;
    this.loanRepository = loanRepository;
    this.sessionManager = sessionManager;
  }

  @Override
  public Result<AccountResult> execute(Context context, String accountId) {
    return accountRepository.findById(Id.of(accountId))
        .map(account -> onAccount(context, account))
        .orElse(Result.failure(Code.NOT_FOUND, "Account not found"));
  }

  private Result<AccountResult> onAccount(Context context, Account account) {
    if (!account.isOwner(context.requester())) {
      return Result.failure(Code.FORBIDDEN, "You are not authorized to delete this account");
    }

    return sessionManager.executeInSession(() -> {
      var transactions = transactionRepository.findByAccountId(account.id());
      var loanIds = new HashSet<Id>();
      for (var transaction : transactions) {
        if (transaction.loan() != null) {
          loanIds.add(transaction.loan());
        }
      }

      for (var loanId : loanIds) {
        clearLoanReferences(loanId, context.requester());
        loanRepository.findById(loanId)
            .filter(loan -> loan.isOwner(context.requester()))
            .ifPresent(loan -> loanRepository.deleteById(loanId));
      }

      for (var transaction : transactions) {
        handleTransaction(account.id(), transaction, context.requester(), loanIds);
      }

      accountRepository.deleteById(account.id());
      return Result.success(account.toResult());
    });
  }

  private void clearLoanReferences(Id loanId, String requester) {
    var loanTransactions = transactionRepository.findByLoanId(loanId);
    for (var transaction : loanTransactions) {
      var updatedTransaction = TransactionUpdateHelper.removeLoanReference(transaction, requester);
      transactionRepository.update(updatedTransaction);
    }
  }

  private void handleTransaction(
      Id accountId,
      Transaction transaction,
      String requester,
      Set<Id> loanIds) {
    switch (transaction.type()) {
      case DEBIT:
      case CREDIT:
        transactionRepository.deleteById(transaction.id());
        break;
      case TRANSFER:
        var isSource = transaction.source() != null && transaction.source().equals(accountId);
        var isDestination = transaction.destination() != null && transaction.destination().equals(accountId);
        if (isSource && isDestination) {
          transactionRepository.deleteById(transaction.id());
          break;
        }

        var removeLoan = transaction.loan() != null && loanIds.contains(transaction.loan());
        var updatedTransfer = convertTransfer(accountId, transaction, requester, removeLoan);
        transactionRepository.update(updatedTransfer);
        break;
    }
  }

  private Transaction convertTransfer(
      Id accountId,
      Transaction transaction,
      String requester,
      boolean removeLoan) {
    var isSource = transaction.source() != null && transaction.source().equals(accountId);
    var isDestination = transaction.destination() != null && transaction.destination().equals(accountId);

    var newType = transaction.type();

    if (isSource) {
      newType = Transaction.Type.CREDIT;
    } else if (isDestination) {
      newType = Transaction.Type.DEBIT;
    }

    return Transaction.builder()
        .id(transaction.id())
        .user(transaction.user())
        .title(transaction.title())
        .description(transaction.description())
        .date(transaction.date())
        .time(transaction.time())
        .source(isSource ? null : transaction.source())
        .destination(isDestination ? null : transaction.destination())
        .loan(removeLoan ? null : transaction.loan())
        .amount(transaction.amount())
        .currency(transaction.currency())
        .category(transaction.category())
        .type(newType)
        .status(transaction.status())
        .createdAt(transaction.createdAt())
        .updatedAt(LocalDateTime.now())
        .createdBy(transaction.createdBy())
        .updatedBy(requester)
        .build();
  }

}
