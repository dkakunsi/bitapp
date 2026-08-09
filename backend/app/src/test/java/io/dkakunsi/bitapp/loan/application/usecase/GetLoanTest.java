package io.dkakunsi.bitapp.loan.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Currency;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.dkakunsi.bitapp.Context;
import io.dkakunsi.bitapp.Id;
import io.dkakunsi.bitapp.Result;
import io.dkakunsi.bitapp.Result.ErrorCode;
import io.dkakunsi.bitapp.loan.domain.entity.Loan;
import io.dkakunsi.bitapp.loan.domain.repository.LoanRepository;

public final class GetLoanTest {

  private static final String REQUESTER = "Requester";

  private static final Context context = Context.builder().requester(REQUESTER).build();

  private GetLoan underTest;

  private LoanRepository loanRepository;

  @BeforeEach
  void setUp() {
    loanRepository = mock(LoanRepository.class);
    underTest = new GetLoan(loanRepository);
  }

  @Test
  void returnLoanData_whenLoanExists() {
    // Given
    var loanId = "loan-123";
    var loanIdObj = Id.of(loanId);
    var existingLoan = Loan.builder()
        .id(Id.of(loanId))
        .user(Id.of("user@email.com"))
        .account(Id.of("account-456"))
        .type(Loan.Type.BORROW)
        .date(LocalDate.of(2026, 1, 15))
        .time(LocalTime.of(10, 30))
        .partyName("John Doe")
        .title("Personal Loan")
        .description("Loan for personal use")
        .amount(BigDecimal.valueOf(10000))
        .remainingAmount(BigDecimal.valueOf(8000))
        .currency(Currency.getInstance("IDR"))
        .interestRate(5.5)
        .build();
    when(loanRepository.findById(loanIdObj)).thenReturn(Optional.of(existingLoan));

    // When
    var result = Context.executeInContext(context, () -> underTest.process(loanId));

    // Then
    assertTrue(result.isSuccess());
    assertTrue(result.data().isPresent());
    var loan = result.data().get();
    assertEquals(loanId, loan.id());
    assertEquals("user@email.com", loan.user());
    assertEquals("account-456", loan.account());
    assertEquals("BORROW", loan.type());
    assertEquals(1768435200000L, loan.date());
    assertEquals(630, loan.time());
    assertEquals("John Doe", loan.partyName());
    assertEquals("Personal Loan", loan.title());
    assertEquals("Loan for personal use", loan.description());
    assertEquals(BigDecimal.valueOf(10000), loan.amount());
    assertEquals(BigDecimal.valueOf(8000), loan.remainingAmount());
    assertEquals("IDR", loan.currency());
    assertEquals(5.5, loan.interestRate());
    verify(loanRepository).findById(loanIdObj);
  }

  @Test
  void returnError_whenLoanNotExists() {
    // Given
    var loanId = "nonexistent-loan";
    var loanIdObj = Id.of(loanId);
    when(loanRepository.findById(loanIdObj)).thenReturn(Optional.empty());

    // When
    var result = Context.executeInContext(context, () -> underTest.process(loanId));

    // Then
    assertFalse(result.isSuccess());
    assertTrue(result.errorCode().isPresent());
    assertEquals(ErrorCode.NOT_FOUND, result.errorCode().get());
    assertEquals("Loan not found", result.errorMessage().get());

    verify(loanRepository).findById(loanIdObj);
  }

  @Test
  void returnServerError_whenRepositoryThrowsException() {
    // Given
    var loanId = "error-loan";
    var loanIdObj = Id.of(loanId);
    when(loanRepository.findById(loanIdObj)).thenThrow(new RuntimeException("Database error"));

    // When
    var result = Context.executeInContext(context, () -> underTest.process(loanId));

    // Then
    assertFalse(result.isSuccess());
    assertTrue(result.errorCode().isPresent());
    assertEquals(ErrorCode.INTERNAL_ERROR, result.errorCode().get());
    assertEquals(Result.DEFAULT_ERROR_MESSAGE, result.errorMessage().get());

    verify(loanRepository).findById(loanIdObj);
  }
}
