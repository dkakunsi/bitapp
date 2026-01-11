package io.dkakunsi.bitapp.account.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import io.dkakunsi.bitapp.account.dto.CreateAccountInput;
import io.dkakunsi.bitapp.account.model.Account;
import io.dkakunsi.bitapp.account.repository.AccountRepository;
import io.dkakunsi.bitapp.common.AppError.Code;
import io.dkakunsi.bitapp.common.Context;

public final class CreateAccountTest {

  private CreateAccount underTest;

  private AccountRepository accountRepository;

  private static final String REQUESTER = "Requester";

  @BeforeEach
  void setUp() {
    accountRepository = mock(AccountRepository.class);
    underTest = new CreateAccount(accountRepository);
  }

  @Test
  void givenValidInsertAccountRequestWhenAccountDoesNotExistsThenShouldSuccessfullyCreated() {
    // Given
    final var createRequest = CreateAccountInput.builder()
        .name("Savings Account")
        .type(Account.Type.BANK)
        .themeColor("#FF5733")
        .build();
    final var context = Context.builder().requester(REQUESTER).build();

    when(accountRepository.create(any())).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    final var result = underTest.process(context, createRequest);

    // Then
    assertTrue(result.isSuccess());
    assertTrue(result.data().isPresent());

    // verify returned data
    final var resultData = result.data().get();
    assertNotNull(resultData.id());
    assertEquals(createRequest.name(), resultData.name());
    assertEquals(createRequest.type(), resultData.type());
    assertEquals(createRequest.themeColor(), resultData.themeColor());
    assertEquals(BigDecimal.ZERO, resultData.balance());
    assertEquals(REQUESTER, resultData.user());

    // verify data passed to repository
    var savingAccountCaptor = ArgumentCaptor.forClass(Account.class);
    verify(accountRepository).create(savingAccountCaptor.capture());
    var capturedAccount = savingAccountCaptor.getValue();
    assertEquals(createRequest.name(), capturedAccount.getName());
    assertEquals(createRequest.type(), capturedAccount.getType());
    assertEquals(createRequest.themeColor(), capturedAccount.getThemeColor());
    assertEquals(BigDecimal.ZERO, capturedAccount.getBalance());
    assertEquals(REQUESTER, capturedAccount.getCreatedBy());
    assertEquals(REQUESTER, capturedAccount.getUpdatedBy());
    assertNotNull(capturedAccount.getCreatedAt());
    assertNotNull(capturedAccount.getUpdatedAt());
    assertNotNull(capturedAccount.getId());
    assertEquals(REQUESTER, capturedAccount.getUser().getId().value());
  }

  @Test
  void givenValidInsertAccountRequestWhenAccountPortThrowAnExceptionThenShouldFail() {
    // Given
    final var createRequest = CreateAccountInput.builder()
        .name("Savings Account")
        .type(Account.Type.BANK)
        .themeColor("#FF5733")
        .build();
    final var context = Context.builder().requester(REQUESTER).build();

    when(accountRepository.create(any())).thenThrow(new RuntimeException("An error occurred"));

    // When
    final var result = underTest.process(context, createRequest);

    // Then
    assertFalse(result.isSuccess());

    final var error = result.error().get();
    assertEquals(Code.SERVER_ERROR, error.code());
    assertEquals("An error occurred", error.message());
  }
}
