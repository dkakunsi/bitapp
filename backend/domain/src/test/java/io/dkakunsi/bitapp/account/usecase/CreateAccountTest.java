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
import io.dkakunsi.bitapp.account.entity.Account;
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
        .type("BANK")
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
    assertEquals(createRequest.type(), resultData.type().toString());
    assertEquals(createRequest.themeColor(), resultData.themeColor());
    assertEquals(BigDecimal.ZERO, resultData.balance());
    assertEquals(REQUESTER, resultData.user());

    // verify data passed to repository
    var savingAccountCaptor = ArgumentCaptor.forClass(Account.class);
    verify(accountRepository).create(savingAccountCaptor.capture());
    var capturedAccount = savingAccountCaptor.getValue();
    assertEquals(createRequest.name(), capturedAccount.name());
    assertEquals(createRequest.type(), capturedAccount.type().toString());
    assertEquals(createRequest.themeColor(), capturedAccount.themeColor());
    assertEquals(BigDecimal.ZERO, capturedAccount.balance());
    assertEquals(REQUESTER, capturedAccount.createdBy());
    assertEquals(REQUESTER, capturedAccount.updatedBy());
    assertNotNull(capturedAccount.createdAt());
    assertNotNull(capturedAccount.updatedAt());
    assertNotNull(capturedAccount.id());
    assertEquals(REQUESTER, capturedAccount.user().value());
  }

  @Test
  void givenValidInsertAccountRequestWhenAccountPortThrowAnExceptionThenShouldFail() {
    // Given
    final var createRequest = CreateAccountInput.builder()
        .name("Savings Account")
        .type("BANK")
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

  @Test
  void givenCreateAccountRequestWithMinimalFieldsWhenProcessedThenShouldSucceed() {
    // Given
    final var createRequest = CreateAccountInput.builder()
        .name("Minimal Account")
        .type("CASH")
        .build();
    final var context = Context.builder().requester(REQUESTER).build();

    when(accountRepository.create(any())).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    final var result = underTest.process(context, createRequest);

    // Then
    assertTrue(result.isSuccess());
    assertTrue(result.data().isPresent());

    final var resultData = result.data().get();
    assertEquals("Minimal Account", resultData.name());
    assertEquals("CASH", resultData.type().toString());
  }

  @Test
  void givenCreateAccountRequestWithAllAccountTypesWhenProcessedThenShouldSucceed() {
    // Given
    String[] accountTypes = { "BANK", "CASH", "EWALLET", "OTHER" };
    final var context = Context.builder().requester(REQUESTER).build();

    for (String type : accountTypes) {
      final var createRequest = CreateAccountInput.builder()
          .name(type + " Account")
          .type(type)
          .themeColor("#FF5733")
          .build();

      when(accountRepository.create(any())).thenAnswer(invocation -> invocation.getArgument(0));

      // When
      final var result = underTest.process(context, createRequest);

      // Then
      assertTrue(result.isSuccess(), "Should create account with type: " + type);
      assertTrue(result.data().isPresent());
      assertEquals(type, result.data().get().type().toString());
    }
  }

  @Test
  void givenCreateAccountRequestWithDifferentThemeColorsWhenProcessedThenShouldPreserveThemeColor() {
    // Given
    final var themeColor = "#00FF00";
    final var createRequest = CreateAccountInput.builder()
        .name("Green Account")
        .type("BANK")
        .themeColor(themeColor)
        .build();
    final var context = Context.builder().requester(REQUESTER).build();

    when(accountRepository.create(any())).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    final var result = underTest.process(context, createRequest);

    // Then
    assertTrue(result.isSuccess());
    assertTrue(result.data().isPresent());
    assertEquals(themeColor, result.data().get().themeColor());

    var savingAccountCaptor = ArgumentCaptor.forClass(Account.class);
    verify(accountRepository).create(savingAccountCaptor.capture());
    var capturedAccount = savingAccountCaptor.getValue();
    assertEquals(themeColor, capturedAccount.themeColor());
  }

  @Test
  void givenCreateAccountRequestWhenProcessedThenShouldInitializeBalanceToZero() {
    // Given
    final var createRequest = CreateAccountInput.builder()
        .name("New Account")
        .type("CASH")
        .themeColor("#FF5733")
        .build();
    final var context = Context.builder().requester(REQUESTER).build();

    when(accountRepository.create(any())).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    final var result = underTest.process(context, createRequest);

    // Then
    assertTrue(result.isSuccess());
    assertTrue(result.data().isPresent());
    assertEquals(BigDecimal.ZERO, result.data().get().balance());

    var savingAccountCaptor = ArgumentCaptor.forClass(Account.class);
    verify(accountRepository).create(savingAccountCaptor.capture());
    var capturedAccount = savingAccountCaptor.getValue();
    assertEquals(BigDecimal.ZERO, capturedAccount.balance());
  }
}
