package io.dkakunsi.bitapp.transaction.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Currency;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.dkakunsi.bitapp.Context;
import io.dkakunsi.bitapp.Id;
import io.dkakunsi.bitapp.Result.ErrorCode;
import io.dkakunsi.bitapp.Session.SessionManager;
import io.dkakunsi.bitapp.transaction.application.port.TransactionAccountPort;
import io.dkakunsi.bitapp.transaction.application.port.TransactionLoanPort;
import io.dkakunsi.bitapp.transaction.domain.entity.Transaction;
import io.dkakunsi.bitapp.transaction.domain.repository.TransactionRepository;

public final class RemoveTransactionUseCaseTest {

  private static final String REQUESTER = "user@email.com";

  private static final Context context = Context.builder().requester(REQUESTER).build();

  private static final String OTHER_USER = "other@email.com";
  private static final String ACCOUNT_ID_1 = "account-1";
  private static final Id ACCOUNT_1 = Id.of(ACCOUNT_ID_1);
  private static final String ACCOUNT_ID_2 = "account-2";
  private static final Id ACCOUNT_2 = Id.of(ACCOUNT_ID_2);
  private static final String LOAN_ID = "loan-1";
  private static final Id LOAN = Id.of(LOAN_ID);

  private RemoveTransaction underTest;

  private TransactionRepository transactionRepository;
  private TransactionAccountPort transactionAccountPort;
  private TransactionLoanPort transactionLoanPort;
  private SessionManager sessionManager;

  @BeforeEach
  void setUp() {
    transactionRepository = mock(TransactionRepository.class);
    transactionAccountPort = mock(TransactionAccountPort.class);
    transactionLoanPort = mock(TransactionLoanPort.class);
    sessionManager = mock(SessionManager.class);
    underTest = new RemoveTransaction(transactionRepository, transactionAccountPort, transactionLoanPort,
        sessionManager);

    when(sessionManager.executeInSession(any())).thenAnswer(invocation -> {
      var function = invocation.getArgument(0, java.util.function.Supplier.class);
      return function.get();
    });
  }

  @Test
  void returnNotFoundWhenTransactionDoesNotExist() {
    // Given
    var transactionId = "trans-404";
    var transactionIdObj = Id.of(transactionId);
    when(transactionRepository.findById(transactionIdObj)).thenReturn(Optional.empty());

    // When
    var result = Context.executeInContext(context, () -> underTest.process(transactionId));

    // Then
    assertFalse(result.isSuccess());
    assertTrue(result.errorCode().isPresent());
    assertEquals(ErrorCode.NOT_FOUND, result.errorCode().get());
    assertEquals("Transaction not found", result.errorMessage().get());
    verify(transactionRepository).findById(transactionIdObj);
  }

  @Test
  void returnNotFoundWhenTransactionBelongsToAnotherUser() {
    // Given
    var transactionId = "trans-other-user";
    var transactionIdObj = Id.of(transactionId);
    var transaction = baseTransaction(transactionId, OTHER_USER, Transaction.Type.DEBIT)
        .source(ACCOUNT_1)
        .amount(BigDecimal.valueOf(50000))
        .build();
    when(transactionRepository.findById(transactionIdObj)).thenReturn(Optional.of(transaction));

    // When
    var result = Context.executeInContext(context, () -> underTest.process(transactionId));

    // Then
    assertFalse(result.isSuccess());
    assertTrue(result.errorCode().isPresent());
    assertEquals(ErrorCode.NOT_FOUND, result.errorCode().get());
    verify(transactionRepository, never()).deleteById(transactionIdObj);
  }

  @Test
  void revertDebitTransactionAndDelete() {
    // Given
    var transactionId = "trans-debit";
    var transactionIdObj = Id.of(transactionId);
    var transaction = baseTransaction(transactionId, REQUESTER, Transaction.Type.DEBIT)
        .source(ACCOUNT_1)
        .amount(BigDecimal.valueOf(100000))
        .build();

    when(transactionRepository.findById(transactionIdObj)).thenReturn(Optional.of(transaction));

    // When
    var result = Context.executeInContext(context, () -> underTest.process(transactionId));

    // Then
    assertTrue(result.isSuccess());
    assertTrue(result.data().isPresent());
    assertEquals(transactionId, result.data().get().id());

    verify(transactionAccountPort).creditBalance(ACCOUNT_1, transaction.amount());
    verify(transactionRepository).deleteById(transactionIdObj);
  }

  @Test
  void revertCreditTransactionAndDelete() {
    // Given
    var transactionId = "trans-credit";
    var transactionIdObj = Id.of(transactionId);
    var transaction = baseTransaction(transactionId, REQUESTER, Transaction.Type.CREDIT)
        .destination(ACCOUNT_2)
        .amount(BigDecimal.valueOf(100000))
        .build();

    when(transactionRepository.findById(transactionIdObj)).thenReturn(Optional.of(transaction));

    // When
    var result = Context.executeInContext(context, () -> underTest.process(transactionId));

    // Then
    assertTrue(result.isSuccess());
    assertTrue(result.data().isPresent());
    assertEquals(transactionId, result.data().get().id());

    verify(transactionAccountPort).debitBalance(ACCOUNT_2, transaction.amount());
    verify(transactionRepository).deleteById(transactionIdObj);
  }

  @Test
  void revertTransferTransactionAndDelete() {
    // Given
    var transactionId = "trans-transfer";
    var transactionIdObj = Id.of(transactionId);
    var transaction = baseTransaction(transactionId, REQUESTER, Transaction.Type.TRANSFER)
        .source(ACCOUNT_1)
        .destination(ACCOUNT_2)
        .amount(BigDecimal.valueOf(200000))
        .build();

    when(transactionRepository.findById(transactionIdObj)).thenReturn(Optional.of(transaction));

    // When
    var result = Context.executeInContext(context, () -> underTest.process(transactionId));

    // Then
    assertTrue(result.isSuccess());
    assertTrue(result.data().isPresent());
    assertEquals(transactionId, result.data().get().id());

    verify(transactionAccountPort).creditBalance(ACCOUNT_1, transaction.amount());
    verify(transactionAccountPort).debitBalance(ACCOUNT_2, transaction.amount());
    verify(transactionRepository).deleteById(transactionIdObj);
  }

  @Test
  void revertLoanAndDelete() {
    // Given
    var transactionId = "trans-loan";
    var transactionIdObj = Id.of(transactionId);
    var transaction = baseTransaction(transactionId, REQUESTER, Transaction.Type.DEBIT)
        .source(ACCOUNT_1)
        .loan(LOAN)
        .amount(BigDecimal.valueOf(100000))
        .build();

    when(transactionRepository.findById(transactionIdObj)).thenReturn(Optional.of(transaction));

    // When
    var result = Context.executeInContext(context, () -> underTest.process(transactionId));

    // Then
    assertTrue(result.isSuccess());
    assertTrue(result.data().isPresent());
    assertEquals(transactionId, result.data().get().id());

    verify(transactionLoanPort).increaseRemainingAmount(LOAN, transaction.amount());
    verify(transactionRepository).deleteById(transactionIdObj);
  }

  private static Transaction.TransactionBuilder baseTransaction(String id, String user, Transaction.Type type) {
    return Transaction.builder()
        .id(Id.of(id))
        .user(Id.of(user))
        .title("Test Transaction")
        .description("Test Description")
        .date(LocalDate.of(2026, 1, 24))
        .time(LocalTime.of(10, 0))
        .currency(Currency.getInstance("IDR"))
        .category(Transaction.Category.OTHER)
        .type(type)
        .active(true)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .createdBy(user)
        .updatedBy(user);
  }

}
