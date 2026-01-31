package io.dkakunsi.bitapp.transaction.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Currency;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.dkakunsi.bitapp.account.repository.AccountRepository;
import io.dkakunsi.bitapp.common.AppError.Code;
import io.dkakunsi.bitapp.common.Context;
import io.dkakunsi.bitapp.database.SessionManager;
import io.dkakunsi.bitapp.domain.entity.EntityStatus;
import io.dkakunsi.bitapp.domain.entity.Id;
import io.dkakunsi.bitapp.loan.entity.Loan;
import io.dkakunsi.bitapp.loan.repository.LoanRepository;
import io.dkakunsi.bitapp.transaction.dto.CreateLoanDisbursementTransactionInput;
import io.dkakunsi.bitapp.transaction.repository.TransactionRepository;

public class CreateLoanDisbursementTransactionTest {

  private CreateTransaction underTest;

  private TransactionRepository transactionRepository;
  private AccountRepository accountRepository;
  private LoanRepository loanRepository;
  private SessionManager sessionManager;

  private static final String REQUESTER = "test@email.com";
  private static final String ACCOUNT_ID = "account-1";
  private static final Id ACCOUNT = Id.of(ACCOUNT_ID);
  private static final String LOAN_ID = "loan-1";

  @BeforeEach
  void setUp() {
    transactionRepository = mock(TransactionRepository.class);
    accountRepository = mock(AccountRepository.class);
    loanRepository = mock(LoanRepository.class);
    sessionManager = mock(SessionManager.class);
    underTest = new CreateTransaction(transactionRepository, accountRepository, loanRepository, sessionManager);

    when(sessionManager.executeInSession(any())).thenAnswer(invocation -> {
      var function = invocation.getArgument(0, java.util.function.Supplier.class);
      return function.get();
    });
  }

  @Test
  void givenBorrowLoanDisbursementWhenProcessedThenShouldCreditAccount() {
    // Given
    var loan = createLoan(LOAN_ID, Loan.Type.BORROW, new BigDecimal("1000000"));
    var createRequest = CreateLoanDisbursementTransactionInput.builder()
        .loan(loan)
        .build();
    var context = Context.builder().requester(REQUESTER).build();

    when(transactionRepository.create(any())).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    var result = underTest.process(context, createRequest);

    // Then
    assertTrue(result.isSuccess());
    assertTrue(result.data().isPresent());

    var resultData = result.data().get();
    assertNotNull(resultData.id());
    assertEquals("Loan Disbursement", resultData.title());
    assertEquals(loan.amount(), resultData.amount());
    assertEquals("CREDIT", resultData.type());
    assertEquals("LOAN_DISBURSEMENT", resultData.category());

    verify(accountRepository).creditBalance(ACCOUNT, loan.amount());
  }

  @Test
  void givenLendLoanDisbursementWhenProcessedThenShouldDebitAccount() {
    // Given
    var loan = createLoan(LOAN_ID, Loan.Type.LEND, new BigDecimal("500000"));
    var createRequest = CreateLoanDisbursementTransactionInput.builder()
        .loan(loan)
        .build();
    var context = Context.builder().requester(REQUESTER).build();

    when(transactionRepository.create(any())).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    var result = underTest.process(context, createRequest);

    // Then
    assertTrue(result.isSuccess());
    assertTrue(result.data().isPresent());

    var resultData = result.data().get();
    assertNotNull(resultData.id());
    assertEquals("Loan Disbursement", resultData.title());
    assertEquals(loan.amount(), resultData.amount());
    assertEquals("DEBIT", resultData.type());
    assertEquals("LOAN_DISBURSEMENT", resultData.category());

    verify(accountRepository).debitBalance(ACCOUNT, loan.amount());
  }

  @Test
  void givenBorrowLoanWithNonExistentAccountWhenProcessedThenShouldReturnBadRequestError() {
    // Given
    var loan = createLoan(LOAN_ID, Loan.Type.BORROW, new BigDecimal("1000000"));
    var createRequest = CreateLoanDisbursementTransactionInput.builder()
        .loan(loan)
        .build();
    var context = Context.builder().requester(REQUESTER).build();

    when(transactionRepository.create(any())).thenAnswer(invocation -> invocation.getArgument(0));
    doThrow(new IllegalArgumentException("account not found"))
        .when(accountRepository).creditBalance(any(), any());

    // When
    var result = underTest.process(context, createRequest);

    // Then
    assertFalse(result.isSuccess());
    assertTrue(result.error().isPresent());
    var error = result.error().get();
    assertEquals(Code.BAD_REQUEST, error.code());
    assertTrue(error.message().contains("account not found"));
  }

  @Test
  void givenLendLoanWithNonExistentAccountWhenProcessedThenShouldReturnBadRequestError() {
    // Given
    var loan = createLoan(LOAN_ID, Loan.Type.LEND, new BigDecimal("500000"));
    var createRequest = CreateLoanDisbursementTransactionInput.builder()
        .loan(loan)
        .build();
    var context = Context.builder().requester(REQUESTER).build();

    when(transactionRepository.create(any())).thenAnswer(invocation -> invocation.getArgument(0));
    doThrow(new IllegalArgumentException("account not found"))
        .when(accountRepository).debitBalance(any(), any());

    // When
    var result = underTest.process(context, createRequest);

    // Then
    assertFalse(result.isSuccess());
    assertTrue(result.error().isPresent());
    var error = result.error().get();
    assertEquals(Code.BAD_REQUEST, error.code());
    assertTrue(error.message().contains("account not found"));
  }

  @Test
  void givenRepositoryErrorWhenCreatingLoanDisbursementTransactionThenShouldReturnServerError() {
    // Given
    var loan = createLoan(LOAN_ID, Loan.Type.BORROW, new BigDecimal("1000000"));
    var createRequest = CreateLoanDisbursementTransactionInput.builder()
        .loan(loan)
        .build();
    var context = Context.builder().requester(REQUESTER).build();

    when(transactionRepository.create(any())).thenThrow(new RuntimeException("Database error"));

    // When
    var result = underTest.process(context, createRequest);

    // Then
    assertFalse(result.isSuccess());
    assertTrue(result.error().isPresent());
    var error = result.error().get();
    assertEquals(Code.SERVER_ERROR, error.code());
    assertEquals("Database error", error.message());
  }

  private Loan createLoan(String id, Loan.Type type, BigDecimal amount) {
    return Loan.builder()
        .id(Id.of(id))
        .user(Id.of(REQUESTER))
        .account(ACCOUNT)
        .type(type)
        .date(LocalDate.now())
        .time(LocalTime.now())
        .partyName("Test Party")
        .title("Test Loan")
        .description("Test loan description")
        .amount(amount)
        .remainingAmount(amount)
        .currency(Currency.getInstance("IDR"))
        .interestRate(5.0)
        .status(EntityStatus.ACTIVE)
        .createdBy(REQUESTER)
        .updatedBy(REQUESTER)
        .build();
  }
}
