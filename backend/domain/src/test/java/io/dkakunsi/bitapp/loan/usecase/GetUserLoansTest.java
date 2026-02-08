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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Currency;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.dkakunsi.bitapp.common.Context;
import io.dkakunsi.bitapp.domain.entity.EntityStatus;
import io.dkakunsi.bitapp.domain.entity.Id;
import io.dkakunsi.bitapp.loan.entity.Loan;
import io.dkakunsi.bitapp.loan.repository.LoanRepository;

public final class GetUserLoansTest {

  private static final String REQUESTER = "requester@email.com";

  private static final Context context = Context.builder().requester(REQUESTER).build();

  private GetUserLoans underTest;

  private LoanRepository loanRepository;

  private static final String USER_ID = "user123";
  private static final Id USER = Id.of(USER_ID);

  @BeforeEach
  void setUp() {
    loanRepository = mock(LoanRepository.class);
    underTest = new GetUserLoans(loanRepository);
  }

  @Test
  void givenValidUserIdWhenLoansExistThenShouldReturnLoansList() {
    // Given
    var input = USER_ID;

    var user = Id.of(USER_ID);
    var loan1 = Loan.builder()
        .id(Id.of("loan1"))
        .user(user)
        .account(Id.of("account-123"))
        .type(Loan.Type.BORROW)
        .date(LocalDate.of(2024, 6, 15))
        .time(LocalTime.of(14, 30))
        .partyName("Bank ABC")
        .title("Car Loan")
        .description("Loan for purchasing a car")
        .amount(BigDecimal.valueOf(500000000))
        .remainingAmount(BigDecimal.valueOf(500000000))
        .currency(Currency.getInstance("IDR"))
        .interestRate(5.5)
        .status(EntityStatus.ACTIVE)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .createdBy(REQUESTER)
        .updatedBy(REQUESTER)
        .build();

    var loan2 = Loan.builder()
        .id(Id.of("loan2"))
        .user(user)
        .account(Id.of("account-456"))
        .type(Loan.Type.LEND)
        .date(LocalDate.of(2024, 6, 20))
        .time(LocalTime.of(10, 0))
        .partyName("John Doe")
        .title("Personal Loan")
        .description("Money lent to friend")
        .amount(BigDecimal.valueOf(10000000))
        .remainingAmount(BigDecimal.valueOf(10000000))
        .currency(Currency.getInstance("IDR"))
        .interestRate(2.0)
        .status(EntityStatus.ACTIVE)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .createdBy(REQUESTER)
        .updatedBy(REQUESTER)
        .build();

    var loans = Arrays.asList(loan1, loan2);
    when(loanRepository.findByUserId(USER)).thenReturn(loans);

    // When
    var result = Context.executeInContext(context, () -> underTest.process(input));

    // Then
    assertTrue(result.isSuccess());
    assertTrue(result.data().isPresent());

    var resultData = result.data().get();
    assertNotNull(resultData);
    assertEquals(2, resultData.size());

    // Verify first loan
    var firstLoan = resultData.get(0);
    assertEquals("loan1", firstLoan.id());
    assertEquals(USER_ID, firstLoan.user());
    assertEquals("BORROW", firstLoan.type());
    assertEquals("Bank ABC", firstLoan.partyName());
    assertEquals("Car Loan", firstLoan.title());
    assertEquals("Loan for purchasing a car", firstLoan.description());
    assertEquals(BigDecimal.valueOf(500000000), firstLoan.amount());
    assertEquals("IDR", firstLoan.currency());
    assertEquals(5.5, firstLoan.interestRate());

    // Verify second loan
    var secondLoan = resultData.get(1);
    assertEquals("loan2", secondLoan.id());
    assertEquals(USER_ID, secondLoan.user());
    assertEquals("LEND", secondLoan.type());
    assertEquals("John Doe", secondLoan.partyName());
    assertEquals("Personal Loan", secondLoan.title());

    verify(loanRepository).findByUserId(USER);
  }

  @Test
  void givenValidUserIdWhenNoLoansExistThenShouldReturnEmptyList() {
    // Given
    var input = USER_ID;

    when(loanRepository.findByUserId(USER)).thenReturn(Collections.emptyList());

    // When
    var result = Context.executeInContext(context, () -> underTest.process(input));

    // Then
    assertTrue(result.isSuccess());
    assertTrue(result.data().isPresent());

    var resultData = result.data().get();
    assertNotNull(resultData);
    assertEquals(0, resultData.size());

    verify(loanRepository).findByUserId(USER);
  }

  @Test
  void givenValidUserIdWhenRepositoryThrowsExceptionThenShouldReturnFailure() {
    // Given
    var input = USER_ID;

    when(loanRepository.findByUserId(any(Id.class))).thenThrow(new RuntimeException("Database error"));

    // When
    var result = Context.executeInContext(context, () -> underTest.process(input));

    // Then
    assertFalse(result.isSuccess());
    assertTrue(result.error().isPresent());

    verify(loanRepository).findByUserId(USER);
  }

  @Test
  void givenUserIdWhenSingleLoanExistsThenShouldReturnSingleLoanList() {
    // Given
    var input = USER_ID;

    var user = Id.of(USER_ID);
    var loan = Loan.builder()
        .id(Id.of("loan1"))
        .user(user)
        .account(Id.of("account-123"))
        .type(Loan.Type.BORROW)
        .date(LocalDate.of(2024, 6, 15))
        .time(LocalTime.of(14, 30))
        .partyName("Credit Union")
        .title("Home Renovation")
        .description("Loan for home improvement")
        .amount(BigDecimal.valueOf(200000000))
        .remainingAmount(BigDecimal.valueOf(200000000))
        .currency(Currency.getInstance("IDR"))
        .interestRate(4.5)
        .status(EntityStatus.ACTIVE)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .createdBy(REQUESTER)
        .updatedBy(REQUESTER)
        .build();

    when(loanRepository.findByUserId(USER)).thenReturn(Collections.singletonList(loan));

    // When
    var result = Context.executeInContext(context, () -> underTest.process(input));

    // Then
    assertTrue(result.isSuccess());
    assertTrue(result.data().isPresent());

    var resultData = result.data().get();
    assertNotNull(resultData);
    assertEquals(1, resultData.size());

    var firstLoan = resultData.get(0);
    assertEquals("loan1", firstLoan.id());
    assertEquals("Credit Union", firstLoan.partyName());
    assertEquals("Home Renovation", firstLoan.title());

    verify(loanRepository).findByUserId(USER);
  }

  @Test
  void givenDifferentUserIdsWhenCalledThenShouldUseCorrectUserId() {
    // Given
    var userId1 = "user111";
    var user1 = Id.of(userId1);
    var userId2 = "user222";
    var user2 = Id.of(userId2);

    when(loanRepository.findByUserId(user1)).thenReturn(Collections.emptyList());
    when(loanRepository.findByUserId(user2)).thenReturn(Collections.emptyList());

    // When
    Context.executeInContext(context, () -> underTest.process(userId1));
    Context.executeInContext(context, () -> underTest.process(userId2));

    // Then
    verify(loanRepository).findByUserId(user1);
    verify(loanRepository).findByUserId(user2);
  }
}
