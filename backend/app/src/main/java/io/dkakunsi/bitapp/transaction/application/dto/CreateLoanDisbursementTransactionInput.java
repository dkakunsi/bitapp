package io.dkakunsi.bitapp.transaction.application.dto;

import java.time.LocalDateTime;

import io.dkakunsi.bitapp.Id;
import io.dkakunsi.bitapp.loan.domain.entity.Loan;
import io.dkakunsi.bitapp.transaction.domain.entity.Transaction;
import lombok.Builder;

@Builder
public record CreateLoanDisbursementTransactionInput(
    Loan loan) implements CreateTransactionInput {

  private static final String LOAN_DISBURSEMENT = "Loan Disbursement";

  @Override
  public Transaction toTransaction(String requester) {
    final var userId = Id.of(requester);
    final var now = LocalDateTime.now();
    final var executor = requester;

    var currency = loan.currency() != null ? loan.currency() : Transaction.DEFAULT_CURRENCY;
    var transactionType = loan.type() == Loan.Type.BORROW ? Transaction.Type.CREDIT
        : Transaction.Type.DEBIT;

    var transactionBuilder = Transaction.builder()
        .id(Id.generate())
        .user(userId)
        .title(LOAN_DISBURSEMENT)
        .description(LOAN_DISBURSEMENT)
        .date(loan.date())
        .time(loan.time())
        .loan(loan.id())
        .amount(loan.amount())
        .currency(currency)
        .category(Transaction.Category.LOAN_DISBURSEMENT)
        .type(transactionType)
        .active(true)
        .createdAt(now)
        .updatedAt(now)
        .createdBy(executor)
        .updatedBy(executor);

    if (loan.type() == Loan.Type.BORROW) {
      transactionBuilder.destination(loan.account());
    } else {
      transactionBuilder.source(loan.account());
    }

    return transactionBuilder.build();
  }
}