package io.dkakunsi.bitapp.loan.usecase;

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

import io.dkakunsi.bitapp.common.AppError.Code;
import io.dkakunsi.bitapp.common.Context;
import io.dkakunsi.bitapp.domain.entity.Id;
import io.dkakunsi.bitapp.loan.entity.Loan;
import io.dkakunsi.bitapp.loan.repository.LoanRepository;

public final class GetLoanTest {

  private static final String REQUESTER = "Requester";

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
    var context = Context.builder().requester(REQUESTER).build();
    var result = underTest.process(context, loanId);

    // Then
    assertTrue(result.isSuccess());
    assertTrue(result.data().isPresent());
    var loan = result.data().get();
    assertEquals(loanId, loan.id());
    assertEquals("user@email.com", loan.user());
    assertEquals("BORROW", loan.type());
    assertEquals("2026-01-15", loan.date());
    assertEquals("10:30", loan.time());
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
    var context = Context.builder().requester(REQUESTER).build();
    var result = underTest.process(context, loanId);

    // Then
    assertFalse(result.isSuccess());
    assertTrue(result.error().isPresent());
    var error = result.error().get();
    assertEquals(Code.NOT_FOUND, error.code());
    assertEquals("Loan not found", error.message());

    verify(loanRepository).findById(loanIdObj);
  }

  @Test
  void returnServerError_whenRepositoryThrowsException() {
    // Given
    var loanId = "error-loan";
    var loanIdObj = Id.of(loanId);
    when(loanRepository.findById(loanIdObj)).thenThrow(new RuntimeException("Database error"));

    // When
    var context = Context.builder().requester(REQUESTER).build();
    var result = underTest.process(context, loanId);

    // Then
    assertFalse(result.isSuccess());
    assertTrue(result.error().isPresent());
    var error = result.error().get();
    assertEquals(Code.SERVER_ERROR, error.code());
    assertEquals("Database error", error.message());

    verify(loanRepository).findById(loanIdObj);
  }
}
