package io.dkakunsi.bitapp.account.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.dkakunsi.bitapp.AppError.Code;
import io.dkakunsi.bitapp.Context;
import io.dkakunsi.bitapp.EntityStatus;
import io.dkakunsi.bitapp.Id;
import io.dkakunsi.bitapp.SessionManager;
import io.dkakunsi.bitapp.account.domain.entity.Account;
import io.dkakunsi.bitapp.account.domain.port.AccountLoanPort;
import io.dkakunsi.bitapp.account.domain.port.AccountTransactionPort;
import io.dkakunsi.bitapp.account.domain.repository.AccountRepository;

public final class RemoveAccountUseCaseTest {

  private static final String REQUESTER = "user@email.com";
  private static final String OTHER_USER = "other@email.com";
  private static final String ACCOUNT_ID = "account-1";
  private static final Id ACCOUNT = Id.of(ACCOUNT_ID);
  private static final Context context = Context.builder().requester(REQUESTER).build();

  private RemoveAccount underTest;

  private AccountRepository accountRepository;
  private AccountTransactionPort accountTransactionPort;
  private AccountLoanPort accountLoanPort;
  private SessionManager sessionManager;

  @BeforeEach
  void setUp() {
    accountRepository = mock(AccountRepository.class);
    accountTransactionPort = mock(AccountTransactionPort.class);
    accountLoanPort = mock(AccountLoanPort.class);
    sessionManager = mock(SessionManager.class);
    underTest = new RemoveAccount(accountRepository, accountTransactionPort, accountLoanPort, sessionManager);
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
  void removeAccountShouldDelegateToPortsAndDeleteAccount() {
    // Given
    var account = createAccount(ACCOUNT_ID, REQUESTER);
    when(accountRepository.findById(ACCOUNT)).thenReturn(Optional.of(account));

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

    verify(accountLoanPort).removeByAccountId(ACCOUNT);
    verify(accountTransactionPort).removeOrUpdateByAccountId(ACCOUNT);
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
}
