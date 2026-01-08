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
import io.dkakunsi.bitapp.common.Context;
import io.dkakunsi.bitapp.common.Input;
import io.dkakunsi.bitapp.common.AppError.Code;

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
    final var input = new Input<>(createRequest, context);

    when(accountRepository.create(any())).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    final var result = underTest.process(input);

    // Then
    assertTrue(result.isSuccess());
    assertTrue(result.data().isPresent());

    // verify returned data
    final var resultData = result.data().get();
    assertNotNull(resultData.getId());
    assertEquals(createRequest.name(), resultData.getName());
    assertEquals(createRequest.type(), resultData.getType());
    assertEquals(createRequest.themeColor(), resultData.getThemeColor());
    assertEquals(BigDecimal.ZERO, resultData.getBalance());
    assertEquals(REQUESTER, resultData.getCreatedBy());
    assertEquals(REQUESTER, resultData.getUpdatedBy());
    assertNotNull(resultData.getCreatedAt());
    assertNotNull(resultData.getUpdatedAt());
    assertEquals(REQUESTER, resultData.getUser().getId().value());

    // verify data passed to port
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
    final var input = new Input<>(createRequest, context);

    when(accountRepository.create(any())).thenThrow(new RuntimeException("An error occurred"));

    // When
    final var result = underTest.process(input);

    // Then
    assertFalse(result.isSuccess());

    final var error = result.error().get();
    assertEquals(Code.SERVER_ERROR, error.code());
    assertEquals("An error occurred", error.message());
  }
}
