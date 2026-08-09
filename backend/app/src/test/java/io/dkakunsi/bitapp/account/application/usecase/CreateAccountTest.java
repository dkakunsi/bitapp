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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import io.dkakunsi.bitapp.Context;
import io.dkakunsi.bitapp.Result;
import io.dkakunsi.bitapp.Result.ErrorCode;
import io.dkakunsi.bitapp.account.application.dto.CreateAccountInput;
import io.dkakunsi.bitapp.account.domain.entity.Account;
import io.dkakunsi.bitapp.account.domain.repository.AccountRepository;

public final class CreateAccountTest {

  private static final String REQUESTER = "Requester";

  private static final Context context = Context.builder().requester(REQUESTER).build();

  private CreateAccount underTest;

  private AccountRepository accountRepository;

  @BeforeEach
  void setup() {
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

    when(accountRepository.create(any())).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    final var result = Context.executeInContext(context, () -> underTest.process(createRequest));

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

    // verify data passed to repository
    var savingAccountCaptor = ArgumentCaptor.forClass(Account.class);
    verify(accountRepository).create(savingAccountCaptor.capture());
    var capturedAccount = savingAccountCaptor.getValue();
    assertEquals(createRequest.name(), capturedAccount.name());
    assertEquals(createRequest.type(), capturedAccount.type().toString());
    assertEquals(createRequest.themeColor(), capturedAccount.themeColor());
    assertEquals(BigDecimal.ZERO, capturedAccount.balance());
    assertNotNull(capturedAccount.createdAt());
    assertNotNull(capturedAccount.updatedAt());
    assertNotNull(capturedAccount.id());
  }

  @Test
  void givenValidInsertAccountRequestWhenAccountPortThrowAnExceptionThenShouldFail() {
    // Given
    final var createRequest = CreateAccountInput.builder()
        .name("Savings Account")
        .type("BANK")
        .themeColor("#FF5733")
        .build();

    when(accountRepository.create(any())).thenThrow(new RuntimeException("An error occurred"));

    // When
    final var result = Context.executeInContext(context, () -> underTest.process(createRequest));

    // Then
    assertFalse(result.isSuccess());

    assertEquals(ErrorCode.INTERNAL_ERROR, result.errorCode().get());
    assertEquals(Result.DEFAULT_ERROR_MESSAGE, result.errorMessage().get());
  }

  @Test
  void givenCreateAccountRequestWithMinimalFieldsWhenProcessedThenShouldSucceed() {
    // Given
    final var createRequest = CreateAccountInput.builder()
        .name("Minimal Account")
        .type("CASH")
        .build();

    when(accountRepository.create(any())).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    final var result = Context.executeInContext(context, () -> underTest.process(createRequest));

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

    for (String type : accountTypes) {
      final var createRequest = CreateAccountInput.builder()
          .name(type + " Account")
          .type(type)
          .themeColor("#FF5733")
          .build();

      when(accountRepository.create(any())).thenAnswer(invocation -> invocation.getArgument(0));

      // When
      final var result = Context.executeInContext(context, () -> underTest.process(createRequest));

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

    when(accountRepository.create(any())).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    final var result = Context.executeInContext(context, () -> underTest.process(createRequest));

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

    when(accountRepository.create(any())).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    final var result = Context.executeInContext(context, () -> underTest.process(createRequest));

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
