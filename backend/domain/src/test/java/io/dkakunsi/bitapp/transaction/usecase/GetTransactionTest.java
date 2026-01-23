package io.dkakunsi.bitapp.transaction.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.dkakunsi.bitapp.common.AppError.Code;
import io.dkakunsi.bitapp.common.Context;
import io.dkakunsi.bitapp.common.EntityStatus;
import io.dkakunsi.bitapp.common.Id;
import io.dkakunsi.bitapp.transaction.entity.Transaction;
import io.dkakunsi.bitapp.transaction.repository.TransactionRepository;

public final class GetTransactionTest {

  private static final String REQUESTER = "user@email.com";
  private static final String OTHER_USER = "other@email.com";

  private GetTransaction underTest;

  private TransactionRepository transactionRepository;

  @BeforeEach
  void setUp() {
    transactionRepository = mock(TransactionRepository.class);
    underTest = new GetTransaction(transactionRepository);
  }

  @Test
  void returnTransactionData_whenTransactionExistsAndUserMatches() {
    // Given
    var transactionId = "trans-123";
    var existingTransaction = Transaction.builder()
        .id(Id.of(transactionId))
        .user(Id.of(REQUESTER))
        .title("Grocery Shopping")
        .description("Weekly groceries")
        .date(LocalDate.of(2026, 1, 23))
        .time(LocalTime.of(14, 30))
        .source(Id.of("account-1"))
        .destination(null)
        .loan(null)
        .amount(150000L)
        .currency("IDR")
        .category(Transaction.Category.FOOD)
        .type(Transaction.Type.DEBIT)
        .status(EntityStatus.ACTIVE)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .createdBy(REQUESTER)
        .updatedBy(REQUESTER)
        .build();
    when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(existingTransaction));

    // When
    var context = Context.builder().requester(REQUESTER).build();
    var result = underTest.process(context, transactionId);

    // Then
    assertTrue(result.isSuccess());
    assertTrue(result.data().isPresent());
    var transaction = result.data().get();
    assertEquals(transactionId, transaction.id());
    assertEquals(REQUESTER, transaction.user());
    assertEquals("DEBIT", transaction.type());
    assertEquals("Grocery Shopping", transaction.title());
    assertEquals("Weekly groceries", transaction.description());
    assertEquals("account-1", transaction.source());
    assertEquals(150000L, transaction.amount());
    assertEquals("IDR", transaction.currency());
    assertEquals("FOOD", transaction.category());
    verify(transactionRepository).findById(transactionId);
  }

  @Test
  void returnTransactionData_whenCreditTransactionExists() {
    // Given
    var transactionId = "trans-456";
    var existingTransaction = Transaction.builder()
        .id(Id.of(transactionId))
        .user(Id.of(REQUESTER))
        .title("Salary Payment")
        .description("Monthly salary")
        .date(LocalDate.of(2026, 1, 23))
        .time(LocalTime.of(9, 0))
        .source(null)
        .destination(Id.of("account-2"))
        .loan(null)
        .amount(5000000L)
        .currency("IDR")
        .category(Transaction.Category.INCOME)
        .type(Transaction.Type.CREDIT)
        .status(EntityStatus.ACTIVE)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .createdBy(REQUESTER)
        .updatedBy(REQUESTER)
        .build();
    when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(existingTransaction));

    // When
    var context = Context.builder().requester(REQUESTER).build();
    var result = underTest.process(context, transactionId);

    // Then
    assertTrue(result.isSuccess());
    assertTrue(result.data().isPresent());
    var transaction = result.data().get();
    assertEquals(transactionId, transaction.id());
    assertEquals("CREDIT", transaction.type());
    assertEquals("Salary Payment", transaction.title());
    assertEquals("account-2", transaction.destination());
    assertEquals(5000000L, transaction.amount());
    assertEquals("INCOME", transaction.category());
    verify(transactionRepository).findById(transactionId);
  }

  @Test
  void returnTransactionData_whenTransferTransactionExists() {
    // Given
    var transactionId = "trans-789";
    var existingTransaction = Transaction.builder()
        .id(Id.of(transactionId))
        .user(Id.of(REQUESTER))
        .title("Internal Transfer")
        .description("Moving funds")
        .date(LocalDate.of(2026, 1, 23))
        .time(LocalTime.of(15, 45))
        .source(Id.of("account-1"))
        .destination(Id.of("account-2"))
        .loan(null)
        .amount(200000L)
        .currency("IDR")
        .category(null)
        .type(Transaction.Type.TRANSFER)
        .status(EntityStatus.ACTIVE)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .createdBy(REQUESTER)
        .updatedBy(REQUESTER)
        .build();
    when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(existingTransaction));

    // When
    var context = Context.builder().requester(REQUESTER).build();
    var result = underTest.process(context, transactionId);

    // Then
    assertTrue(result.isSuccess());
    assertTrue(result.data().isPresent());
    var transaction = result.data().get();
    assertEquals(transactionId, transaction.id());
    assertEquals("TRANSFER", transaction.type());
    assertEquals("account-1", transaction.source());
    assertEquals("account-2", transaction.destination());
    verify(transactionRepository).findById(transactionId);
  }

  @Test
  void returnTransactionData_whenTransactionWithLoanExists() {
    // Given
    var transactionId = "trans-loan";
    var loanId = "loan-123";
    var existingTransaction = Transaction.builder()
        .id(Id.of(transactionId))
        .user(Id.of(REQUESTER))
        .title("Loan Payment")
        .description("Monthly loan payment")
        .date(LocalDate.of(2026, 1, 23))
        .time(LocalTime.of(11, 0))
        .source(Id.of("account-1"))
        .destination(null)
        .loan(Id.of(loanId))
        .amount(100000L)
        .currency("IDR")
        .category(Transaction.Category.LOAN)
        .type(Transaction.Type.DEBIT)
        .status(EntityStatus.ACTIVE)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .createdBy(REQUESTER)
        .updatedBy(REQUESTER)
        .build();
    when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(existingTransaction));

    // When
    var context = Context.builder().requester(REQUESTER).build();
    var result = underTest.process(context, transactionId);

    // Then
    assertTrue(result.isSuccess());
    assertTrue(result.data().isPresent());
    var transaction = result.data().get();
    assertEquals(transactionId, transaction.id());
    assertEquals(loanId, transaction.loan());
    assertEquals("LOAN", transaction.category());
    verify(transactionRepository).findById(transactionId);
  }

  @Test
  void returnNotFound_whenTransactionNotExists() {
    // Given
    var transactionId = "nonexistent-transaction";
    when(transactionRepository.findById(transactionId)).thenReturn(Optional.empty());

    // When
    var context = Context.builder().requester(REQUESTER).build();
    var result = underTest.process(context, transactionId);

    // Then
    assertFalse(result.isSuccess());
    assertTrue(result.error().isPresent());
    var error = result.error().get();
    assertEquals(Code.NOT_FOUND, error.code());
    assertEquals("Transaction not found", error.message());
    verify(transactionRepository).findById(transactionId);
  }

  @Test
  void returnNotFound_whenTransactionBelongsToOtherUser() {
    // Given
    var transactionId = "trans-other-user";
    var otherUserTransaction = Transaction.builder()
        .id(Id.of(transactionId))
        .user(Id.of(OTHER_USER))
        .title("Other User Transaction")
        .description("Should not be accessible")
        .date(LocalDate.of(2026, 1, 23))
        .time(LocalTime.of(10, 0))
        .source(Id.of("account-other"))
        .destination(null)
        .loan(null)
        .amount(50000L)
        .currency("IDR")
        .category(Transaction.Category.OTHER)
        .type(Transaction.Type.DEBIT)
        .status(EntityStatus.ACTIVE)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .createdBy(OTHER_USER)
        .updatedBy(OTHER_USER)
        .build();
    when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(otherUserTransaction));

    // When
    var context = Context.builder().requester(REQUESTER).build();
    var result = underTest.process(context, transactionId);

    // Then
    assertFalse(result.isSuccess());
    assertTrue(result.error().isPresent());
    var error = result.error().get();
    assertEquals(Code.NOT_FOUND, error.code());
    assertEquals("Transaction not found", error.message());
    verify(transactionRepository).findById(transactionId);
  }

  @Test
  void returnServerError_whenRepositoryThrowsException() {
    // Given
    var transactionId = "error-transaction";
    when(transactionRepository.findById(transactionId)).thenThrow(new RuntimeException("Database error"));

    // When
    var context = Context.builder().requester(REQUESTER).build();
    var result = underTest.process(context, transactionId);

    // Then
    assertFalse(result.isSuccess());
    assertTrue(result.error().isPresent());
    var error = result.error().get();
    assertEquals(Code.SERVER_ERROR, error.code());
    assertEquals("Database error", error.message());
    verify(transactionRepository).findById(transactionId);
  }
}
