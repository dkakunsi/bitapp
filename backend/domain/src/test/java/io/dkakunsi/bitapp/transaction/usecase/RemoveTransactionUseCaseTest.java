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
  private static final String ACCOUNT_ID_2 = "account-2";
  private static final String LOAN_ID = "loan-1";

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
    when(transactionRepository.findById(transactionId)).thenReturn(Optional.empty());

    // When
    var context = Context.builder().requester(REQUESTER).build();
    var result = underTest.process(context, transactionId);

    // Then
    assertFalse(result.isSuccess());
    assertTrue(result.error().isPresent());
    var error = result.error().get();
    assertEquals(Code.NOT_FOUND, error.code());
    assertEquals("Transaction not found", error.message());
    verify(transactionRepository).findById(transactionId);
  }

  @Test
  void returnNotFoundWhenTransactionBelongsToAnotherUser() {
    // Given
    var transactionId = "trans-other-user";
    var transaction = baseTransaction(transactionId, OTHER_USER, Transaction.Type.DEBIT)
        .source(Id.of(ACCOUNT_ID_1))
        .amount(50000L)
        .build();
    when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));

    // When
    var context = Context.builder().requester(REQUESTER).build();
    var result = underTest.process(context, transactionId);

    // Then
    assertFalse(result.isSuccess());
    assertTrue(result.error().isPresent());
    assertEquals(Code.NOT_FOUND, result.error().get().code());
    verify(transactionRepository, never()).deleteById(transactionId);
  }

  @Test
  void revertDebitTransactionAndDelete() {
    // Given
    var transactionId = "trans-debit";
    var transaction = baseTransaction(transactionId, REQUESTER, Transaction.Type.DEBIT)
        .source(Id.of(ACCOUNT_ID_1))
        .amount(100000L)
        .build();

    var sourceAccount = createAccount(ACCOUNT_ID_1, new BigDecimal("900000"));
    when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));
    when(accountRepository.findById(ACCOUNT_ID_1)).thenReturn(Optional.of(sourceAccount));

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
    verify(transactionRepository).deleteById(transactionId);
  }

  @Test
  void revertCreditTransactionAndDelete() {
    // Given
    var transactionId = "trans-credit";
    var transaction = baseTransaction(transactionId, REQUESTER, Transaction.Type.CREDIT)
        .destination(Id.of(ACCOUNT_ID_2))
        .amount(100000L)
        .build();

    var destinationAccount = createAccount(ACCOUNT_ID_2, new BigDecimal("600000"));
    when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));
    when(accountRepository.findById(ACCOUNT_ID_2)).thenReturn(Optional.of(destinationAccount));

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
    verify(transactionRepository).deleteById(transactionId);
  }

  @Test
  void revertTransferTransactionAndDelete() {
    // Given
    var transactionId = "trans-transfer";
    var transaction = baseTransaction(transactionId, REQUESTER, Transaction.Type.TRANSFER)
        .source(Id.of(ACCOUNT_ID_1))
        .destination(Id.of(ACCOUNT_ID_2))
        .amount(200000L)
        .build();

    var sourceAccount = createAccount(ACCOUNT_ID_1, new BigDecimal("800000"));
    var destinationAccount = createAccount(ACCOUNT_ID_2, new BigDecimal("700000"));
    when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));
    when(accountRepository.findById(ACCOUNT_ID_1)).thenReturn(Optional.of(sourceAccount));
    when(accountRepository.findById(ACCOUNT_ID_2)).thenReturn(Optional.of(destinationAccount));

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
    verify(transactionRepository).deleteById(transactionId);
  }

  @Test
  void revertLoanAndDelete() {
    // Given
    var transactionId = "trans-loan";
    var transaction = baseTransaction(transactionId, REQUESTER, Transaction.Type.DEBIT)
        .source(Id.of(ACCOUNT_ID_1))
        .loan(Id.of(LOAN_ID))
        .amount(100000L)
        .build();

    var sourceAccount = createAccount(ACCOUNT_ID_1, new BigDecimal("900000"));
    var loan = createLoan(LOAN_ID, new BigDecimal("1900000"));
    when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));
    when(accountRepository.findById(ACCOUNT_ID_1)).thenReturn(Optional.of(sourceAccount));
    when(loanRepository.findById(LOAN_ID)).thenReturn(Optional.of(loan));

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
    verify(transactionRepository).deleteById(transactionId);
  }

  private static Transaction.TransactionBuilder baseTransaction(String id, String user, Transaction.Type type) {
    return Transaction.builder()
        .id(Id.of(id))
        .user(Id.of(user))
        .title("Test Transaction")
        .description("Test Description")
        .date(LocalDate.of(2026, 1, 24))
        .time(LocalTime.of(10, 0))
        .currency("IDR")
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
