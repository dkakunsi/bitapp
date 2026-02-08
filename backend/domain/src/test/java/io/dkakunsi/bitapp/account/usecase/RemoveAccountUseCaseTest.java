package io.dkakunsi.bitapp.account.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import io.dkakunsi.bitapp.account.entity.Account;
import io.dkakunsi.bitapp.account.repository.AccountRepository;
import io.dkakunsi.bitapp.common.AppError.Code;
import io.dkakunsi.bitapp.common.Context;
import io.dkakunsi.bitapp.database.SessionManager;
import io.dkakunsi.bitapp.domain.entity.EntityStatus;
import io.dkakunsi.bitapp.domain.entity.Id;
import io.dkakunsi.bitapp.domain.usecase.Result;
import io.dkakunsi.bitapp.loan.entity.Loan;
import io.dkakunsi.bitapp.loan.repository.LoanRepository;
import io.dkakunsi.bitapp.loan.usecase.RemoveLoan;
import io.dkakunsi.bitapp.transaction.entity.Transaction;
import io.dkakunsi.bitapp.transaction.repository.TransactionRepository;

public final class RemoveAccountUseCaseTest {

  private static final String REQUESTER = "user@email.com";
  private static final String OTHER_USER = "other@email.com";
  private static final String ACCOUNT_ID = "account-1";
  private static final Id ACCOUNT = Id.of(ACCOUNT_ID);
  private static final String OTHER_ACCOUNT_ID = "account-2";
  private static final String LOAN_ID = "loan-1";
  private static final Id LOAN = Id.of(LOAN_ID);
  private static final Context context = Context.builder().requester(REQUESTER).build();

  private RemoveAccount underTest;

  private AccountRepository accountRepository;
  private TransactionRepository transactionRepository;
  private LoanRepository loanRepository;
  private RemoveLoan removeLoan;
  private SessionManager sessionManager;

  @BeforeEach
  void setUp() {
    accountRepository = mock(AccountRepository.class);
    transactionRepository = mock(TransactionRepository.class);
    loanRepository = mock(LoanRepository.class);
    removeLoan = mock(RemoveLoan.class);
    sessionManager = mock(SessionManager.class);
    underTest = new RemoveAccount(accountRepository, transactionRepository, loanRepository, removeLoan, sessionManager);
  }

  @Test
  void returnNotFoundWhenAccountDoesNotExist() {
    // Given
    when(accountRepository.findById(ACCOUNT)).thenReturn(Optional.empty());

    // When
    var result = Context.executeInContext(context, () -> underTest.process(ACCOUNT_ID));

    // Then
    assertFalse(result.isSuccess());
    assertTrue(result.error().isPresent());
    assertEquals(Code.NOT_FOUND, result.error().get().code());
    assertEquals("Account not found", result.error().get().message());
  }

  @Test
  void returnForbiddenWhenAccountBelongsToAnotherUser() {
    // Given
    var account = createAccount(ACCOUNT_ID, OTHER_USER);
    when(accountRepository.findById(ACCOUNT)).thenReturn(Optional.of(account));

    // When
    var result = Context.executeInContext(context, () -> underTest.process(ACCOUNT_ID));

    // Then
    assertFalse(result.isSuccess());
    assertTrue(result.error().isPresent());
    assertEquals(Code.FORBIDDEN, result.error().get().code());
    verify(accountRepository, never()).deleteById(ACCOUNT);
  }

  @Test
  void removeAccountShouldHandleTransactionsAndLoans() {
    // Given
    var account = createAccount(ACCOUNT_ID, REQUESTER);
    var debit = createTransaction("debit-1", Transaction.Type.DEBIT, ACCOUNT_ID, null, null);
    var credit = createTransaction("credit-1", Transaction.Type.CREDIT, null, ACCOUNT_ID, null);
    var transferSource = createTransaction("transfer-1", Transaction.Type.TRANSFER, ACCOUNT_ID, OTHER_ACCOUNT_ID, null);
    var transferDestination = createTransaction("transfer-2", Transaction.Type.TRANSFER, OTHER_ACCOUNT_ID, ACCOUNT_ID,
        null);
    var loanTransaction = createTransaction("loan-1", Transaction.Type.CREDIT, null, ACCOUNT_ID, LOAN_ID);

    when(accountRepository.findById(ACCOUNT)).thenReturn(Optional.of(account));
    when(transactionRepository.findByAccountId(ACCOUNT))
        .thenReturn(List.of(debit, credit, transferSource, transferDestination, loanTransaction));
    when(transactionRepository.findByLoanId(LOAN)).thenReturn(List.of(loanTransaction));
    when(loanRepository.findByAccountId(ACCOUNT)).thenReturn(List.of(createLoan(LOAN_ID, REQUESTER)));

    when(removeLoan.execute(anyString())).thenReturn(Result.success("OK"));

    when(sessionManager.executeInSession(any()))
        .thenAnswer(invocation -> {
          var callable = invocation.getArgument(0, java.util.function.Supplier.class);
          return callable.get();
        });

    // When
    var result = Context.executeInContext(context, () -> underTest.process(ACCOUNT_ID));

    // Then
    assertTrue(result.isSuccess());
    assertTrue(result.data().isPresent());
    assertEquals(ACCOUNT_ID, result.data().get().id());

    verify(transactionRepository).deleteById(Id.of("debit-1"));
    verify(transactionRepository).deleteById(Id.of("credit-1"));
    verify(transactionRepository).deleteById(Id.of("loan-1"));
    verify(removeLoan).execute(eq(LOAN_ID));

    var updatedCaptor = ArgumentCaptor.forClass(Transaction.class);
    verify(transactionRepository, org.mockito.Mockito.times(2)).update(updatedCaptor.capture());

    var updates = updatedCaptor.getAllValues();
    var transferSourceUpdate = updates.stream()
        .filter(update -> update.id().value().equals("transfer-1"))
        .findFirst()
        .orElseThrow();
    assertEquals(Transaction.Type.CREDIT, transferSourceUpdate.type());
    assertEquals(OTHER_ACCOUNT_ID, transferSourceUpdate.destination().value());

    var transferDestinationUpdate = updates.stream()
        .filter(update -> update.id().value().equals("transfer-2"))
        .findFirst()
        .orElseThrow();
    assertEquals(Transaction.Type.DEBIT, transferDestinationUpdate.type());
    assertEquals(OTHER_ACCOUNT_ID, transferDestinationUpdate.source().value());

    verify(accountRepository).deleteById(ACCOUNT);
  }

  private static Account createAccount(String id, String user) {
    return Account.builder()
        .id(Id.of(id))
        .user(Id.of(user))
        .name("Test Account")
        .type(Account.Type.BANK)
        .themeColor("#FFFFFF")
        .balance(BigDecimal.ZERO)
        .status(EntityStatus.ACTIVE)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .createdBy(user)
        .updatedBy(user)
        .build();
  }

  private static Loan createLoan(String id, String user) {
    return Loan.builder()
        .id(Id.of(id))
        .user(Id.of(user))
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

  private static Transaction createTransaction(
      String id,
      Transaction.Type type,
      String source,
      String destination,
      String loanId) {
    return Transaction.builder()
        .id(Id.of(id))
        .user(Id.of(REQUESTER))
        .title("Test Transaction")
        .description("Test")
        .date(LocalDate.of(2026, 1, 24))
        .time(LocalTime.of(10, 0))
        .source(source != null ? Id.of(source) : null)
        .destination(destination != null ? Id.of(destination) : null)
        .loan(loanId != null ? Id.of(loanId) : null)
        .amount(BigDecimal.valueOf(100000))
        .currency(Currency.getInstance("IDR"))
        .category(Transaction.Category.OTHER)
        .type(type)
        .status(EntityStatus.ACTIVE)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .createdBy(REQUESTER)
        .updatedBy(REQUESTER)
        .build();
  }
}
