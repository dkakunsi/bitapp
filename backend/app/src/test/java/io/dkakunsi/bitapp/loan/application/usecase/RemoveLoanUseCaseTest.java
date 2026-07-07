package io.dkakunsi.bitapp.loan.application.usecase;

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

import io.dkakunsi.bitapp.AppError.Code;
import io.dkakunsi.bitapp.Context;
import io.dkakunsi.bitapp.EntityStatus;
import io.dkakunsi.bitapp.Id;
import io.dkakunsi.bitapp.SessionManager;
import io.dkakunsi.bitapp.loan.domain.entity.Loan;
import io.dkakunsi.bitapp.loan.domain.port.LoanTransactionPort;
import io.dkakunsi.bitapp.loan.domain.repository.LoanRepository;

public final class RemoveLoanUseCaseTest {

  private static final String REQUESTER = "user@email.com";
  private static final String OTHER_USER = "other@email.com";
  private static final String LOAN_ID = "loan-123";
  private static final Id LOAN = Id.of(LOAN_ID);
  private static final Context context = Context.builder().requester(REQUESTER).build();

  private RemoveLoan underTest;

  private LoanRepository loanRepository;
  private LoanTransactionPort loanTransactionPort;
  private SessionManager sessionManager;

  @BeforeEach
  void setUp() {
    loanRepository = mock(LoanRepository.class);
    loanTransactionPort = mock(LoanTransactionPort.class);
    sessionManager = mock(SessionManager.class);
    underTest = new RemoveLoan(loanRepository, loanTransactionPort, sessionManager);

    when(sessionManager.executeInSession(any())).thenAnswer(invocation -> {
      var function = invocation.getArgument(0, java.util.function.Supplier.class);
      return function.get();
    });
  }

  @Test
  void returnNotFoundWhenLoanDoesNotExist() {
    // Given
    when(loanRepository.findById(LOAN)).thenReturn(Optional.empty());

    // When
    var result = Context.executeInContext(context, () -> underTest.process(LOAN_ID));

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
    var result = Context.executeInContext(context, () -> underTest.process(LOAN_ID));

    // Then
    assertFalse(result.isSuccess());
    assertTrue(result.error().isPresent());
    assertEquals(Code.FORBIDDEN, result.error().get().code());
    verify(loanRepository, never()).deleteById(LOAN);
  }

  @Test
  void removeLoanShouldDelegateTransactionUpdateAndDeleteLoan() {
    // Given
    var loan = createLoan(LOAN_ID, REQUESTER);
    when(loanRepository.findById(LOAN)).thenReturn(Optional.of(loan));

    // When
    var result = Context.executeInContext(context, () -> underTest.process(LOAN_ID));

    // Then
    assertTrue(result.isSuccess());
    assertTrue(result.data().isPresent());
    assertEquals(LOAN_ID, result.data().get().id());

    verify(loanTransactionPort).updateTransactionByLoanRemoval(LOAN);
    verify(loanRepository).deleteById(LOAN);
  }

  private static Loan createLoan(String id, String user) {
    return Loan.builder()
        .id(Id.of(id))
        .user(Id.of(user))
        .account(Id.of("account-123"))
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

}
