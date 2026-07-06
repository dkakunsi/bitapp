package io.dkakunsi.bitapp.loan.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Currency;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import io.dkakunsi.bitapp.AppError.Code;
import io.dkakunsi.bitapp.Context;
import io.dkakunsi.bitapp.DateTimeConverter;
import io.dkakunsi.bitapp.EntityStatus;
import io.dkakunsi.bitapp.Id;
import io.dkakunsi.bitapp.SessionManager;
import io.dkakunsi.bitapp.account.domain.entity.Account;
import io.dkakunsi.bitapp.account.domain.repository.AccountRepository;
import io.dkakunsi.bitapp.loan.application.dto.CreateLoanInput;
import io.dkakunsi.bitapp.loan.domain.entity.Loan;
import io.dkakunsi.bitapp.loan.domain.repository.LoanRepository;
import io.dkakunsi.bitapp.transaction.application.usecase.CreateTransaction;
import io.dkakunsi.bitapp.transaction.domain.repository.TransactionRepository;

public final class CreateLoanTest {

  private static final String REQUESTER = "Requester";

  private static final Context context = Context.builder().requester(REQUESTER).build();

  private static final String ACCOUNT_ID = "account-123";

  private CreateLoan underTest;

  private LoanRepository loanRepository;
  private AccountRepository accountRepository;
  private TransactionRepository transactionRepository;
  private CreateTransaction createTransaction;
  private SessionManager sessionManager;

  @BeforeEach
  void setUp() {
    loanRepository = mock(LoanRepository.class);
    accountRepository = mock(AccountRepository.class);
    transactionRepository = mock(TransactionRepository.class);
    sessionManager = mock(SessionManager.class);
    createTransaction = new CreateTransaction(transactionRepository, accountRepository, loanRepository, sessionManager);
    underTest = new CreateLoan(loanRepository, accountRepository, sessionManager, createTransaction);

    when(sessionManager.executeInSession(any())).thenAnswer(invocation -> {
      var function = invocation.getArgument(0, java.util.function.Supplier.class);
      return function.get();
    });
  }

  @Test
  void givenValidCreateLoanRequestWhenProcessedThenShouldSuccessfullyCreateLoan() {
    // Given
    var date = DateTimeConverter.epochMilli(LocalDate.of(2026, 1, 15));
    var time = DateTimeConverter.minutesSinceMidnight(LocalTime.of(14, 30));
    final var createRequest = CreateLoanInput.builder()
        .type("BORROW")
        .date(date)
        .time(time)
        .partyName("John Doe")
        .title("Personal Loan")
        .description("Emergency loan")
        .amount(new BigDecimal("5000.00"))
        .currency("USD")
        .interestRate(5.5)
        .build();

    when(loanRepository.create(any())).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    final var result = Context.executeInContext(context, () -> underTest.process(createRequest));

    // Then
    assertTrue(result.isSuccess());
    assertTrue(result.data().isPresent());

    // Verify returned data
    final var resultData = result.data().get();
    assertNotNull(resultData.id());
    assertEquals(REQUESTER, resultData.user());
    assertEquals("BORROW", resultData.type());
    assertEquals(1768435200000L, resultData.date());
    assertEquals(870, resultData.time());
    assertEquals("John Doe", resultData.partyName());
    assertEquals("Personal Loan", resultData.title());
    assertEquals("Emergency loan", resultData.description());
    assertEquals(new BigDecimal("5000.00"), resultData.amount());
    assertEquals(new BigDecimal("5000.00"), resultData.remainingAmount());
    assertEquals("USD", resultData.currency());
    assertEquals(5.5, resultData.interestRate());

    // Verify data passed to repository
    var loanCaptor = ArgumentCaptor.forClass(Loan.class);
    verify(loanRepository).create(loanCaptor.capture());
    var capturedLoan = loanCaptor.getValue();
    assertNotNull(capturedLoan.id());
    assertEquals(REQUESTER, capturedLoan.user().value());
    assertEquals(Loan.Type.BORROW, capturedLoan.type());
    assertEquals(LocalDate.of(2026, 1, 15), capturedLoan.date());
    assertEquals(LocalTime.of(14, 30), capturedLoan.time());
    assertEquals("John Doe", capturedLoan.partyName());
    assertEquals("Personal Loan", capturedLoan.title());
    assertEquals("Emergency loan", capturedLoan.description());
    assertEquals(new BigDecimal("5000.00"), capturedLoan.amount());
    assertEquals(new BigDecimal("5000.00"), capturedLoan.remainingAmount());
    assertEquals(Currency.getInstance("USD"), capturedLoan.currency());
    assertEquals(5.5, capturedLoan.interestRate());
    assertEquals(EntityStatus.ACTIVE, capturedLoan.status());
    assertNotNull(capturedLoan.createdAt());
    assertNotNull(capturedLoan.updatedAt());
    assertEquals(REQUESTER, capturedLoan.createdBy());
    assertEquals(REQUESTER, capturedLoan.updatedBy());
  }

  @Test
  void givenCreateLoanRequestWithLendTypeWhenProcessedThenShouldSuccessfullyCreateLoan() {
    // Given
    final var createRequest = CreateLoanInput.builder()
        .type("LEND")
        .partyName("Jane Smith")
        .title("Business Loan")
        .amount(new BigDecimal("10000.00"))
        .interestRate(3.0)
        .build();

    when(loanRepository.create(any())).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    final var result = Context.executeInContext(context, () -> underTest.process(createRequest));

    // Then
    assertTrue(result.isSuccess());
    assertTrue(result.data().isPresent());

    final var resultData = result.data().get();
    assertEquals("LEND", resultData.type());
    assertEquals("Jane Smith", resultData.partyName());
    assertEquals("Business Loan", resultData.title());
    assertEquals(new BigDecimal("10000.00"), resultData.amount());
    assertEquals(3.0, resultData.interestRate());

    // Verify loan entity created with LEND type
    var loanCaptor = ArgumentCaptor.forClass(Loan.class);
    verify(loanRepository).create(loanCaptor.capture());
    var capturedLoan = loanCaptor.getValue();
    assertEquals(Loan.Type.LEND, capturedLoan.type());
  }

  @Test
  void givenCreateLoanRequestWithoutDateAndTimeWhenProcessedThenShouldUseCurrentDateAndTime() {
    // Given
    final var createRequest = CreateLoanInput.builder()
        .type("BORROW")
        .partyName("Test Party")
        .title("Test Loan")
        .amount(new BigDecimal("1000.00"))
        .interestRate(2.5)
        .build();

    when(loanRepository.create(any())).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    final var result = Context.executeInContext(context, () -> underTest.process(createRequest));

    // Then
    assertTrue(result.isSuccess());
    assertTrue(result.data().isPresent());

    final var resultData = result.data().get();
    assertNotNull(resultData.date());
    assertNotNull(resultData.time());

    // Verify date and time are set to current values
    var loanCaptor = ArgumentCaptor.forClass(Loan.class);
    verify(loanRepository).create(loanCaptor.capture());
    var capturedLoan = loanCaptor.getValue();
    assertNotNull(capturedLoan.date());
    assertNotNull(capturedLoan.time());
  }

  @Test
  void givenCreateLoanRequestWithoutCurrencyWhenProcessedThenShouldUseDefaultCurrency() {
    // Given
    final var createRequest = CreateLoanInput.builder()
        .type("BORROW")
        .partyName("Test Party")
        .title("Test Loan")
        .amount(new BigDecimal("2000.00"))
        .interestRate(4.0)
        .build();

    when(loanRepository.create(any())).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    final var result = Context.executeInContext(context, () -> underTest.process(createRequest));

    // Then
    assertTrue(result.isSuccess());
    assertTrue(result.data().isPresent());

    final var resultData = result.data().get();
    assertEquals("IDR", resultData.currency()); // Default currency is IDR

    // Verify default currency
    var loanCaptor = ArgumentCaptor.forClass(Loan.class);
    verify(loanRepository).create(loanCaptor.capture());
    var capturedLoan = loanCaptor.getValue();
    assertEquals(Currency.getInstance("IDR"), capturedLoan.currency());
  }

  @Test
  void givenCreateLoanRequestWithZeroInterestRateWhenProcessedThenShouldSucceed() {
    // Given
    final var createRequest = CreateLoanInput.builder()
        .type("LEND")
        .partyName("No Interest Party")
        .title("Interest-Free Loan")
        .amount(new BigDecimal("3000.00"))
        .interestRate(0.0)
        .build();

    when(loanRepository.create(any())).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    final var result = Context.executeInContext(context, () -> underTest.process(createRequest));

    // Then
    assertTrue(result.isSuccess());
    assertTrue(result.data().isPresent());

    final var resultData = result.data().get();
    assertEquals(0.0, resultData.interestRate());
  }

  @Test
  void givenInvalidCreateLoanRequestWithBlankTitleWhenProcessedThenShouldFail() {
    // Given
    final var createRequest = CreateLoanInput.builder()
        .type("BORROW")
        .title("")
        .amount(new BigDecimal("1000.00"))
        .interestRate(5.0)
        .build();

    // When
    final var result = Context.executeInContext(context, () -> underTest.process(createRequest));

    // Then
    assertFalse(result.isSuccess());
    assertTrue(result.error().isPresent());
    assertEquals(Code.BAD_REQUEST, result.error().get().code());
  }

  @Test
  void givenInvalidCreateLoanRequestWithInvalidTypeWhenProcessedThenShouldFail() {
    // Given
    final var createRequest = CreateLoanInput.builder()
        .type("INVALID_TYPE")
        .title("Test Loan")
        .amount(new BigDecimal("1000.00"))
        .interestRate(5.0)
        .build();

    // When
    final var result = Context.executeInContext(context, () -> underTest.process(createRequest));

    // Then
    assertFalse(result.isSuccess());
    assertTrue(result.error().isPresent());
    assertEquals(Code.BAD_REQUEST, result.error().get().code());
  }

  @Test
  void givenInvalidCreateLoanRequestWithNegativeAmountWhenProcessedThenShouldFail() {
    // Given
    final var createRequest = CreateLoanInput.builder()
        .type("BORROW")
        .title("Test Loan")
        .amount(new BigDecimal("-100.00"))
        .interestRate(5.0)
        .build();

    // When
    final var result = Context.executeInContext(context, () -> underTest.process(createRequest));

    // Then
    assertFalse(result.isSuccess());
    assertTrue(result.error().isPresent());
    assertEquals(Code.BAD_REQUEST, result.error().get().code());
  }

  @Test
  void givenValidCreateLoanRequestWhenRepositoryThrowsExceptionThenShouldFail() {
    // Given
    final var createRequest = CreateLoanInput.builder()
        .type("BORROW")
        .title("Test Loan")
        .amount(new BigDecimal("1000.00"))
        .interestRate(5.0)
        .build();

    when(loanRepository.create(any())).thenThrow(new RuntimeException("Database error"));

    // When
    final var result = Context.executeInContext(context, () -> underTest.process(createRequest));

    // Then
    assertFalse(result.isSuccess());
    assertTrue(result.error().isPresent());

    final var error = result.error().get();
    assertEquals(Code.SERVER_ERROR, error.code());
    assertEquals("Database error", error.message());
  }

  @Test
  void givenCreateLoanRequestWithDescriptionWhenProcessedThenDescriptionShouldBeStored() {
    // Given
    final var description = "This is a detailed description of the loan";
    final var createRequest = CreateLoanInput.builder()
        .type("BORROW")
        .title("Test Loan")
        .description(description)
        .amount(new BigDecimal("1500.00"))
        .interestRate(3.5)
        .build();

    when(loanRepository.create(any())).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    final var result = Context.executeInContext(context, () -> underTest.process(createRequest));

    // Then
    assertTrue(result.isSuccess());
    assertTrue(result.data().isPresent());

    final var resultData = result.data().get();
    assertEquals(description, resultData.description());

    var loanCaptor = ArgumentCaptor.forClass(Loan.class);
    verify(loanRepository).create(loanCaptor.capture());
    var capturedLoan = loanCaptor.getValue();
    assertEquals(description, capturedLoan.description());
  }

  @Test
  void givenCreateLoanRequestWhenProcessedThenRemainingAmountShouldEqualAmount() {
    // Given
    final var amount = new BigDecimal("7500.00");
    final var createRequest = CreateLoanInput.builder()
        .type("LEND")
        .title("Test Loan")
        .amount(amount)
        .interestRate(4.5)
        .build();

    when(loanRepository.create(any())).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    final var result = Context.executeInContext(context, () -> underTest.process(createRequest));

    // Then
    assertTrue(result.isSuccess());
    assertTrue(result.data().isPresent());

    final var resultData = result.data().get();
    assertEquals(amount, resultData.amount());
    assertEquals(amount, resultData.remainingAmount());

    var loanCaptor = ArgumentCaptor.forClass(Loan.class);
    verify(loanRepository).create(loanCaptor.capture());
    var capturedLoan = loanCaptor.getValue();
    assertEquals(amount, capturedLoan.amount());
    assertEquals(amount, capturedLoan.remainingAmount());
  }

  @Test
  void givenCreateBorrowLoanWithAccountWhenProcessedThenShouldCreateLoanAndCreditDisbursementTransaction() {
    // Given
    final var createRequest = CreateLoanInput.builder()
        .type("BORROW")
        .account(ACCOUNT_ID)
        .partyName("Bank XYZ")
        .title("Housing Loan")
        .amount(new BigDecimal("50000.00"))
        .interestRate(6.5)
        .build();

    var account = createAccount(ACCOUNT_ID, REQUESTER, new BigDecimal("10000.00"));
    when(accountRepository.findById(Id.of(ACCOUNT_ID))).thenReturn(Optional.of(account));
    when(loanRepository.create(any())).thenAnswer(invocation -> invocation.getArgument(0));
    when(transactionRepository.create(any())).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    final var result = Context.executeInContext(context, () -> underTest.process(createRequest));

    // Then
    assertTrue(result.isSuccess());
    assertTrue(result.data().isPresent());

    final var resultData = result.data().get();
    assertEquals("BORROW", resultData.type());
    assertEquals(ACCOUNT_ID, resultData.account());
    assertEquals(new BigDecimal("50000.00"), resultData.amount());

    // Verify loan was created
    verify(loanRepository).create(any(Loan.class));

    // Verify disbursement transaction was created with credit to account
    verify(transactionRepository).create(any());
    verify(accountRepository).creditBalance(Id.of(ACCOUNT_ID), new BigDecimal("50000.00"));
  }

  @Test
  void givenCreateLendLoanWithAccountWhenProcessedThenShouldCreateLoanAndDebitDisbursementTransaction() {
    // Given
    final var createRequest = CreateLoanInput.builder()
        .type("LEND")
        .account(ACCOUNT_ID)
        .partyName("John Borrower")
        .title("Personal Loan to Friend")
        .amount(new BigDecimal("5000.00"))
        .interestRate(3.0)
        .build();

    var account = createAccount(ACCOUNT_ID, REQUESTER, new BigDecimal("20000.00"));
    when(accountRepository.findById(Id.of(ACCOUNT_ID))).thenReturn(Optional.of(account));
    when(loanRepository.create(any())).thenAnswer(invocation -> invocation.getArgument(0));
    when(transactionRepository.create(any())).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    final var result = Context.executeInContext(context, () -> underTest.process(createRequest));

    // Then
    assertTrue(result.isSuccess());
    assertTrue(result.data().isPresent());

    final var resultData = result.data().get();
    assertEquals("LEND", resultData.type());
    assertEquals(ACCOUNT_ID, resultData.account());
    assertEquals(new BigDecimal("5000.00"), resultData.amount());

    // Verify loan was created
    verify(loanRepository).create(any(Loan.class));

    // Verify disbursement transaction was created with debit from account
    verify(transactionRepository).create(any());
    verify(accountRepository).debitBalance(Id.of(ACCOUNT_ID), new BigDecimal("5000.00"));
  }

  @Test
  void givenCreateLoanWithoutAccountWhenProcessedThenShouldCreateLoanWithoutDisbursementTransaction() {
    // Given
    final var createRequest = CreateLoanInput.builder()
        .type("BORROW")
        .partyName("Cash Lender")
        .title("Cash Loan")
        .amount(new BigDecimal("3000.00"))
        .interestRate(4.5)
        .build();

    when(loanRepository.create(any())).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    final var result = Context.executeInContext(context, () -> underTest.process(createRequest));

    // Then
    assertTrue(result.isSuccess());
    assertTrue(result.data().isPresent());

    final var resultData = result.data().get();
    assertEquals("BORROW", resultData.type());
    assertEquals(new BigDecimal("3000.00"), resultData.amount());

    // Verify loan was created
    verify(loanRepository).create(any(Loan.class));

    // Verify NO disbursement transaction was created
    verify(transactionRepository, never()).create(any());
    verify(accountRepository, never()).creditBalance(any(), any());
    verify(accountRepository, never()).debitBalance(any(), any());
  }

  @Test
  void givenCreateLoanWithNonExistentAccountWhenProcessedThenShouldReturnNotFoundError() {
    // Given
    final var nonExistentAccountId = "non-existent-account";
    final var createRequest = CreateLoanInput.builder()
        .type("BORROW")
        .account(nonExistentAccountId)
        .partyName("Bank XYZ")
        .title("Housing Loan")
        .amount(new BigDecimal("50000.00"))
        .interestRate(6.5)
        .build();

    when(accountRepository.findById(Id.of(nonExistentAccountId))).thenReturn(Optional.empty());

    // When
    final var result = Context.executeInContext(context, () -> underTest.process(createRequest));

    // Then
    assertFalse(result.isSuccess());
    assertTrue(result.error().isPresent());

    final var error = result.error().get();
    assertEquals(Code.NOT_FOUND, error.code());
    assertEquals("Account not found", error.message());

    // Verify loan and transaction were not created
    verify(loanRepository, never()).create(any());
    verify(transactionRepository, never()).create(any());
  }

  @Test
  void givenCreateLoanWithAccountOwnedByDifferentUserWhenProcessedThenShouldReturnForbiddenError() {
    // Given
    final var otherUser = "otherUser@example.com";
    final var createRequest = CreateLoanInput.builder()
        .type("BORROW")
        .account(ACCOUNT_ID)
        .partyName("Bank XYZ")
        .title("Housing Loan")
        .amount(new BigDecimal("50000.00"))
        .interestRate(6.5)
        .build();

    var account = createAccount(ACCOUNT_ID, otherUser, new BigDecimal("10000.00"));
    when(accountRepository.findById(Id.of(ACCOUNT_ID))).thenReturn(Optional.of(account));

    // When
    final var result = Context.executeInContext(context, () -> underTest.process(createRequest));

    // Then
    assertFalse(result.isSuccess());
    assertTrue(result.error().isPresent());

    final var error = result.error().get();
    assertEquals(Code.FORBIDDEN, error.code());
    assertEquals("You are not authorized to use this account", error.message());

    // Verify loan and transaction were not created
    verify(loanRepository, never()).create(any());
    verify(transactionRepository, never()).create(any());
  }

  @Test
  void givenCreateLoanWithAccountWhenDisbursementTransactionFailsThenShouldReturnError() {
    // Given
    final var createRequest = CreateLoanInput.builder()
        .type("BORROW")
        .account(ACCOUNT_ID)
        .partyName("Bank XYZ")
        .title("Housing Loan")
        .amount(new BigDecimal("50000.00"))
        .interestRate(6.5)
        .build();

    var account = createAccount(ACCOUNT_ID, REQUESTER, new BigDecimal("10000.00"));
    when(accountRepository.findById(Id.of(ACCOUNT_ID))).thenReturn(Optional.of(account));
    when(loanRepository.create(any())).thenAnswer(invocation -> invocation.getArgument(0));
    when(transactionRepository.create(any())).thenThrow(new RuntimeException("Transaction creation failed"));

    // When
    final var result = Context.executeInContext(context, () -> underTest.process(createRequest));

    // Then
    assertFalse(result.isSuccess());
    assertTrue(result.error().isPresent());

    final var error = result.error().get();
    assertEquals(Code.SERVER_ERROR, error.code());
    assertEquals("Transaction creation failed", error.message());
  }

  private Account createAccount(String id, String owner, BigDecimal balance) {
    return Account.builder()
        .id(Id.of(id))
        .user(Id.of(owner))
        .name("Test Account")
        .type(Account.Type.BANK)
        .balance(balance)
        .status(EntityStatus.ACTIVE)
        .createdBy(owner)
        .updatedBy(owner)
        .build();
  }
}