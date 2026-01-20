package io.dkakunsi.bitapp.loan.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
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
import io.dkakunsi.bitapp.common.EntityStatus;
import io.dkakunsi.bitapp.common.Id;
import io.dkakunsi.bitapp.loan.dto.GetUserLoansInput;
import io.dkakunsi.bitapp.loan.entity.Loan;
import io.dkakunsi.bitapp.loan.repository.LoanRepository;

public final class GetUserLoansTest {

  private GetUserLoans underTest;

  private LoanRepository loanRepository;

  private static final String USER_ID = "user123";
  private static final String REQUESTER = "requester@email.com";

  @BeforeEach
  void setUp() {
    loanRepository = mock(LoanRepository.class);
    underTest = new GetUserLoans(loanRepository);
  }

  @Test
  void givenValidUserIdWhenLoansExistThenShouldReturnLoansList() {
    // Given
    var input = GetUserLoansInput.builder()
        .userId(USER_ID)
        .build();
    var context = Context.builder().requester(REQUESTER).build();

    var user = Id.of(USER_ID);
    var loan1 = Loan.builder()
        .id(Id.of("loan1"))
        .user(user)
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
    when(loanRepository.findByUserId(USER_ID)).thenReturn(loans);

    // When
    var result = underTest.process(context, input);

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

    verify(loanRepository).findByUserId(USER_ID);
  }

  @Test
  void givenValidUserIdWhenNoLoansExistThenShouldReturnEmptyList() {
    // Given
    var input = GetUserLoansInput.builder()
        .userId(USER_ID)
        .build();
    var context = Context.builder().requester(REQUESTER).build();

    when(loanRepository.findByUserId(USER_ID)).thenReturn(Collections.emptyList());

    // When
    var result = underTest.process(context, input);

    // Then
    assertTrue(result.isSuccess());
    assertTrue(result.data().isPresent());

    var resultData = result.data().get();
    assertNotNull(resultData);
    assertEquals(0, resultData.size());

    verify(loanRepository).findByUserId(USER_ID);
  }

  @Test
  void givenValidUserIdWhenRepositoryThrowsExceptionThenShouldReturnFailure() {
    // Given
    var input = GetUserLoansInput.builder()
        .userId(USER_ID)
        .build();
    var context = Context.builder().requester(REQUESTER).build();

    when(loanRepository.findByUserId(anyString())).thenThrow(new RuntimeException("Database error"));

    // When
    var result = underTest.process(context, input);

    // Then
    assertFalse(result.isSuccess());
    assertTrue(result.error().isPresent());

    verify(loanRepository).findByUserId(USER_ID);
  }

  @Test
  void givenUserIdWhenSingleLoanExistsThenShouldReturnSingleLoanList() {
    // Given
    var input = GetUserLoansInput.builder()
        .userId(USER_ID)
        .build();
    var context = Context.builder().requester(REQUESTER).build();

    var user = Id.of(USER_ID);
    var loan = Loan.builder()
        .id(Id.of("loan1"))
        .user(user)
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

    when(loanRepository.findByUserId(USER_ID)).thenReturn(Collections.singletonList(loan));

    // When
    var result = underTest.process(context, input);

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

    verify(loanRepository).findByUserId(USER_ID);
  }

  @Test
  void givenDifferentUserIdsWhenCalledThenShouldUseCorrectUserId() {
    // Given
    var userId1 = "user111";
    var userId2 = "user222";
    var input1 = GetUserLoansInput.builder().userId(userId1).build();
    var input2 = GetUserLoansInput.builder().userId(userId2).build();
    var context = Context.builder().requester(REQUESTER).build();

    when(loanRepository.findByUserId(userId1)).thenReturn(Collections.emptyList());
    when(loanRepository.findByUserId(userId2)).thenReturn(Collections.emptyList());

    // When
    underTest.process(context, input1);
    underTest.process(context, input2);

    // Then
    verify(loanRepository).findByUserId(userId1);
    verify(loanRepository).findByUserId(userId2);
  }
}
