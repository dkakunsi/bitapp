package io.dkakunsi.bitapp.transaction.application.usecase;

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

import io.dkakunsi.bitapp.AppError.Code;
import io.dkakunsi.bitapp.Context;
import io.dkakunsi.bitapp.EntityStatus;
import io.dkakunsi.bitapp.Id;
import io.dkakunsi.bitapp.SessionManager;
import io.dkakunsi.bitapp.loan.domain.entity.Loan;
import io.dkakunsi.bitapp.transaction.application.dto.CreateLoanDisbursementTransactionInput;
import io.dkakunsi.bitapp.transaction.application.port.TransactionAccountPort;
import io.dkakunsi.bitapp.transaction.application.port.TransactionLoanPort;
import io.dkakunsi.bitapp.transaction.domain.repository.TransactionRepository;

public class CreateLoanDisbursementTransactionTest {

  private static final String REQUESTER = "test@email.com";

  private static final Context context = Context.builder().requester(REQUESTER).build();

  private static final String ACCOUNT_ID = "account-1";
  private static final Id ACCOUNT = Id.of(ACCOUNT_ID);
  private static final String LOAN_ID = "loan-1";

  private CreateTransaction underTest;

  private TransactionRepository transactionRepository;
  private TransactionAccountPort transactionAccountPort;
  private TransactionLoanPort transactionLoanPort;
  private SessionManager sessionManager;

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
  void givenBorrowLoanDisbursementWhenProcessedThenShouldCreditAccount() {
    // Given
    var loan = createLoan(LOAN_ID, Loan.Type.BORROW, new BigDecimal("1000000"));
    var createRequest = CreateLoanDisbursementTransactionInput.builder()
        .loan(loan)
        .build();

    when(transactionRepository.create(any())).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    var result = Context.executeInContext(context, () -> underTest.process(createRequest));

    // Then
    assertTrue(result.isSuccess());
    assertTrue(result.data().isPresent());

    var resultData = result.data().get();
    assertNotNull(resultData.id());
    assertEquals("Loan Disbursement", resultData.title());
    assertEquals(loan.amount(), resultData.amount());
    assertEquals("CREDIT", resultData.type());
    assertEquals("LOAN_DISBURSEMENT", resultData.category());

    verify(transactionAccountPort).creditBalance(ACCOUNT, loan.amount());
  }

  @Test
  void givenLendLoanDisbursementWhenProcessedThenShouldDebitAccount() {
    // Given
    var loan = createLoan(LOAN_ID, Loan.Type.LEND, new BigDecimal("500000"));
    var createRequest = CreateLoanDisbursementTransactionInput.builder()
        .loan(loan)
        .build();

    when(transactionRepository.create(any())).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    var result = Context.executeInContext(context, () -> underTest.process(createRequest));

    // Then
    assertTrue(result.isSuccess());
    assertTrue(result.data().isPresent());

    var resultData = result.data().get();
    assertNotNull(resultData.id());
    assertEquals("Loan Disbursement", resultData.title());
    assertEquals(loan.amount(), resultData.amount());
    assertEquals("DEBIT", resultData.type());
    assertEquals("LOAN_DISBURSEMENT", resultData.category());

    verify(transactionAccountPort).debitBalance(ACCOUNT, loan.amount());
  }

  @Test
  void givenBorrowLoanWithNonExistentAccountWhenProcessedThenShouldReturnBadRequestError() {
    // Given
    var loan = createLoan(LOAN_ID, Loan.Type.BORROW, new BigDecimal("1000000"));
    var createRequest = CreateLoanDisbursementTransactionInput.builder()
        .loan(loan)
        .build();

    when(transactionRepository.create(any())).thenAnswer(invocation -> invocation.getArgument(0));
    doThrow(new IllegalArgumentException("account not found"))
        .when(transactionAccountPort).creditBalance(any(), any());

    // When
    var result = Context.executeInContext(context, () -> underTest.process(createRequest));

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

    when(transactionRepository.create(any())).thenAnswer(invocation -> invocation.getArgument(0));
    doThrow(new IllegalArgumentException("account not found"))
        .when(transactionAccountPort).debitBalance(any(), any());

    // When
    var result = Context.executeInContext(context, () -> underTest.process(createRequest));

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

    when(transactionRepository.create(any())).thenThrow(new RuntimeException("Database error"));

    // When
    var result = Context.executeInContext(context, () -> underTest.process(createRequest));

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
