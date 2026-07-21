package io.dkakunsi.bitapp.account.application.usecase;

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

import io.dkakunsi.bitapp.Context;
import io.dkakunsi.bitapp.Id;
import io.dkakunsi.bitapp.Result.ErrorCode;
import io.dkakunsi.bitapp.account.domain.entity.Account;
import io.dkakunsi.bitapp.account.domain.entity.Account.Type;
import io.dkakunsi.bitapp.account.domain.repository.AccountRepository;

public final class GetAccountTest {

  private static final String REQUESTER = "Requester";

  private static final Context context = Context.builder().requester(REQUESTER).build();

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
    var id = Id.of(accountId);

    var existingAccount = Account.builder()
        .id(id)
        .name("My Savings")
        .type(Type.BANK)
        .themeColor("#FF5733")
        .balance(BigDecimal.valueOf(1000))
        .user(Id.of("user@email.com"))
        .build();
    when(accountRepository.findById(id)).thenReturn(Optional.of(existingAccount));

    // When
    var result = Context.executeInContext(context, () -> underTest.process(accountId));

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
    verify(accountRepository).findById(id);
  }

  @Test
  void returnError_whenAccountNotExists() {
    // Given
    var accountId = "nonexistent-account";
    var id = Id.of(accountId);
    when(accountRepository.findById(id)).thenReturn(Optional.empty());

    // When
    var result = Context.executeInContext(context, () -> underTest.process(accountId));

    // Then
    assertFalse(result.isSuccess());
    assertTrue(result.errorCode().isPresent());
    assertEquals(ErrorCode.NOT_FOUND, result.errorCode().get());
    assertEquals("Account not found", result.errorMessage().get());

    verify(accountRepository).findById(id);
  }

  @Test
  void returnServerError_whenRepositoryThrowsException() {
    // Given
    var accountId = "error-account";
    var id = Id.of(accountId);
    when(accountRepository.findById(id)).thenThrow(new RuntimeException("Database error"));

    // When
    var result = Context.executeInContext(context, () -> underTest.process(accountId));

    // Then
    assertFalse(result.isSuccess());
    assertTrue(result.errorCode().isPresent());
    assertEquals(ErrorCode.INTERNAL_ERROR, result.errorCode().get());
    assertEquals("Database error", result.errorMessage().get());

    verify(accountRepository).findById(id);
  }
}
