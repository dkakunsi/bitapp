package io.dkakunsi.bitapp.account.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.dkakunsi.bitapp.Context;
import io.dkakunsi.bitapp.Id;
import io.dkakunsi.bitapp.Result;
import io.dkakunsi.bitapp.Result.ErrorCode;
import io.dkakunsi.bitapp.account.domain.entity.Account;
import io.dkakunsi.bitapp.account.domain.repository.AccountRepository;

public final class GetUserAccountsTest {

  private static final String REQUESTER = "Requester";

  private static final Context context = Context.builder().requester(REQUESTER).build();

  private static final String USER_ID = "user123";

  private static final Id USER = Id.of(USER_ID);

  private GetUserAccounts underTest;

  private AccountRepository accountRepository;

  @BeforeEach
  void setUp() {
    accountRepository = mock(AccountRepository.class);
    underTest = new GetUserAccounts(accountRepository);
  }

  @Test
  void givenValidUserIdWhenAccountsExistThenShouldReturnAccountsList() {
    // Given
    var input = USER_ID;

    var account1 = Account.builder()
        .id(Id.of("account1"))
        .name("Savings Account")
        .type(Account.Type.BANK)
        .themeColor("#FF5733")
        .balance(BigDecimal.valueOf(1000.00))
        .user(USER)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .createdBy(REQUESTER)
        .updatedBy(REQUESTER)
        .build();

    var account2 = Account.builder()
        .id(Id.of("account2"))
        .name("Checking Account")
        .type(Account.Type.CASH)
        .themeColor("#3357FF")
        .balance(BigDecimal.valueOf(500.00))
        .user(USER)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .createdBy(REQUESTER)
        .updatedBy(REQUESTER)
        .build();

    var accounts = Arrays.asList(account1, account2);
    when(accountRepository.findByUserId(USER)).thenReturn(accounts);

    // When
    var result = Context.executeInContext(context, () -> underTest.process(input));

    // Then
    assertTrue(result.isSuccess());
    assertTrue(result.data().isPresent());

    var resultData = result.data().get();
    assertNotNull(resultData);
    assertEquals(2, resultData.size());

    // Verify first account
    var firstAccount = resultData.get(0);
    assertEquals("account1", firstAccount.id());
    assertEquals("Savings Account", firstAccount.name());
    assertEquals(Account.Type.BANK.name(), firstAccount.type());
    assertEquals("#FF5733", firstAccount.themeColor());
    assertEquals(BigDecimal.valueOf(1000.00), firstAccount.balance());
    assertEquals(USER_ID, firstAccount.user());

    // Verify second account
    var secondAccount = resultData.get(1);
    assertEquals("account2", secondAccount.id());
    assertEquals("Checking Account", secondAccount.name());
    assertEquals(Account.Type.CASH.name(), secondAccount.type());
    assertEquals("#3357FF", secondAccount.themeColor());
    assertEquals(BigDecimal.valueOf(500.00), secondAccount.balance());
    assertEquals(USER_ID, secondAccount.user());

    verify(accountRepository).findByUserId(USER);
  }

  @Test
  void givenValidUserIdWhenNoAccountsExistThenShouldReturnEmptyList() {
    // Given
    var input = USER_ID;

    when(accountRepository.findByUserId(USER)).thenReturn(Collections.emptyList());

    // When
    var result = Context.executeInContext(context, () -> underTest.process(input));

    // Then
    assertTrue(result.isSuccess());
    assertTrue(result.data().isPresent());

    var resultData = result.data().get();
    assertNotNull(resultData);
    assertEquals(0, resultData.size());

    verify(accountRepository).findByUserId(USER);
  }

  @Test
  void givenValidUserIdWhenRepositoryThrowsExceptionThenShouldReturnFailure() {
    // Given
    var input = USER_ID;

    when(accountRepository.findByUserId(any(Id.class)))
        .thenThrow(new RuntimeException("Database connection error"));

    // When
    var result = Context.executeInContext(context, () -> underTest.process(input));

    // Then
    assertFalse(result.isSuccess());
    assertTrue(result.errorCode().isPresent());

    assertEquals(ErrorCode.INTERNAL_ERROR, result.errorCode().get());
    assertEquals(Result.DEFAULT_ERROR_MESSAGE, result.errorMessage().get());

    verify(accountRepository).findByUserId(USER);
  }

  @Test
  void givenUserIdWhenSingleAccountExistsThenShouldReturnSingleAccountList() {
    // Given
    var input = USER_ID;

    var user = Id.of(USER_ID);
    var account = Account.builder()
        .id(Id.of("account1"))
        .name("E-Wallet")
        .type(Account.Type.EWALLET)
        .themeColor("#00FF00")
        .balance(BigDecimal.valueOf(250.50))
        .user(user)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .createdBy(REQUESTER)
        .updatedBy(REQUESTER)
        .build();

    when(accountRepository.findByUserId(USER)).thenReturn(List.of(account));

    // When
    var result = Context.executeInContext(context, () -> underTest.process(input));

    // Then
    assertTrue(result.isSuccess());
    assertTrue(result.data().isPresent());

    var resultData = result.data().get();
    assertNotNull(resultData);
    assertEquals(1, resultData.size());

    var accountItem = resultData.get(0);
    assertEquals("account1", accountItem.id());
    assertEquals("E-Wallet", accountItem.name());
    assertEquals(Account.Type.EWALLET.name(), accountItem.type());
    assertEquals("#00FF00", accountItem.themeColor());
    assertEquals(BigDecimal.valueOf(250.50), accountItem.balance());
    assertEquals(USER_ID, accountItem.user());

    verify(accountRepository).findByUserId(USER);
  }

  @Test
  void givenDifferentUserIdsWhenCalledThenShouldUseCorrectUserId() {
    // Given
    var userId1 = "user111";
    var user1 = Id.of(userId1);
    var userId2 = "user222";
    var user2 = Id.of(userId2);

    when(accountRepository.findByUserId(user1)).thenReturn(Collections.emptyList());
    when(accountRepository.findByUserId(user2)).thenReturn(Collections.emptyList());

    // When
    Context.executeInContext(context, () -> underTest.process(userId1));
    Context.executeInContext(context, () -> underTest.process(userId2));

    // Then
    verify(accountRepository).findByUserId(user1);
    verify(accountRepository).findByUserId(user2);
  }
}
