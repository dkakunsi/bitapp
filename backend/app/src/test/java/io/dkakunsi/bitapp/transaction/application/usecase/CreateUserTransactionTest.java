package io.dkakunsi.bitapp.transaction.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import io.dkakunsi.bitapp.Context;
import io.dkakunsi.bitapp.DateTimeConverter;
import io.dkakunsi.bitapp.Id;
import io.dkakunsi.bitapp.Result.ErrorCode;
import io.dkakunsi.bitapp.Session.SessionManager;
import io.dkakunsi.bitapp.transaction.application.dto.CreateUserTransactionInput;
import io.dkakunsi.bitapp.transaction.application.port.TransactionAccountPort;
import io.dkakunsi.bitapp.transaction.application.port.TransactionLoanPort;
import io.dkakunsi.bitapp.transaction.domain.repository.TransactionRepository;

public final class CreateUserTransactionTest {

  private static final String REQUESTER = "test@email.com";

  private static final Context context = Context.builder().requester(REQUESTER).build();

  private CreateTransaction underTest;

  private TransactionRepository transactionRepository;
  private TransactionAccountPort transactionAccountPort;
  private TransactionLoanPort transactionLoanPort;
  private SessionManager sessionManager;

  private static final String ACCOUNT_ID_1 = "account-1";
  private static final Id ACCOUNT_1 = Id.of(ACCOUNT_ID_1);
  private static final String ACCOUNT_ID_2 = "account-2";
  private static final Id ACCOUNT_2 = Id.of(ACCOUNT_ID_2);
  private static final String LOAN_ID = "loan-1";

  @BeforeEach
  void setUp() {
    transactionRepository = mock(TransactionRepository.class);
    transactionAccountPort = mock(TransactionAccountPort.class);
    transactionLoanPort = mock(TransactionLoanPort.class);
    sessionManager = mock(SessionManager.class);
    underTest = new CreateTransaction(transactionRepository, transactionAccountPort, transactionLoanPort,
        sessionManager);

    when(transactionAccountPort.isExistingAccount(any())).thenReturn(true);
    when(transactionLoanPort.isExistingLoan(any())).thenReturn(true);

    when(sessionManager.executeInSession(any())).thenAnswer(invocation -> {
      var function = invocation.getArgument(0, java.util.function.Supplier.class);
      return function.get();
    });
  }

  @Test
  void givenValidDebitTransactionWhenProcessedThenShouldSuccessfullyCreate() {
    // Given
    var createRequest = CreateUserTransactionInput.builder()
        .title("Grocery Shopping")
        .description("Monthly groceries")
        .date(DateTimeConverter.epochMilli(LocalDate.now()))
        .time(DateTimeConverter.minutesSinceMidnight(LocalTime.now()))
        .source(ACCOUNT_ID_1)
        .amount(BigDecimal.valueOf(50000))
        .currency("IDR")
        .category("FOOD")
        .type("DEBIT")
        .build();

    when(transactionRepository.create(any())).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    var result = Context.executeInContext(context, () -> underTest.process(createRequest));

    // Then
    assertTrue(result.isSuccess());
    assertTrue(result.data().isPresent());

    var resultData = result.data().get();
    assertNotNull(resultData.id());
    assertEquals(createRequest.title(), resultData.title());
    assertEquals(createRequest.amount(), resultData.amount());
    assertEquals("DEBIT", resultData.type());

    verify(transactionAccountPort).debitBalance(ACCOUNT_1, BigDecimal.valueOf(50000));
  }

  @Test
  void givenValidCreditTransactionWhenProcessedThenShouldSuccessfullyCreate() {
    // Given
    var createRequest = CreateUserTransactionInput.builder()
        .title("Salary")
        .description("Monthly salary")
        .date(DateTimeConverter.epochMilli(LocalDate.now()))
        .time(DateTimeConverter.minutesSinceMidnight(LocalTime.now()))
        .destination(ACCOUNT_ID_1)
        .amount(BigDecimal.valueOf(5000000))
        .currency("IDR")
        .category("SALARY")
        .type("CREDIT")
        .build();

    when(transactionRepository.create(any())).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    var result = Context.executeInContext(context, () -> underTest.process(createRequest));

    // Then
    assertTrue(result.isSuccess());
    assertTrue(result.data().isPresent());

    var resultData = result.data().get();
    assertEquals(createRequest.title(), resultData.title());
    assertEquals("CREDIT", resultData.type());

    verify(transactionAccountPort).creditBalance(ACCOUNT_1, BigDecimal.valueOf(5000000));
  }

  @Test
  void givenValidTransferTransactionWhenProcessedThenShouldSuccessfullyCreate() {
    // Given
    var createRequest = CreateUserTransactionInput.builder()
        .title("Transfer to Savings")
        .description("Monthly savings")
        .date(DateTimeConverter.epochMilli(LocalDate.now()))
        .time(DateTimeConverter.minutesSinceMidnight(LocalTime.now()))
        .source(ACCOUNT_ID_1)
        .destination(ACCOUNT_ID_2)
        .amount(BigDecimal.valueOf(100000))
        .currency("IDR")
        .category("OTHER")
        .type("TRANSFER")
        .build();

    when(transactionRepository.create(any())).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    var result = Context.executeInContext(context, () -> underTest.process(createRequest));

    // Then
    assertTrue(result.isSuccess());
    assertTrue(result.data().isPresent());

    var resultData = result.data().get();
    assertEquals(createRequest.title(), resultData.title());
    assertEquals("TRANSFER", resultData.type());

    verify(transactionAccountPort).debitBalance(ACCOUNT_1, BigDecimal.valueOf(100000));
    verify(transactionAccountPort).creditBalance(ACCOUNT_2, BigDecimal.valueOf(100000));
  }

  @Test
  void givenTransactionWithLoanWhenProcessedThenShouldUpdateLoanRemainingAmount() {
    // Given
    var createRequest = CreateUserTransactionInput.builder()
        .title("Loan Payment")
        .description("Monthly loan payment")
        .date(DateTimeConverter.epochMilli(LocalDate.now()))
        .time(DateTimeConverter.minutesSinceMidnight(LocalTime.now()))
        .source(ACCOUNT_ID_1)
        .loan(LOAN_ID)
        .amount(BigDecimal.valueOf(100000))
        .currency("IDR")
        .category("LOAN")
        .type("DEBIT")
        .build();

    when(transactionRepository.create(any())).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    var result = Context.executeInContext(context, () -> underTest.process(createRequest));

    // Then
    assertTrue(result.isSuccess());
    assertTrue(result.data().isPresent());

    var transactionCaptor = ArgumentCaptor.forClass(Id.class);
    verify(transactionLoanPort).decreaseRemainingAmount(transactionCaptor.capture(), any());
  }

  @Test
  void givenDebitTransactionWithNonExistentSourceAccountThenShouldReturnNotFoundError() {
    // Given
    var nonExistentAccountId = "non-existent-account";
    var createRequest = CreateUserTransactionInput.builder()
        .title("Transaction")
        .source(nonExistentAccountId)
        .amount(BigDecimal.valueOf(50000))
        .category("FOOD")
        .type("DEBIT")
        .build();

    when(transactionRepository.create(any())).thenAnswer(invocation -> invocation.getArgument(0));
    when(transactionAccountPort.isExistingAccount(Id.of(nonExistentAccountId))).thenReturn(false);

    // When
    var result = Context.executeInContext(context, () -> underTest.process(createRequest));

    // Then
    assertFalse(result.isSuccess());
    assertTrue(result.errorCode().isPresent());
    assertEquals(ErrorCode.BAD_REQUEST, result.errorCode().get());
    assertEquals("source account not found", result.errorMessage().get());
  }

  @Test
  void givenCreditTransactionWithNonExistentDestinationAccountThenShouldReturnNotFoundError() {
    // Given
    var nonExistentAccountId = "non-existent-account";
    var createRequest = CreateUserTransactionInput.builder()
        .title("Transaction")
        .destination(nonExistentAccountId)
        .amount(BigDecimal.valueOf(50000))
        .category("SALARY")
        .type("CREDIT")
        .build();

    when(transactionRepository.create(any())).thenAnswer(invocation -> invocation.getArgument(0));
    when(transactionAccountPort.isExistingAccount(Id.of(nonExistentAccountId))).thenReturn(false);

    // When
    var result = Context.executeInContext(context, () -> underTest.process(createRequest));

    // Then
    assertFalse(result.isSuccess());
    assertTrue(result.errorCode().isPresent());
    assertEquals(ErrorCode.BAD_REQUEST, result.errorCode().get());
    assertEquals("destination account not found", result.errorMessage().get());
  }

  @Test
  void givenTransactionWithNonExistentLoanThenShouldReturnNotFoundError() {
    // Given
    var nonExistentLoanId = "non-existent-loan";
    var createRequest = CreateUserTransactionInput.builder()
        .title("Loan Payment")
        .source(ACCOUNT_ID_1)
        .loan(nonExistentLoanId)
        .amount(BigDecimal.valueOf(100000))
        .category("LOAN")
        .type("DEBIT")
        .build();

    when(transactionRepository.create(any())).thenAnswer(invocation -> invocation.getArgument(0));
    when(transactionLoanPort.isExistingLoan(Id.of(nonExistentLoanId))).thenReturn(false);

    // When
    var result = Context.executeInContext(context, () -> underTest.process(createRequest));

    // Then
    assertFalse(result.isSuccess());
    assertTrue(result.errorCode().isPresent());
    assertEquals(ErrorCode.BAD_REQUEST, result.errorCode().get());
    assertEquals("loan not found", result.errorMessage().get());
  }

  @Test
  void givenRepositoryErrorWhenCreatingTransactionThenShouldReturnServerError() {
    // Given
    var createRequest = CreateUserTransactionInput.builder()
        .title("Transaction")
        .source(ACCOUNT_ID_1)
        .amount(BigDecimal.valueOf(50000))
        .category("FOOD")
        .type("DEBIT")
        .build();

    when(transactionRepository.create(any())).thenThrow(new RuntimeException("Database error"));

    // When
    var result = Context.executeInContext(context, () -> underTest.process(createRequest));

    // Then
    assertFalse(result.isSuccess());
    assertTrue(result.errorCode().isPresent());
    assertEquals(ErrorCode.INTERNAL_ERROR, result.errorCode().get());
    assertEquals("Database error", result.errorMessage().get());
  }
}
