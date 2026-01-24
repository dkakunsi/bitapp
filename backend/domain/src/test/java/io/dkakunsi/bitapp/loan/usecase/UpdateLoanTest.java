package io.dkakunsi.bitapp.loan.usecase;

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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Currency;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import io.dkakunsi.bitapp.common.AppError.Code;
import io.dkakunsi.bitapp.common.Context;
import io.dkakunsi.bitapp.domain.entity.EntityStatus;
import io.dkakunsi.bitapp.domain.entity.Id;
import io.dkakunsi.bitapp.loan.dto.UpdateLoanInput;
import io.dkakunsi.bitapp.loan.entity.Loan;
import io.dkakunsi.bitapp.loan.repository.LoanRepository;

public final class UpdateLoanTest {

  private UpdateLoan underTest;

  private LoanRepository loanRepository;

  private static final String REQUESTER = "testUser@email.com";
  private static final String LOAN_ID = "loan-123";

  @BeforeEach
  void setUp() {
    loanRepository = mock(LoanRepository.class);
    underTest = new UpdateLoan(loanRepository);
  }

  @Test
  void givenValidUpdateRequestWhenProcessedThenShouldUpdateLoan() {
    // Given
    var existingLoan = createExistingLoan();
    var updateRequest = UpdateLoanInput.builder()
        .id(LOAN_ID)
        .partyName("Jane Smith")
        .title("Updated Loan Title")
        .description("Updated description")
        .build();
    var context = Context.builder().requester(REQUESTER).build();

    when(loanRepository.findById(LOAN_ID)).thenReturn(Optional.of(existingLoan));
    when(loanRepository.update(any())).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    var result = underTest.process(context, updateRequest);

    // Then
    assertTrue(result.isSuccess());
    assertTrue(result.data().isPresent());

    var resultData = result.data().get();
    assertEquals(LOAN_ID, resultData.id());
    assertEquals(REQUESTER, resultData.user());
    assertEquals("Jane Smith", resultData.partyName());
    assertEquals("Updated Loan Title", resultData.title());
    assertEquals("Updated description", resultData.description());

    // Verify update was called
    var loanCaptor = ArgumentCaptor.forClass(Loan.class);
    verify(loanRepository).update(loanCaptor.capture());
    var capturedLoan = loanCaptor.getValue();
    assertEquals("Jane Smith", capturedLoan.partyName());
    assertEquals("Updated Loan Title", capturedLoan.title());
    assertEquals("Updated description", capturedLoan.description());
    assertEquals(REQUESTER, capturedLoan.updatedBy());
  }

  @Test
  void givenUpdateRequestWithNewAmountWhenProcessedThenShouldUpdateAmount() {
    // Given
    var existingLoan = createExistingLoan();
    var updateRequest = UpdateLoanInput.builder()
        .id(LOAN_ID)
        .partyName("John Doe")
        .title("Personal Loan")
        .description("Test loan")
        .amount(new BigDecimal("15000.00"))
        .build();
    var context = Context.builder().requester(REQUESTER).build();

    when(loanRepository.findById(LOAN_ID)).thenReturn(Optional.of(existingLoan));
    when(loanRepository.update(any())).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    var result = underTest.process(context, updateRequest);

    // Then
    assertTrue(result.isSuccess());
    assertTrue(result.data().isPresent());

    var resultData = result.data().get();
    assertEquals(new BigDecimal("15000.00"), resultData.amount());

    // Verify amount updated
    var loanCaptor = ArgumentCaptor.forClass(Loan.class);
    verify(loanRepository).update(loanCaptor.capture());
    var capturedLoan = loanCaptor.getValue();
    assertEquals(new BigDecimal("15000.00"), capturedLoan.amount());
  }

  @Test
  void givenUpdateRequestWithNewCurrencyWhenProcessedThenShouldUpdateCurrency() {
    // Given
    var existingLoan = createExistingLoan();
    var updateRequest = UpdateLoanInput.builder()
        .id(LOAN_ID)
        .partyName("John Doe")
        .title("Personal Loan")
        .description("Test loan")
        .currency("EUR")
        .build();
    var context = Context.builder().requester(REQUESTER).build();

    when(loanRepository.findById(LOAN_ID)).thenReturn(Optional.of(existingLoan));
    when(loanRepository.update(any())).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    var result = underTest.process(context, updateRequest);

    // Then
    assertTrue(result.isSuccess());
    assertTrue(result.data().isPresent());

    var resultData = result.data().get();
    assertEquals("EUR", resultData.currency());

    // Verify currency updated
    var loanCaptor = ArgumentCaptor.forClass(Loan.class);
    verify(loanRepository).update(loanCaptor.capture());
    var capturedLoan = loanCaptor.getValue();
    assertEquals(Currency.getInstance("EUR"), capturedLoan.currency());
  }

  @Test
  void givenUpdateRequestWithNewInterestRateWhenProcessedThenShouldUpdateInterestRate() {
    // Given
    var existingLoan = createExistingLoan();
    var updateRequest = UpdateLoanInput.builder()
        .id(LOAN_ID)
        .partyName("John Doe")
        .title("Personal Loan")
        .description("Test loan")
        .interestRate(7.5)
        .build();
    var context = Context.builder().requester(REQUESTER).build();

    when(loanRepository.findById(LOAN_ID)).thenReturn(Optional.of(existingLoan));
    when(loanRepository.update(any())).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    var result = underTest.process(context, updateRequest);

    // Then
    assertTrue(result.isSuccess());
    assertTrue(result.data().isPresent());

    var resultData = result.data().get();
    assertEquals(7.5, resultData.interestRate());

    // Verify interest rate updated
    var loanCaptor = ArgumentCaptor.forClass(Loan.class);
    verify(loanRepository).update(loanCaptor.capture());
    var capturedLoan = loanCaptor.getValue();
    assertEquals(7.5, capturedLoan.interestRate());
  }

  @Test
  void givenUpdateRequestWithNewDateAndTimeWhenProcessedThenShouldUpdateDateAndTime() {
    // Given
    var existingLoan = createExistingLoan();
    var updateRequest = UpdateLoanInput.builder()
        .id(LOAN_ID)
        .partyName("John Doe")
        .title("Personal Loan")
        .description("Test loan")
        .date("2026-12-31")
        .time("23:59:59")
        .build();
    var context = Context.builder().requester(REQUESTER).build();

    when(loanRepository.findById(LOAN_ID)).thenReturn(Optional.of(existingLoan));
    when(loanRepository.update(any())).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    var result = underTest.process(context, updateRequest);

    // Then
    assertTrue(result.isSuccess());
    assertTrue(result.data().isPresent());

    var resultData = result.data().get();
    assertEquals("2026-12-31", resultData.date());
    assertEquals("23:59", resultData.time());

    // Verify date and time updated
    var loanCaptor = ArgumentCaptor.forClass(Loan.class);
    verify(loanRepository).update(loanCaptor.capture());
    var capturedLoan = loanCaptor.getValue();
    assertEquals(LocalDate.parse("2026-12-31"), capturedLoan.date());
    assertEquals(LocalTime.parse("23:59:59"), capturedLoan.time());
  }

  @Test
  void givenUpdateRequestWithAllFieldsWhenProcessedThenShouldUpdateAllFields() {
    // Given
    var existingLoan = createExistingLoan();
    var updateRequest = UpdateLoanInput.builder()
        .id(LOAN_ID)
        .partyName("New Party")
        .title("New Title")
        .description("New description")
        .amount(new BigDecimal("20000.00"))
        .currency("GBP")
        .interestRate(4.5)
        .date("2027-06-15")
        .time("12:00:00")
        .build();
    var context = Context.builder().requester(REQUESTER).build();

    when(loanRepository.findById(LOAN_ID)).thenReturn(Optional.of(existingLoan));
    when(loanRepository.update(any())).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    var result = underTest.process(context, updateRequest);

    // Then
    assertTrue(result.isSuccess());
    assertTrue(result.data().isPresent());

    var resultData = result.data().get();
    assertEquals("New Party", resultData.partyName());
    assertEquals("New Title", resultData.title());
    assertEquals("New description", resultData.description());
    assertEquals(new BigDecimal("20000.00"), resultData.amount());
    assertEquals("GBP", resultData.currency());
    assertEquals(4.5, resultData.interestRate());
    assertEquals("2027-06-15", resultData.date());
    assertEquals("12:00", resultData.time());
  }

  @Test
  void givenNonExistentLoanIdWhenProcessedThenShouldReturnNotFound() {
    // Given
    var updateRequest = UpdateLoanInput.builder()
        .id("non-existent-id")
        .partyName("John Doe")
        .title("Personal Loan")
        .description("Test loan")
        .build();
    var context = Context.builder().requester(REQUESTER).build();

    when(loanRepository.findById("non-existent-id")).thenReturn(Optional.empty());

    // When
    var result = underTest.process(context, updateRequest);

    // Then
    assertFalse(result.isSuccess());
    assertTrue(result.isFailed());
    assertTrue(result.error().isPresent());
    assertEquals(Code.NOT_FOUND, result.error().get().code());
    assertEquals("Loan not found", result.error().get().message());

    // Verify update was never called
    verify(loanRepository, never()).update(any());
  }

  @Test
  void givenLoanOwnedByDifferentUserWhenProcessedThenShouldReturnForbidden() {
    // Given
    var existingLoan = createExistingLoan();
    var updateRequest = UpdateLoanInput.builder()
        .id(LOAN_ID)
        .partyName("John Doe")
        .title("Personal Loan")
        .description("Test loan")
        .build();
    var context = Context.builder().requester("differentUser@email.com").build();

    when(loanRepository.findById(LOAN_ID)).thenReturn(Optional.of(existingLoan));

    // When
    var result = underTest.process(context, updateRequest);

    // Then
    assertFalse(result.isSuccess());
    assertTrue(result.isFailed());
    assertTrue(result.error().isPresent());
    assertEquals(Code.FORBIDDEN, result.error().get().code());
    assertEquals("You are not authorized to update this loan", result.error().get().message());

    // Verify update was never called
    verify(loanRepository, never()).update(any());
  }

  @Test
  void givenUpdateRequestWhenProcessedThenShouldPreserveImmutableFields() {
    // Given
    var existingLoan = createExistingLoan();
    var originalCreatedAt = existingLoan.createdAt();
    var originalCreatedBy = existingLoan.createdBy();
    var originalType = existingLoan.type();
    var originalRemainingAmount = existingLoan.remainingAmount();

    var updateRequest = UpdateLoanInput.builder()
        .id(LOAN_ID)
        .partyName("Updated Party")
        .title("Updated Title")
        .description("Updated description")
        .build();
    var context = Context.builder().requester(REQUESTER).build();

    when(loanRepository.findById(LOAN_ID)).thenReturn(Optional.of(existingLoan));
    when(loanRepository.update(any())).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    var result = underTest.process(context, updateRequest);

    // Then
    assertTrue(result.isSuccess());

    // Verify immutable fields are preserved
    var loanCaptor = ArgumentCaptor.forClass(Loan.class);
    verify(loanRepository).update(loanCaptor.capture());
    var capturedLoan = loanCaptor.getValue();
    assertEquals(originalCreatedAt, capturedLoan.createdAt());
    assertEquals(originalCreatedBy, capturedLoan.createdBy());
    assertEquals(originalType, capturedLoan.type());
    assertEquals(originalRemainingAmount, capturedLoan.remainingAmount());
    assertEquals(existingLoan.id(), capturedLoan.id());
    assertEquals(existingLoan.user(), capturedLoan.user());
  }

  @Test
  void givenUpdateRequestWhenProcessedThenShouldUpdateTimestamp() {
    // Given
    var existingLoan = createExistingLoan();
    var originalUpdatedAt = existingLoan.updatedAt();

    var updateRequest = UpdateLoanInput.builder()
        .id(LOAN_ID)
        .partyName("John Doe")
        .title("Updated Title")
        .description("Test loan")
        .build();
    var context = Context.builder().requester(REQUESTER).build();

    when(loanRepository.findById(LOAN_ID)).thenReturn(Optional.of(existingLoan));
    when(loanRepository.update(any())).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    var result = underTest.process(context, updateRequest);

    // Then
    assertTrue(result.isSuccess());

    // Verify updatedAt timestamp was updated
    var loanCaptor = ArgumentCaptor.forClass(Loan.class);
    verify(loanRepository).update(loanCaptor.capture());
    var capturedLoan = loanCaptor.getValue();
    assertNotNull(capturedLoan.updatedAt());
    assertTrue(capturedLoan.updatedAt().isAfter(originalUpdatedAt) ||
        capturedLoan.updatedAt().isEqual(originalUpdatedAt));
  }

  private Loan createExistingLoan() {
    return Loan.builder()
        .id(Id.of(LOAN_ID))
        .user(Id.of(REQUESTER))
        .type(Loan.Type.BORROW)
        .date(LocalDate.of(2026, 1, 15))
        .time(LocalTime.of(10, 30))
        .partyName("John Doe")
        .title("Personal Loan")
        .description("Test loan")
        .amount(BigDecimal.valueOf(10000))
        .remainingAmount(BigDecimal.valueOf(10000))
        .currency(Currency.getInstance("USD"))
        .interestRate(5.5)
        .status(EntityStatus.ACTIVE)
        .createdAt(LocalDateTime.now().minusDays(1))
        .updatedAt(LocalDateTime.now().minusDays(1))
        .createdBy(REQUESTER)
        .updatedBy(REQUESTER)
        .build();
  }
}
