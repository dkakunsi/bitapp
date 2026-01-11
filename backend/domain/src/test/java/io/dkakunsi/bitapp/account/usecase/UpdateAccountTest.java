package io.dkakunsi.bitapp.account.usecase;

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

import io.dkakunsi.bitapp.account.dto.UpdateAccountInput;
import io.dkakunsi.bitapp.account.model.Account;
import io.dkakunsi.bitapp.account.repository.AccountRepository;
import io.dkakunsi.bitapp.common.AppError.Code;
import io.dkakunsi.bitapp.common.Context;
import io.dkakunsi.bitapp.common.Id;
import io.dkakunsi.bitapp.user.model.User;

public final class UpdateAccountTest {

  private UpdateAccount underTest;

  private AccountRepository accountRepository;

  private static final String REQUESTER = "requester@email.com";
  private static final String ACCOUNT_ID = "account123";

  @BeforeEach
  void setUp() {
    accountRepository = mock(AccountRepository.class);
    underTest = new UpdateAccount(accountRepository);
  }

  @Test
  void givenValidUpdateRequestWhenAllFieldsProvidedThenShouldUpdateSuccessfully() {
    // Given
    var input = UpdateAccountInput.builder()
        .id(ACCOUNT_ID)
        .name("Updated Savings")
        .type(Account.Type.CASH)
        .themeColor("#00FF00")
        .build();
    var context = Context.builder().requester(REQUESTER).build();

    var existingAccount = Account.builder()
        .id(Id.of(ACCOUNT_ID))
        .name("Original Name")
        .type(Account.Type.BANK)
        .themeColor("#FF5733")
        .balance(BigDecimal.valueOf(1000))
        .user(User.builder().id(Id.of(REQUESTER)).build())
        .createdAt(LocalDateTime.now().minusDays(1))
        .updatedAt(LocalDateTime.now().minusDays(1))
        .createdBy("creator@email.com")
        .updatedBy("creator@email.com")
        .build();

    var updatedAccount = Account.builder()
        .id(Id.of(ACCOUNT_ID))
        .name("Updated Savings")
        .type(Account.Type.CASH)
        .themeColor("#00FF00")
        .balance(BigDecimal.valueOf(1000))
        .user(User.builder().id(Id.of(REQUESTER)).build())
        .createdAt(LocalDateTime.now().minusDays(1))
        .updatedAt(LocalDateTime.now())
        .createdBy("creator@email.com")
        .updatedBy(REQUESTER)
        .build();

    when(accountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(existingAccount));
    when(accountRepository.update(any(Account.class))).thenReturn(updatedAccount);

    // When
    var result = underTest.process(context, input);

    // Then
    assertTrue(result.isSuccess());
    assertTrue(result.data().isPresent());

    var resultData = result.data().get();
    assertEquals(ACCOUNT_ID, resultData.id());
    assertEquals("Updated Savings", resultData.name());
    assertEquals("CASH", resultData.type());
    assertEquals("#00FF00", resultData.themeColor());

    verify(accountRepository).findById(ACCOUNT_ID);
    verify(accountRepository).update(any(Account.class));
  }

  @Test
  void givenUpdateRequestWithOnlyNameWhenProcessedThenShouldUpdateOnlyName() {
    // Given
    var input = UpdateAccountInput.builder()
        .id(ACCOUNT_ID)
        .name("New Name Only")
        .build();
    var context = Context.builder().requester(REQUESTER).build();

    var existingAccount = Account.builder()
        .id(Id.of(ACCOUNT_ID))
        .name("Old Name")
        .type(Account.Type.BANK)
        .themeColor("#FF5733")
        .balance(BigDecimal.valueOf(500))
        .user(User.builder().id(Id.of(REQUESTER)).build())
        .createdAt(LocalDateTime.now().minusDays(1))
        .updatedAt(LocalDateTime.now().minusDays(1))
        .createdBy("creator@email.com")
        .updatedBy("creator@email.com")
        .build();

    var updatedAccount = Account.builder()
        .id(Id.of(ACCOUNT_ID))
        .name("New Name Only")
        .type(Account.Type.BANK)
        .themeColor("#FF5733")
        .balance(BigDecimal.valueOf(500))
        .user(User.builder().id(Id.of(REQUESTER)).build())
        .createdAt(LocalDateTime.now().minusDays(1))
        .updatedAt(LocalDateTime.now())
        .createdBy("creator@email.com")
        .updatedBy(REQUESTER)
        .build();

    when(accountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(existingAccount));
    when(accountRepository.update(any(Account.class))).thenReturn(updatedAccount);

    // When
    var result = underTest.process(context, input);

    // Then
    assertTrue(result.isSuccess());
    assertTrue(result.data().isPresent());

    var resultData = result.data().get();
    assertEquals("New Name Only", resultData.name());
    assertEquals("BANK", resultData.type());
    assertEquals("#FF5733", resultData.themeColor());
  }

  @Test
  void givenUpdateRequestWithOnlyTypeWhenProcessedThenShouldUpdateOnlyType() {
    // Given
    var input = UpdateAccountInput.builder()
        .id(ACCOUNT_ID)
        .type(Account.Type.EWALLET)
        .build();
    var context = Context.builder().requester(REQUESTER).build();

    var existingAccount = Account.builder()
        .id(Id.of(ACCOUNT_ID))
        .name("My Account")
        .type(Account.Type.BANK)
        .themeColor("#FF5733")
        .balance(BigDecimal.valueOf(500))
        .user(User.builder().id(Id.of(REQUESTER)).build())
        .createdAt(LocalDateTime.now().minusDays(1))
        .updatedAt(LocalDateTime.now().minusDays(1))
        .createdBy("creator@email.com")
        .updatedBy("creator@email.com")
        .build();

    var updatedAccount = Account.builder()
        .id(Id.of(ACCOUNT_ID))
        .name("My Account")
        .type(Account.Type.EWALLET)
        .themeColor("#FF5733")
        .balance(BigDecimal.valueOf(500))
        .user(User.builder().id(Id.of(REQUESTER)).build())
        .createdAt(LocalDateTime.now().minusDays(1))
        .updatedAt(LocalDateTime.now())
        .createdBy("creator@email.com")
        .updatedBy(REQUESTER)
        .build();

    when(accountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(existingAccount));
    when(accountRepository.update(any(Account.class))).thenReturn(updatedAccount);

    // When
    var result = underTest.process(context, input);

    // Then
    assertTrue(result.isSuccess());
    assertTrue(result.data().isPresent());

    var resultData = result.data().get();
    assertEquals("EWALLET", resultData.type());
  }

  @Test
  void givenUpdateRequestWithOnlyThemeColorWhenProcessedThenShouldUpdateOnlyThemeColor() {
    // Given
    var input = UpdateAccountInput.builder()
        .id(ACCOUNT_ID)
        .themeColor("#AABBCC")
        .build();
    var context = Context.builder().requester(REQUESTER).build();

    var existingAccount = Account.builder()
        .id(Id.of(ACCOUNT_ID))
        .name("My Account")
        .type(Account.Type.BANK)
        .themeColor("#FF5733")
        .balance(BigDecimal.valueOf(500))
        .user(User.builder().id(Id.of(REQUESTER)).build())
        .createdAt(LocalDateTime.now().minusDays(1))
        .updatedAt(LocalDateTime.now().minusDays(1))
        .createdBy("creator@email.com")
        .updatedBy("creator@email.com")
        .build();

    var updatedAccount = Account.builder()
        .id(Id.of(ACCOUNT_ID))
        .name("My Account")
        .type(Account.Type.BANK)
        .themeColor("#AABBCC")
        .balance(BigDecimal.valueOf(500))
        .user(User.builder().id(Id.of(REQUESTER)).build())
        .createdAt(LocalDateTime.now().minusDays(1))
        .updatedAt(LocalDateTime.now())
        .createdBy("creator@email.com")
        .updatedBy(REQUESTER)
        .build();

    when(accountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(existingAccount));
    when(accountRepository.update(any(Account.class))).thenReturn(updatedAccount);

    // When
    var result = underTest.process(context, input);

    // Then
    assertTrue(result.isSuccess());
    assertTrue(result.data().isPresent());

    var resultData = result.data().get();
    assertEquals("#AABBCC", resultData.themeColor());
  }

  @Test
  void givenUpdateRequestWhenRepositoryThrowsExceptionThenShouldReturnFailure() {
    // Given
    var input = UpdateAccountInput.builder()
        .id(ACCOUNT_ID)
        .name("Updated Name")
        .build();
    var context = Context.builder().requester(REQUESTER).build();

    when(accountRepository.findById(ACCOUNT_ID))
        .thenThrow(new RuntimeException("Database error"));

    // When
    var result = underTest.process(context, input);

    // Then
    assertFalse(result.isSuccess());
    assertTrue(result.error().isPresent());
    assertEquals(Code.SERVER_ERROR, result.error().get().code());
    assertEquals("Database error", result.error().get().message());
  }

  @Test
  void givenUpdateRequestWhenAccountNotFoundThenShouldReturnNotFoundError() {
    // Given
    var input = UpdateAccountInput.builder()
        .id("non-existing-account")
        .name("Updated Name")
        .build();
    var context = Context.builder().requester(REQUESTER).build();

    when(accountRepository.findById("non-existing-account")).thenReturn(Optional.empty());

    // When
    var result = underTest.process(context, input);

    // Then
    assertFalse(result.isSuccess());
    assertTrue(result.error().isPresent());
    assertEquals(Code.NOT_FOUND, result.error().get().code());
    assertEquals("Account not found", result.error().get().message());
    verify(accountRepository).findById("non-existing-account");
    verify(accountRepository, never()).update(any(Account.class));
  }

  @Test
  void givenUpdateRequestWhenRequesterIsNotOwnerThenShouldReturnBadRequestError() {
    // Given
    var differentUser = "different@email.com";
    var input = UpdateAccountInput.builder()
        .id(ACCOUNT_ID)
        .name("Updated Name")
        .build();
    var context = Context.builder().requester(differentUser).build();

    var existingAccount = Account.builder()
        .id(Id.of(ACCOUNT_ID))
        .name("Original Name")
        .type(Account.Type.BANK)
        .themeColor("#FF5733")
        .balance(BigDecimal.valueOf(500))
        .user(User.builder().id(Id.of(REQUESTER)).build())
        .createdAt(LocalDateTime.now().minusDays(1))
        .updatedAt(LocalDateTime.now().minusDays(1))
        .createdBy(REQUESTER)
        .updatedBy(REQUESTER)
        .build();

    when(accountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(existingAccount));

    // When
    var result = underTest.process(context, input);

    // Then
    assertFalse(result.isSuccess());
    assertTrue(result.error().isPresent());
    assertEquals(Code.BAD_REQUEST, result.error().get().code());
    assertEquals("User can only update their own account", result.error().get().message());
    verify(accountRepository).findById(ACCOUNT_ID);
    verify(accountRepository, never()).update(any(Account.class));
  }

  @Test
  void givenUpdateRequestWhenRequesterIsOwnerThenShouldAllowUpdate() {
    // Given
    var input = UpdateAccountInput.builder()
        .id(ACCOUNT_ID)
        .name("Updated Name")
        .build();
    var context = Context.builder().requester(REQUESTER).build();

    var existingAccount = Account.builder()
        .id(Id.of(ACCOUNT_ID))
        .name("Original Name")
        .type(Account.Type.BANK)
        .themeColor("#FF5733")
        .balance(BigDecimal.valueOf(500))
        .user(User.builder().id(Id.of(REQUESTER)).build())
        .createdAt(LocalDateTime.now().minusDays(1))
        .updatedAt(LocalDateTime.now().minusDays(1))
        .createdBy(REQUESTER)
        .updatedBy(REQUESTER)
        .build();

    var updatedAccount = Account.builder()
        .id(Id.of(ACCOUNT_ID))
        .name("Updated Name")
        .type(Account.Type.BANK)
        .themeColor("#FF5733")
        .balance(BigDecimal.valueOf(500))
        .user(User.builder().id(Id.of(REQUESTER)).build())
        .createdAt(LocalDateTime.now().minusDays(1))
        .updatedAt(LocalDateTime.now())
        .createdBy(REQUESTER)
        .updatedBy(REQUESTER)
        .build();

    when(accountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(existingAccount));
    when(accountRepository.update(any(Account.class))).thenReturn(updatedAccount);

    // When
    var result = underTest.process(context, input);

    // Then
    assertTrue(result.isSuccess());
    assertTrue(result.data().isPresent());
    assertEquals("Updated Name", result.data().get().name());
    verify(accountRepository).findById(ACCOUNT_ID);
    verify(accountRepository).update(any(Account.class));
  }
}
