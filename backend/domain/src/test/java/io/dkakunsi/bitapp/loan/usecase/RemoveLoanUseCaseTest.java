package io.dkakunsi.bitapp.loan.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import io.dkakunsi.bitapp.common.AppError.Code;
import io.dkakunsi.bitapp.common.Context;
import io.dkakunsi.bitapp.domain.entity.EntityStatus;
import io.dkakunsi.bitapp.domain.entity.Id;
import io.dkakunsi.bitapp.loan.entity.Loan;
import io.dkakunsi.bitapp.loan.repository.LoanRepository;
import io.dkakunsi.bitapp.transaction.entity.Transaction;
import io.dkakunsi.bitapp.transaction.repository.TransactionRepository;

public final class RemoveLoanUseCaseTest {

  private static final String REQUESTER = "user@email.com";
  private static final String OTHER_USER = "other@email.com";
  private static final String LOAN_ID = "loan-123";
  private static final Id LOAN = Id.of(LOAN_ID);

  private RemoveLoan underTest;

  private LoanRepository loanRepository;
  private TransactionRepository transactionRepository;

  @BeforeEach
  void setUp() {
    loanRepository = mock(LoanRepository.class);
    transactionRepository = mock(TransactionRepository.class);
    underTest = new RemoveLoan(loanRepository, transactionRepository);
  }

  @Test
  void returnNotFoundWhenLoanDoesNotExist() {
    // Given
    when(loanRepository.findById(LOAN)).thenReturn(Optional.empty());

    // When
    var context = Context.builder().requester(REQUESTER).build();
    var result = underTest.process(context, LOAN_ID);

    // Then
    assertFalse(result.isSuccess());
    assertTrue(result.error().isPresent());
    assertEquals(Code.NOT_FOUND, result.error().get().code());
    assertEquals("Loan not found", result.error().get().message());
    verify(loanRepository).findById(LOAN);
  }

  @Test
  void returnForbiddenWhenLoanBelongsToAnotherUser() {
    // Given
    var loan = createLoan(LOAN_ID, OTHER_USER);
    when(loanRepository.findById(LOAN)).thenReturn(Optional.of(loan));

    // When
    var context = Context.builder().requester(REQUESTER).build();
    var result = underTest.process(context, LOAN_ID);

    // Then
    assertFalse(result.isSuccess());
    assertTrue(result.error().isPresent());
    assertEquals(Code.FORBIDDEN, result.error().get().code());
    verify(loanRepository, never()).deleteById(LOAN);
  }

  @Test
  void removeLoanShouldClearTransactionLoanReferences() {
    // Given
    var loan = createLoan(LOAN_ID, REQUESTER);
    var transaction1 = createTransaction("trans-1", REQUESTER, LOAN_ID);
    var transaction2 = createTransaction("trans-2", REQUESTER, LOAN_ID);

    when(loanRepository.findById(LOAN)).thenReturn(Optional.of(loan));
    when(transactionRepository.findByLoanId(LOAN)).thenReturn(List.of(transaction1, transaction2));

    // When
    var context = Context.builder().requester(REQUESTER).build();
    var result = underTest.process(context, LOAN_ID);

    // Then
    assertTrue(result.isSuccess());
    assertTrue(result.data().isPresent());
    assertEquals(LOAN_ID, result.data().get().id());

    var transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
    verify(transactionRepository, org.mockito.Mockito.times(2)).update(transactionCaptor.capture());
    for (var updatedTransaction : transactionCaptor.getAllValues()) {
      assertNotNull(updatedTransaction);
      assertEquals(null, updatedTransaction.loan());
      assertEquals(REQUESTER, updatedTransaction.updatedBy());
    }

    verify(loanRepository).deleteById(LOAN);
  }

  private static Loan createLoan(String id, String user) {
    return Loan.builder()
        .id(Id.of(id))
        .user(Id.of(user))
        .type(Loan.Type.BORROW)
        .date(LocalDate.of(2026, 1, 24))
        .time(LocalTime.of(10, 0))
        .partyName("Bank")
        .title("Test Loan")
        .description("Test")
        .amount(BigDecimal.valueOf(1000000))
        .remainingAmount(BigDecimal.valueOf(1000000))
        .currency(Currency.getInstance("IDR"))
        .interestRate(5.0)
        .status(EntityStatus.ACTIVE)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .createdBy(user)
        .updatedBy(user)
        .build();
  }

  private static Transaction createTransaction(String id, String user, String loanId) {
    return Transaction.builder()
        .id(Id.of(id))
        .user(Id.of(user))
        .title("Payment")
        .description("Loan repayment")
        .date(LocalDate.of(2026, 1, 24))
        .time(LocalTime.of(11, 0))
        .source(Id.of("account-1"))
        .loan(Id.of(loanId))
        .amount(BigDecimal.valueOf(100000))
        .currency(Currency.getInstance("IDR"))
        .category(Transaction.Category.LOAN)
        .type(Transaction.Type.DEBIT)
        .status(EntityStatus.ACTIVE)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .createdBy(user)
        .updatedBy(user)
        .build();
  }
}
