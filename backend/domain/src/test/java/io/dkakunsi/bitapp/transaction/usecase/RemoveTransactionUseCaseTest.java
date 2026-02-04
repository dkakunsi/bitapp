package io.dkakunsi.bitapp.transaction.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

import io.dkakunsi.bitapp.account.entity.Account;
import io.dkakunsi.bitapp.account.repository.AccountRepository;
import io.dkakunsi.bitapp.common.AppError.Code;
import io.dkakunsi.bitapp.common.Context;
import io.dkakunsi.bitapp.domain.entity.EntityStatus;
import io.dkakunsi.bitapp.domain.entity.Id;
import io.dkakunsi.bitapp.loan.entity.Loan;
import io.dkakunsi.bitapp.loan.repository.LoanRepository;
import io.dkakunsi.bitapp.transaction.entity.Transaction;
import io.dkakunsi.bitapp.transaction.repository.TransactionRepository;

public final class RemoveTransactionUseCaseTest {

  private static final String REQUESTER = "user@email.com";
  private static final String OTHER_USER = "other@email.com";
  private static final String ACCOUNT_ID_1 = "account-1";
  private static final Id ACCOUNT_1 = Id.of(ACCOUNT_ID_1);
  private static final String ACCOUNT_ID_2 = "account-2";
  private static final Id ACCOUNT_2 = Id.of(ACCOUNT_ID_2);
  private static final String LOAN_ID = "loan-1";
  private static final Id LOAN = Id.of(LOAN_ID);

  private RemoveTransaction underTest;

  private TransactionRepository transactionRepository;
  private AccountRepository accountRepository;
  private LoanRepository loanRepository;

  @BeforeEach
  void setUp() {
    transactionRepository = mock(TransactionRepository.class);
    accountRepository = mock(AccountRepository.class);
    loanRepository = mock(LoanRepository.class);
    underTest = new RemoveTransaction(transactionRepository, accountRepository, loanRepository);
  }

  @Test
  void returnNotFoundWhenTransactionDoesNotExist() {
    // Given
    var transactionId = "trans-404";
    var transactionIdObj = Id.of(transactionId);
    when(transactionRepository.findById(transactionIdObj)).thenReturn(Optional.empty());

    // When
    var context = Context.builder().requester(REQUESTER).build();
    var result = underTest.process(context, transactionId);

    // Then
    assertFalse(result.isSuccess());
    assertTrue(result.error().isPresent());
    var error = result.error().get();
    assertEquals(Code.NOT_FOUND, error.code());
    assertEquals("Transaction not found", error.message());
    verify(transactionRepository).findById(transactionIdObj);
  }

  @Test
  void returnNotFoundWhenTransactionBelongsToAnotherUser() {
    // Given
    var transactionId = "trans-other-user";
    var transactionIdObj = Id.of(transactionId);
    var transaction = baseTransaction(transactionId, OTHER_USER, Transaction.Type.DEBIT)
        .source(ACCOUNT_1)
        .amount(BigDecimal.valueOf(50000))
        .build();
    when(transactionRepository.findById(transactionIdObj)).thenReturn(Optional.of(transaction));

    // When
    var context = Context.builder().requester(REQUESTER).build();
    var result = underTest.process(context, transactionId);

    // Then
    assertFalse(result.isSuccess());
    assertTrue(result.error().isPresent());
    assertEquals(Code.NOT_FOUND, result.error().get().code());
    verify(transactionRepository, never()).deleteById(transactionIdObj);
  }

  @Test
  void revertDebitTransactionAndDelete() {
    // Given
    var transactionId = "trans-debit";
    var transactionIdObj = Id.of(transactionId);
    var transaction = baseTransaction(transactionId, REQUESTER, Transaction.Type.DEBIT)
        .source(ACCOUNT_1)
        .amount(BigDecimal.valueOf(100000))
        .build();

    var sourceAccount = createAccount(ACCOUNT_ID_1, new BigDecimal("900000"));
    when(transactionRepository.findById(transactionIdObj)).thenReturn(Optional.of(transaction));
    when(accountRepository.findById(ACCOUNT_1)).thenReturn(Optional.of(sourceAccount));

    // When
    var context = Context.builder().requester(REQUESTER).build();
    var result = underTest.process(context, transactionId);

    // Then
    assertTrue(result.isSuccess());
    assertTrue(result.data().isPresent());
    assertEquals(transactionId, result.data().get().id());

    var accountCaptor = ArgumentCaptor.forClass(Account.class);
    verify(accountRepository).update(accountCaptor.capture());
    assertEquals(new BigDecimal("1000000"), accountCaptor.getValue().balance());
    verify(transactionRepository).deleteById(transactionIdObj);
  }

  @Test
  void revertCreditTransactionAndDelete() {
    // Given
    var transactionId = "trans-credit";
    var transactionIdObj = Id.of(transactionId);
    var transaction = baseTransaction(transactionId, REQUESTER, Transaction.Type.CREDIT)
        .destination(ACCOUNT_2)
        .amount(BigDecimal.valueOf(100000))
        .build();

    var destinationAccount = createAccount(ACCOUNT_ID_2, new BigDecimal("600000"));
    when(transactionRepository.findById(transactionIdObj)).thenReturn(Optional.of(transaction));
    when(accountRepository.findById(ACCOUNT_2)).thenReturn(Optional.of(destinationAccount));

    // When
    var context = Context.builder().requester(REQUESTER).build();
    var result = underTest.process(context, transactionId);

    // Then
    assertTrue(result.isSuccess());
    assertTrue(result.data().isPresent());
    assertEquals(transactionId, result.data().get().id());

    var accountCaptor = ArgumentCaptor.forClass(Account.class);
    verify(accountRepository).update(accountCaptor.capture());
    assertEquals(new BigDecimal("500000"), accountCaptor.getValue().balance());
    verify(transactionRepository).deleteById(transactionIdObj);
  }

  @Test
  void revertTransferTransactionAndDelete() {
    // Given
    var transactionId = "trans-transfer";
    var transactionIdObj = Id.of(transactionId);
    var transaction = baseTransaction(transactionId, REQUESTER, Transaction.Type.TRANSFER)
        .source(ACCOUNT_1)
        .destination(ACCOUNT_2)
        .amount(BigDecimal.valueOf(200000))
        .build();

    var sourceAccount = createAccount(ACCOUNT_ID_1, new BigDecimal("800000"));
    var destinationAccount = createAccount(ACCOUNT_ID_2, new BigDecimal("700000"));
    when(transactionRepository.findById(transactionIdObj)).thenReturn(Optional.of(transaction));
    when(accountRepository.findById(ACCOUNT_1)).thenReturn(Optional.of(sourceAccount));
    when(accountRepository.findById(ACCOUNT_2)).thenReturn(Optional.of(destinationAccount));

    // When
    var context = Context.builder().requester(REQUESTER).build();
    var result = underTest.process(context, transactionId);

    // Then
    assertTrue(result.isSuccess());
    assertTrue(result.data().isPresent());
    assertEquals(transactionId, result.data().get().id());

    var accountCaptor = ArgumentCaptor.forClass(Account.class);
    verify(accountRepository, org.mockito.Mockito.times(2)).update(accountCaptor.capture());
    var updatedAccounts = accountCaptor.getAllValues();
    assertEquals(new BigDecimal("1000000"), updatedAccounts.get(0).balance());
    assertEquals(new BigDecimal("500000"), updatedAccounts.get(1).balance());
    verify(transactionRepository).deleteById(transactionIdObj);
  }

  @Test
  void revertLoanAndDelete() {
    // Given
    var transactionId = "trans-loan";
    var transactionIdObj = Id.of(transactionId);
    var transaction = baseTransaction(transactionId, REQUESTER, Transaction.Type.DEBIT)
        .source(ACCOUNT_1)
        .loan(LOAN)
        .amount(BigDecimal.valueOf(100000))
        .build();

    var sourceAccount = createAccount(ACCOUNT_ID_1, new BigDecimal("900000"));
    var loan = createLoan(LOAN_ID, new BigDecimal("1900000"));
    when(transactionRepository.findById(transactionIdObj)).thenReturn(Optional.of(transaction));
    when(accountRepository.findById(ACCOUNT_1)).thenReturn(Optional.of(sourceAccount));
    when(loanRepository.findById(LOAN)).thenReturn(Optional.of(loan));

    // When
    var context = Context.builder().requester(REQUESTER).build();
    var result = underTest.process(context, transactionId);

    // Then
    assertTrue(result.isSuccess());
    assertTrue(result.data().isPresent());
    assertEquals(transactionId, result.data().get().id());

    var loanCaptor = ArgumentCaptor.forClass(Loan.class);
    verify(loanRepository).update(loanCaptor.capture());
    assertEquals(new BigDecimal("2000000"), loanCaptor.getValue().remainingAmount());
    verify(transactionRepository).deleteById(transactionIdObj);
  }

  private static Transaction.TransactionBuilder baseTransaction(String id, String user, Transaction.Type type) {
    return Transaction.builder()
        .id(Id.of(id))
        .user(Id.of(user))
        .title("Test Transaction")
        .description("Test Description")
        .date(LocalDate.of(2026, 1, 24))
        .time(LocalTime.of(10, 0))
        .currency(Currency.getInstance("IDR"))
        .category(Transaction.Category.OTHER)
        .type(type)
        .status(EntityStatus.ACTIVE)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .createdBy(user)
        .updatedBy(user);
  }

  private static Account createAccount(String id, BigDecimal balance) {
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

  private static Loan createLoan(String id, BigDecimal remainingAmount) {
    return Loan.builder()
        .id(Id.of(id))
        .user(Id.of(REQUESTER))
        .type(Loan.Type.BORROW)
        .partyName("Test Lender")
        .title("Test Loan")
        .amount(new BigDecimal("2000000"))
        .remainingAmount(remainingAmount)
        .currency(Currency.getInstance("IDR"))
        .status(EntityStatus.ACTIVE)
        .createdBy(REQUESTER)
        .updatedBy(REQUESTER)
        .build();
  }
}
