package io.dkakunsi.bitapp.loan.usecase;

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
import java.util.Currency;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import io.dkakunsi.bitapp.common.AppError.Code;
import io.dkakunsi.bitapp.common.Context;
import io.dkakunsi.bitapp.common.ModelStatus;
import io.dkakunsi.bitapp.loan.dto.CreateLoanInput;
import io.dkakunsi.bitapp.loan.entity.Loan;
import io.dkakunsi.bitapp.loan.repository.LoanRepository;

public final class CreateLoanTest {

  private CreateLoan underTest;

  private LoanRepository loanRepository;

  private static final String REQUESTER = "testUser";

  @BeforeEach
  void setUp() {
    loanRepository = mock(LoanRepository.class);
    underTest = new CreateLoan(loanRepository);
  }

  @Test
  void givenValidCreateLoanRequestWhenProcessedThenShouldSuccessfullyCreateLoan() {
    // Given
    final var createRequest = CreateLoanInput.builder()
        .type("BORROW")
        .date("2026-01-15")
        .time("14:30:00")
        .partyName("John Doe")
        .title("Personal Loan")
        .description("Emergency loan")
        .amount(new BigDecimal("5000.00"))
        .currency("USD")
        .interestRate(5.5)
        .build();
    final var context = Context.builder().requester(REQUESTER).build();

    when(loanRepository.create(any())).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    final var result = underTest.process(context, createRequest);

    // Then
    assertTrue(result.isSuccess());
    assertTrue(result.data().isPresent());

    // Verify returned data
    final var resultData = result.data().get();
    assertNotNull(resultData.id());
    assertEquals(REQUESTER, resultData.user());
    assertEquals("BORROW", resultData.type());
    assertEquals(LocalDate.parse("2026-01-15"), resultData.date());
    assertEquals(LocalTime.parse("14:30:00"), resultData.time());
    assertEquals("John Doe", resultData.partyName());
    assertEquals("Personal Loan", resultData.title());
    assertEquals("Emergency loan", resultData.description());
    assertEquals(new BigDecimal("5000.00"), resultData.amount());
    assertEquals(new BigDecimal("5000.00"), resultData.remainingAmount());
    assertEquals("USD", resultData.currency());
    assertEquals(5.5, resultData.interestRate());
    assertEquals("ACTIVE", resultData.status());
    assertNotNull(resultData.createdAt());
    assertNotNull(resultData.updatedAt());
    assertEquals(REQUESTER, resultData.createdBy());
    assertEquals(REQUESTER, resultData.updatedBy());

    // Verify data passed to repository
    var loanCaptor = ArgumentCaptor.forClass(Loan.class);
    verify(loanRepository).create(loanCaptor.capture());
    var capturedLoan = loanCaptor.getValue();
    assertNotNull(capturedLoan.id());
    assertEquals(REQUESTER, capturedLoan.user().value());
    assertEquals(Loan.Type.BORROW, capturedLoan.type());
    assertEquals(LocalDate.parse("2026-01-15"), capturedLoan.date());
    assertEquals(LocalTime.parse("14:30:00"), capturedLoan.time());
    assertEquals("John Doe", capturedLoan.partyName());
    assertEquals("Personal Loan", capturedLoan.title());
    assertEquals("Emergency loan", capturedLoan.description());
    assertEquals(new BigDecimal("5000.00"), capturedLoan.amount());
    assertEquals(new BigDecimal("5000.00"), capturedLoan.remainingAmount());
    assertEquals(Currency.getInstance("USD"), capturedLoan.currency());
    assertEquals(5.5, capturedLoan.interestRate());
    assertEquals(ModelStatus.ACTIVE, capturedLoan.status());
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
    final var context = Context.builder().requester(REQUESTER).build();

    when(loanRepository.create(any())).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    final var result = underTest.process(context, createRequest);

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
    final var context = Context.builder().requester(REQUESTER).build();

    when(loanRepository.create(any())).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    final var result = underTest.process(context, createRequest);

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
    final var context = Context.builder().requester(REQUESTER).build();

    when(loanRepository.create(any())).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    final var result = underTest.process(context, createRequest);

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
    final var context = Context.builder().requester(REQUESTER).build();

    when(loanRepository.create(any())).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    final var result = underTest.process(context, createRequest);

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
    final var context = Context.builder().requester(REQUESTER).build();

    // When
    final var result = underTest.process(context, createRequest);

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
    final var context = Context.builder().requester(REQUESTER).build();

    // When
    final var result = underTest.process(context, createRequest);

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
    final var context = Context.builder().requester(REQUESTER).build();

    // When
    final var result = underTest.process(context, createRequest);

    // Then
    assertFalse(result.isSuccess());
    assertTrue(result.error().isPresent());
    assertEquals(Code.BAD_REQUEST, result.error().get().code());
  }

  @Test
  void givenInvalidCreateLoanRequestWithInvalidDateFormatWhenProcessedThenShouldFail() {
    // Given
    final var createRequest = CreateLoanInput.builder()
        .type("BORROW")
        .date("invalid-date")
        .title("Test Loan")
        .amount(new BigDecimal("1000.00"))
        .interestRate(5.0)
        .build();
    final var context = Context.builder().requester(REQUESTER).build();

    // When
    final var result = underTest.process(context, createRequest);

    // Then
    assertFalse(result.isSuccess());
    assertTrue(result.error().isPresent());
    assertEquals(Code.BAD_REQUEST, result.error().get().code());
  }

  @Test
  void givenInvalidCreateLoanRequestWithInvalidTimeFormatWhenProcessedThenShouldFail() {
    // Given
    final var createRequest = CreateLoanInput.builder()
        .type("BORROW")
        .time("invalid-time")
        .title("Test Loan")
        .amount(new BigDecimal("1000.00"))
        .interestRate(5.0)
        .build();
    final var context = Context.builder().requester(REQUESTER).build();

    // When
    final var result = underTest.process(context, createRequest);

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
    final var context = Context.builder().requester(REQUESTER).build();

    when(loanRepository.create(any())).thenThrow(new RuntimeException("Database error"));

    // When
    final var result = underTest.process(context, createRequest);

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
    final var context = Context.builder().requester(REQUESTER).build();

    when(loanRepository.create(any())).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    final var result = underTest.process(context, createRequest);

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
    final var context = Context.builder().requester(REQUESTER).build();

    when(loanRepository.create(any())).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    final var result = underTest.process(context, createRequest);

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
}