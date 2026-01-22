package io.dkakunsi.bitapp.account.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
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

import io.dkakunsi.bitapp.account.entity.Account;
import io.dkakunsi.bitapp.account.repository.AccountRepository;
import io.dkakunsi.bitapp.common.AppError.Code;
import io.dkakunsi.bitapp.common.Context;
import io.dkakunsi.bitapp.common.Id;

public final class GetUserAccountsTest {

  private GetUserAccounts underTest;

  private AccountRepository accountRepository;

  private static final String USER_ID = "user123";
  private static final String REQUESTER = "requester@email.com";

  @BeforeEach
  void setUp() {
    accountRepository = mock(AccountRepository.class);
    underTest = new GetUserAccounts(accountRepository);
  }

  @Test
  void givenValidUserIdWhenAccountsExistThenShouldReturnAccountsList() {
    // Given
    var input = USER_ID;
    var context = Context.builder().requester(REQUESTER).build();

    var user = Id.of(USER_ID);
    var account1 = Account.builder()
        .id(Id.of("account1"))
        .name("Savings Account")
        .type(Account.Type.BANK)
        .themeColor("#FF5733")
        .balance(BigDecimal.valueOf(1000.00))
        .user(user)
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
        .user(user)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .createdBy(REQUESTER)
        .updatedBy(REQUESTER)
        .build();

    var accounts = Arrays.asList(account1, account2);
    when(accountRepository.findByUserId(USER_ID)).thenReturn(accounts);

    // When
    var result = underTest.process(context, input);

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

    verify(accountRepository).findByUserId(USER_ID);
  }

  @Test
  void givenValidUserIdWhenNoAccountsExistThenShouldReturnEmptyList() {
    // Given
    var input = USER_ID;
    var context = Context.builder().requester(REQUESTER).build();

    when(accountRepository.findByUserId(USER_ID)).thenReturn(Collections.emptyList());

    // When
    var result = underTest.process(context, input);

    // Then
    assertTrue(result.isSuccess());
    assertTrue(result.data().isPresent());

    var resultData = result.data().get();
    assertNotNull(resultData);
    assertEquals(0, resultData.size());

    verify(accountRepository).findByUserId(USER_ID);
  }

  @Test
  void givenValidUserIdWhenRepositoryThrowsExceptionThenShouldReturnFailure() {
    // Given
    var input = USER_ID;
    var context = Context.builder().requester(REQUESTER).build();

    when(accountRepository.findByUserId(anyString()))
        .thenThrow(new RuntimeException("Database connection error"));

    // When
    var result = underTest.process(context, input);

    // Then
    assertFalse(result.isSuccess());
    assertTrue(result.error().isPresent());

    var error = result.error().get();
    assertEquals(Code.SERVER_ERROR, error.code());
    assertEquals("Database connection error", error.message());

    verify(accountRepository).findByUserId(USER_ID);
  }

  @Test
  void givenUserIdWhenSingleAccountExistsThenShouldReturnSingleAccountList() {
    // Given
    var input = USER_ID;
    var context = Context.builder().requester(REQUESTER).build();

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

    when(accountRepository.findByUserId(USER_ID)).thenReturn(List.of(account));

    // When
    var result = underTest.process(context, input);

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

    verify(accountRepository).findByUserId(USER_ID);
  }

  @Test
  void givenDifferentUserIdsWhenCalledThenShouldUseCorrectUserId() {
    // Given
    var userId1 = "user111";
    var userId2 = "user222";
    var context = Context.builder().requester(REQUESTER).build();

    when(accountRepository.findByUserId(userId1)).thenReturn(Collections.emptyList());
    when(accountRepository.findByUserId(userId2)).thenReturn(Collections.emptyList());

    // When
    underTest.process(context, userId1);
    underTest.process(context, userId2);

    // Then
    verify(accountRepository).findByUserId(userId1);
    verify(accountRepository).findByUserId(userId2);
  }
}
