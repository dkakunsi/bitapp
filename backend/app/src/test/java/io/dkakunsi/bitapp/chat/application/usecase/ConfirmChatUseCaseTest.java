package io.dkakunsi.bitapp.chat.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.dkakunsi.bitapp.AppError.Code;
import io.dkakunsi.bitapp.Context;
import io.dkakunsi.bitapp.Id;
import io.dkakunsi.bitapp.Result;
import io.dkakunsi.bitapp.chat.application.port.AccountPort;
import io.dkakunsi.bitapp.chat.application.port.LoanPort;
import io.dkakunsi.bitapp.chat.application.port.TransactionPort;
import io.dkakunsi.bitapp.chat.domain.entity.Chat;
import io.dkakunsi.bitapp.chat.domain.entity.Draft;
import io.dkakunsi.bitapp.chat.domain.repository.DraftRepository;

public final class ConfirmChatUseCaseTest {

  private static final String REQUESTER = "user@email.com";
  private static final Context CONTEXT = Context.builder().requester(REQUESTER).build();

  private ConfirmChatUseCase underTest;
  private DraftRepository draftRepository;
  private AccountPort accountPort;
  private LoanPort loanPort;
  private TransactionPort transactionPort;

  @BeforeEach
  void setUp() {
    draftRepository = mock(DraftRepository.class);
    accountPort = mock(AccountPort.class);
    loanPort = mock(LoanPort.class);
    transactionPort = mock(TransactionPort.class);

    underTest = new ConfirmChatUseCase(draftRepository, accountPort, loanPort, transactionPort);
  }

  @Test
  void returnNotFoundWhenDraftDoesNotExist() {
    // Given
    var draftId = "draft-123";
    when(draftRepository.findById(Id.of(draftId))).thenReturn(Optional.empty());

    // When
    var result = Context.executeInContext(CONTEXT, () -> underTest.execute(draftId));

    // Then
    assertFalse(result.isSuccess());
    assertTrue(result.error().isPresent());
    assertEquals(Code.NOT_FOUND, result.error().get().code());
  }

  @Test
  void returnForbiddenWhenRequesterDoesNotMatchDraftOwner() {
    // Given
    var draftId = "draft-456";
    var otherUser = "other@email.com";
    var draft = new Draft(Id.of(draftId), Id.of(otherUser), Chat.Type.ACCOUNT,
        new JSONObject(), List.of());

    when(draftRepository.findById(Id.of(draftId))).thenReturn(Optional.of(draft));

    // When
    var result = Context.executeInContext(CONTEXT, () -> underTest.execute(draftId));

    // Then
    assertFalse(result.isSuccess());
    assertTrue(result.error().isPresent());
    assertEquals(Code.FORBIDDEN, result.error().get().code());

    verify(accountPort, never()).createAccount(draft);
    verify(loanPort, never()).createLoan(draft);
    verify(transactionPort, never()).createTransaction(draft);
  }

  @Test
  void delegateToAccountPortWhenDraftTypeIsAccount() {
    // Given
    var draftId = "draft-account";
    var draft = new Draft(Id.of(draftId), Id.of(REQUESTER), Chat.Type.ACCOUNT,
        new JSONObject("{\"name\":\"Savings\"}"), List.of());

    when(draftRepository.findById(Id.of(draftId))).thenReturn(Optional.of(draft));
    when(accountPort.createAccount(draft)).thenReturn(Result.success());

    // When
    var result = Context.executeInContext(CONTEXT, () -> underTest.execute(draftId));

    // Then
    assertTrue(result.isSuccess());
    verify(accountPort).createAccount(draft);
    verify(loanPort, never()).createLoan(draft);
    verify(transactionPort, never()).createTransaction(draft);
  }

  @Test
  void delegateToLoanPortWhenDraftTypeIsLoan() {
    // Given
    var draftId = "draft-loan";
    var draft = new Draft(Id.of(draftId), Id.of(REQUESTER), Chat.Type.LOAN,
        new JSONObject("{\"amount\":10000}"), List.of());

    when(draftRepository.findById(Id.of(draftId))).thenReturn(Optional.of(draft));
    when(loanPort.createLoan(draft)).thenReturn(Result.success());

    // When
    var result = Context.executeInContext(CONTEXT, () -> underTest.execute(draftId));

    // Then
    assertTrue(result.isSuccess());
    verify(loanPort).createLoan(draft);
    verify(accountPort, never()).createAccount(draft);
    verify(transactionPort, never()).createTransaction(draft);
  }

  @Test
  void delegateToTransactionPortWhenDraftTypeIsTransaction() {
    // Given
    var draftId = "draft-transaction";
    var draft = new Draft(Id.of(draftId), Id.of(REQUESTER), Chat.Type.TRANSACTION,
        new JSONObject("{\"amount\":500}"), List.of());

    when(draftRepository.findById(Id.of(draftId))).thenReturn(Optional.of(draft));
    when(transactionPort.createTransaction(draft)).thenReturn(Result.success());

    // When
    var result = Context.executeInContext(CONTEXT, () -> underTest.execute(draftId));

    // Then
    assertTrue(result.isSuccess());
    verify(transactionPort).createTransaction(draft);
    verify(accountPort, never()).createAccount(draft);
    verify(loanPort, never()).createLoan(draft);
  }

  @Test
  void propagateFailureFromAccountPort() {
    // Given
    var draftId = "draft-account-fail";
    var draft = new Draft(Id.of(draftId), Id.of(REQUESTER), Chat.Type.ACCOUNT,
        new JSONObject(), List.of());

    when(draftRepository.findById(Id.of(draftId))).thenReturn(Optional.of(draft));
    when(accountPort.createAccount(draft))
        .thenReturn(Result.failure(Code.SERVER_ERROR, "Account creation failed"));

    // When
    var result = Context.executeInContext(CONTEXT, () -> underTest.execute(draftId));

    // Then
    assertFalse(result.isSuccess());
    assertTrue(result.error().isPresent());
    assertEquals(Code.SERVER_ERROR, result.error().get().code());
  }
}
