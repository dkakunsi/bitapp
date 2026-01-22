package io.dkakunsi.bitapp.transaction.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
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

import io.dkakunsi.bitapp.account.entity.Account;
import io.dkakunsi.bitapp.account.repository.AccountRepository;
import io.dkakunsi.bitapp.common.AppError.Code;
import io.dkakunsi.bitapp.common.Context;
import io.dkakunsi.bitapp.common.EntityStatus;
import io.dkakunsi.bitapp.common.Id;
import io.dkakunsi.bitapp.loan.entity.Loan;
import io.dkakunsi.bitapp.loan.repository.LoanRepository;
import io.dkakunsi.bitapp.transaction.dto.CreateTransactionInput;
import io.dkakunsi.bitapp.transaction.repository.TransactionRepository;

public final class CreateTransactionTest {

  private CreateTransaction underTest;

  private TransactionRepository transactionRepository;
  private AccountRepository accountRepository;
  private LoanRepository loanRepository;

  private static final String REQUESTER = "test@email.com";
  private static final String ACCOUNT_ID_1 = "account-1";
  private static final String ACCOUNT_ID_2 = "account-2";
  private static final String LOAN_ID = "loan-1";

  @BeforeEach
  void setUp() {
    transactionRepository = mock(TransactionRepository.class);
    accountRepository = mock(AccountRepository.class);
    loanRepository = mock(LoanRepository.class);
    underTest = new CreateTransaction(transactionRepository, accountRepository, loanRepository);
  }

  @Test
  void givenValidDebitTransactionWhenProcessedThenShouldSuccessfullyCreate() {
    // Given
    var createRequest = CreateTransactionInput.builder()
        .title("Grocery Shopping")
        .description("Monthly groceries")
        .date(LocalDate.now())
        .time(LocalTime.now())
        .source(ACCOUNT_ID_1)
        .amount(50000L)
        .currency("IDR")
        .category("FOOD")
        .type("DEBIT")
        .build();
    var context = Context.builder().requester(REQUESTER).build();

    var sourceAccount = createAccount(ACCOUNT_ID_1, new BigDecimal("100000"));
    when(accountRepository.findById(ACCOUNT_ID_1)).thenReturn(Optional.of(sourceAccount));
    when(transactionRepository.create(any())).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    var result = underTest.process(context, createRequest);

    // Then
    assertTrue(result.isSuccess());
    assertTrue(result.data().isPresent());

    var resultData = result.data().get();
    assertNotNull(resultData.id());
    assertEquals(createRequest.title(), resultData.title());
    assertEquals(createRequest.amount(), resultData.amount());
    assertEquals("DEBIT", resultData.type());

    var accountCaptor = ArgumentCaptor.forClass(Account.class);
    verify(accountRepository).update(accountCaptor.capture());
    var updatedAccount = accountCaptor.getValue();
    assertEquals(new BigDecimal("50000"), updatedAccount.balance());
  }

  @Test
  void givenValidCreditTransactionWhenProcessedThenShouldSuccessfullyCreate() {
    // Given
    var createRequest = CreateTransactionInput.builder()
        .title("Salary")
        .description("Monthly salary")
        .date(LocalDate.now())
        .time(LocalTime.now())
        .destination(ACCOUNT_ID_1)
        .amount(5000000L)
        .currency("IDR")
        .category("INCOME")
        .type("CREDIT")
        .build();
    var context = Context.builder().requester(REQUESTER).build();

    var destAccount = createAccount(ACCOUNT_ID_1, new BigDecimal("100000"));
    when(accountRepository.findById(ACCOUNT_ID_1)).thenReturn(Optional.of(destAccount));
    when(transactionRepository.create(any())).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    var result = underTest.process(context, createRequest);

    // Then
    assertTrue(result.isSuccess());
    assertTrue(result.data().isPresent());

    var resultData = result.data().get();
    assertEquals(createRequest.title(), resultData.title());
    assertEquals("CREDIT", resultData.type());

    var accountCaptor = ArgumentCaptor.forClass(Account.class);
    verify(accountRepository).update(accountCaptor.capture());
    var updatedAccount = accountCaptor.getValue();
    assertEquals(new BigDecimal("5100000"), updatedAccount.balance());
  }

  @Test
  void givenValidTransferTransactionWhenProcessedThenShouldSuccessfullyCreate() {
    // Given
    var createRequest = CreateTransactionInput.builder()
        .title("Transfer to Savings")
        .description("Monthly savings")
        .date(LocalDate.now())
        .time(LocalTime.now())
        .source(ACCOUNT_ID_1)
        .destination(ACCOUNT_ID_2)
        .amount(100000L)
        .currency("IDR")
        .category("OTHER")
        .type("TRANSFER")
        .build();
    var context = Context.builder().requester(REQUESTER).build();

    var sourceAccount = createAccount(ACCOUNT_ID_1, new BigDecimal("500000"));
    var destAccount = createAccount(ACCOUNT_ID_2, new BigDecimal("200000"));
    when(accountRepository.findById(ACCOUNT_ID_1)).thenReturn(Optional.of(sourceAccount));
    when(accountRepository.findById(ACCOUNT_ID_2)).thenReturn(Optional.of(destAccount));
    when(transactionRepository.create(any())).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    var result = underTest.process(context, createRequest);

    // Then
    assertTrue(result.isSuccess());
    assertTrue(result.data().isPresent());

    var resultData = result.data().get();
    assertEquals(createRequest.title(), resultData.title());
    assertEquals("TRANSFER", resultData.type());

    var accountCaptor = ArgumentCaptor.forClass(Account.class);
    verify(accountRepository, times(2)).update(accountCaptor.capture());
    var updatedAccounts = accountCaptor.getAllValues();
    assertEquals(new BigDecimal("400000"), updatedAccounts.get(0).balance());
    assertEquals(new BigDecimal("300000"), updatedAccounts.get(1).balance());
  }

  @Test
  void givenTransactionWithLoanWhenProcessedThenShouldUpdateLoanRemainingAmount() {
    // Given
    var createRequest = CreateTransactionInput.builder()
        .title("Loan Payment")
        .description("Monthly loan payment")
        .date(LocalDate.now())
        .time(LocalTime.now())
        .source(ACCOUNT_ID_1)
        .loan(LOAN_ID)
        .amount(100000L)
        .currency("IDR")
        .category("LOAN")
        .type("DEBIT")
        .build();
    var context = Context.builder().requester(REQUESTER).build();

    var sourceAccount = createAccount(ACCOUNT_ID_1, new BigDecimal("500000"));
    var loan = createLoan(LOAN_ID, new BigDecimal("500000"));
    when(accountRepository.findById(ACCOUNT_ID_1)).thenReturn(Optional.of(sourceAccount));
    when(loanRepository.findById(LOAN_ID)).thenReturn(Optional.of(loan));
    when(transactionRepository.create(any())).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    var result = underTest.process(context, createRequest);

    // Then
    assertTrue(result.isSuccess());
    assertTrue(result.data().isPresent());

    var loanCaptor = ArgumentCaptor.forClass(Loan.class);
    verify(loanRepository).update(loanCaptor.capture());
    var updatedLoan = loanCaptor.getValue();
    assertEquals(new BigDecimal("400000"), updatedLoan.remainingAmount());
  }

  @Test
  void givenDebitTransactionWithNonExistentSourceAccountThenShouldReturnNotFoundError() {
    // Given
    var createRequest = CreateTransactionInput.builder()
        .title("Transaction")
        .source("non-existent-account")
        .amount(50000L)
        .category("FOOD")
        .type("DEBIT")
        .build();
    var context = Context.builder().requester(REQUESTER).build();

    when(accountRepository.findById("non-existent-account")).thenReturn(Optional.empty());

    // When
    var result = underTest.process(context, createRequest);

    // Then
    assertFalse(result.isSuccess());
    assertTrue(result.error().isPresent());
    var error = result.error().get();
    assertEquals(Code.NOT_FOUND, error.code());
    assertTrue(error.message().contains("source account not found"));
  }

  @Test
  void givenCreditTransactionWithNonExistentDestinationAccountThenShouldReturnNotFoundError() {
    // Given
    var createRequest = CreateTransactionInput.builder()
        .title("Transaction")
        .destination("non-existent-account")
        .amount(50000L)
        .category("INCOME")
        .type("CREDIT")
        .build();
    var context = Context.builder().requester(REQUESTER).build();

    when(accountRepository.findById("non-existent-account")).thenReturn(Optional.empty());

    // When
    var result = underTest.process(context, createRequest);

    // Then
    assertFalse(result.isSuccess());
    assertTrue(result.error().isPresent());
    var error = result.error().get();
    assertEquals(Code.NOT_FOUND, error.code());
    assertTrue(error.message().contains("destination account not found"));
  }

  @Test
  void givenTransactionWithNonExistentLoanThenShouldReturnNotFoundError() {
    // Given
    var createRequest = CreateTransactionInput.builder()
        .title("Loan Payment")
        .source(ACCOUNT_ID_1)
        .loan("non-existent-loan")
        .amount(100000L)
        .category("LOAN")
        .type("DEBIT")
        .build();
    var context = Context.builder().requester(REQUESTER).build();

    var sourceAccount = createAccount(ACCOUNT_ID_1, new BigDecimal("500000"));
    when(accountRepository.findById(ACCOUNT_ID_1)).thenReturn(Optional.of(sourceAccount));
    when(loanRepository.findById("non-existent-loan")).thenReturn(Optional.empty());

    // When
    var result = underTest.process(context, createRequest);

    // Then
    assertFalse(result.isSuccess());
    assertTrue(result.error().isPresent());
    var error = result.error().get();
    assertEquals(Code.NOT_FOUND, error.code());
    assertTrue(error.message().contains("loan not found"));
  }

  @Test
  void givenRepositoryErrorWhenCreatingTransactionThenShouldReturnServerError() {
    // Given
    var createRequest = CreateTransactionInput.builder()
        .title("Transaction")
        .source(ACCOUNT_ID_1)
        .amount(50000L)
        .category("FOOD")
        .type("DEBIT")
        .build();
    var context = Context.builder().requester(REQUESTER).build();

    var sourceAccount = createAccount(ACCOUNT_ID_1, new BigDecimal("100000"));
    when(accountRepository.findById(ACCOUNT_ID_1)).thenReturn(Optional.of(sourceAccount));
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

  private Account createAccount(String id, BigDecimal balance) {
    return Account.builder()
        .id(Id.of(id))
        .user(Id.of(REQUESTER))
        .name("Test Account")
        .type(Account.Type.BANK)
        .balance(balance)
        .status(EntityStatus.ACTIVE)
        .createdBy(REQUESTER)
        .updatedBy(REQUESTER)
        .build();
  }

  private Loan createLoan(String id, BigDecimal remainingAmount) {
    return Loan.builder()
        .id(Id.of(id))
        .user(Id.of(REQUESTER))
        .type(Loan.Type.BORROW)
        .partyName("Test Lender")
        .title("Test Loan")
        .amount(new BigDecimal("1000000"))
        .remainingAmount(remainingAmount)
        .currency(Currency.getInstance("IDR"))
        .status(EntityStatus.ACTIVE)
        .createdBy(REQUESTER)
        .updatedBy(REQUESTER)
        .build();
  }
}
