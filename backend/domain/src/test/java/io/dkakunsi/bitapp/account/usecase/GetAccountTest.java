package io.dkakunsi.bitapp.account.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.dkakunsi.bitapp.account.entity.Account;
import io.dkakunsi.bitapp.account.entity.Account.Type;
import io.dkakunsi.bitapp.account.repository.AccountRepository;
import io.dkakunsi.bitapp.common.AppError.Code;
import io.dkakunsi.bitapp.common.Context;
import io.dkakunsi.bitapp.domain.entity.Id;

public final class GetAccountTest {

  private static final String REQUESTER = "Requester";

  private GetAccount underTest;

  private AccountRepository accountRepository;

  @BeforeEach
  void setUp() {
    accountRepository = mock(AccountRepository.class);
    underTest = new GetAccount(accountRepository);
  }

  @Test
  void returnAccountData_whenAccountExists() {
    // Given
    var accountId = "account-123";
    var existingAccount = Account.builder()
        .id(Id.of(accountId))
        .name("My Savings")
        .type(Type.BANK)
        .themeColor("#FF5733")
        .balance(BigDecimal.valueOf(1000))
        .user(Id.of("user@email.com"))
        .build();
    when(accountRepository.findById(accountId)).thenReturn(Optional.of(existingAccount));

    // When
    var context = Context.builder().requester(REQUESTER).build();
    var result = underTest.process(context, accountId);

    // Then
    assertTrue(result.isSuccess());
    assertTrue(result.data().isPresent());
    var account = result.data().get();
    assertEquals(accountId, account.id());
    assertEquals("My Savings", account.name());
    assertEquals("BANK", account.type());
    assertEquals("#FF5733", account.themeColor());
    assertEquals(BigDecimal.valueOf(1000), account.balance());
    assertEquals("user@email.com", account.user());
    verify(accountRepository).findById(accountId);
  }

  @Test
  void returnError_whenAccountNotExists() {
    // Given
    var accountId = "nonexistent-account";
    when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

    // When
    var context = Context.builder().requester(REQUESTER).build();
    var result = underTest.process(context, accountId);

    // Then
    assertFalse(result.isSuccess());
    assertTrue(result.error().isPresent());
    var error = result.error().get();
    assertEquals(Code.NOT_FOUND, error.code());
    assertEquals("Account not found", error.message());

    verify(accountRepository).findById(accountId);
  }

  @Test
  void returnServerError_whenRepositoryThrowsException() {
    // Given
    var accountId = "error-account";
    when(accountRepository.findById(accountId)).thenThrow(new RuntimeException("Database error"));

    // When
    var context = Context.builder().requester(REQUESTER).build();
    var result = underTest.process(context, accountId);

    // Then
    assertFalse(result.isSuccess());
    assertTrue(result.error().isPresent());
    var error = result.error().get();
    assertEquals(Code.SERVER_ERROR, error.code());
    assertEquals("Database error", error.message());

    verify(accountRepository).findById(accountId);
  }
}
