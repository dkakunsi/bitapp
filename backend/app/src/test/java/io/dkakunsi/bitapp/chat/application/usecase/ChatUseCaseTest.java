package io.dkakunsi.bitapp.chat.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import io.dkakunsi.bitapp.AppError.Code;
import io.dkakunsi.bitapp.Context;
import io.dkakunsi.bitapp.Id;
import io.dkakunsi.bitapp.chat.domain.entity.Chat;
import io.dkakunsi.bitapp.chat.domain.entity.Draft;
import io.dkakunsi.bitapp.chat.domain.entity.PromptMessage;
import io.dkakunsi.bitapp.chat.domain.entity.PromptResult;
import io.dkakunsi.bitapp.chat.domain.repository.DraftRepository;
import io.dkakunsi.bitapp.chat.domain.repository.LanguageModelRepository;

public final class ChatUseCaseTest {

  private static final String REQUESTER = "user@email.com";
  private static final Context CONTEXT = Context.builder().requester(REQUESTER).build();

  private ChatUseCase underTest;
  private LanguageModelRepository languageModelRepository;
  private DraftRepository draftRepository;
  private PromptMessage.PromptMessageBuilder promptMessageBuilder;

  @BeforeEach
  void setUp() {
    languageModelRepository = mock(LanguageModelRepository.class);
    draftRepository = mock(DraftRepository.class);
    promptMessageBuilder = mock(PromptMessage.PromptMessageBuilder.class);

    Map<Chat.Type, PromptMessage.PromptMessageBuilder> promptMessageBuilders = new EnumMap<>(Chat.Type.class);
    promptMessageBuilders.put(Chat.Type.ACCOUNT, promptMessageBuilder);

    underTest = new ChatUseCase(languageModelRepository, draftRepository, promptMessageBuilders);
  }

  @Test
  void createDraftWhenNotFoundThenPersistUpdatedData() {
    // Given
    var chat = new Chat(Chat.Type.ACCOUNT, "draft-1", "create account for food", "en");
    var promptMessage = new PromptMessage("prompt text");
    var promptResult = new PromptResult(true, null, "{\"name\":\"Food Account\",\"balance\":50000}");

    when(draftRepository.findOne(chat.draftId())).thenReturn(Optional.empty());
    when(promptMessageBuilder.build(eq(chat), any(Draft.class))).thenReturn(promptMessage);
    when(languageModelRepository.prompt(promptMessage)).thenReturn(promptResult);

    // When
    var result = Context.executeInContext(CONTEXT, () -> underTest.execute(chat));

    // Then
    assertTrue(result.isSuccess());
    assertTrue(result.data().isPresent());

    var builtDraftCaptor = ArgumentCaptor.forClass(Draft.class);
    verify(promptMessageBuilder).build(eq(chat), builtDraftCaptor.capture());

    var builtDraft = builtDraftCaptor.getValue();
    assertEquals(Chat.Type.ACCOUNT, builtDraft.type());
    assertEquals(Id.of(REQUESTER), builtDraft.userId());

    var savedDraftCaptor = ArgumentCaptor.forClass(Draft.class);
    verify(draftRepository).save(savedDraftCaptor.capture());

    var savedDraft = savedDraftCaptor.getValue();
    assertEquals("Food Account", savedDraft.data().getString("name"));
    assertEquals(50000, savedDraft.data().getInt("balance"));
    assertEquals(savedDraft, result.data().get());
  }

  @Test
  void useExistingDraftWhenFoundThenPersistUpdatedData() {
    // Given
    var chat = new Chat(Chat.Type.ACCOUNT, "draft-2", "update existing draft", "en");
    var existingDraft = new Draft(
        Id.of("draft-2"),
        Id.of("owner@email.com"),
        Chat.Type.ACCOUNT,
        new JSONObject("{\"existing\":\"value\"}"));
    var promptMessage = new PromptMessage("prompt text");
    var promptResult = new PromptResult(true, null, "{\"title\":\"Updated Draft\"}");

    when(draftRepository.findOne(chat.draftId())).thenReturn(Optional.of(existingDraft));
    when(promptMessageBuilder.build(chat, existingDraft)).thenReturn(promptMessage);
    when(languageModelRepository.prompt(promptMessage)).thenReturn(promptResult);

    // When
    var result = Context.executeInContext(CONTEXT, () -> underTest.execute(chat));

    // Then
    assertTrue(result.isSuccess());
    assertTrue(result.data().isPresent());

    verify(promptMessageBuilder).build(chat, existingDraft);

    var savedDraftCaptor = ArgumentCaptor.forClass(Draft.class);
    verify(draftRepository).save(savedDraftCaptor.capture());

    var savedDraft = savedDraftCaptor.getValue();
    assertEquals(existingDraft.id(), savedDraft.id());
    assertEquals(existingDraft.userId(), savedDraft.userId());
    assertEquals(existingDraft.type(), savedDraft.type());
    assertEquals("Updated Draft", savedDraft.data().getString("title"));
  }

  @Test
  void processReturnServerErrorWhenPromptBuilderIsMissing() {
    // Given
    var chat = new Chat(Chat.Type.LOAN, "draft-3", "create loan", "en");
    var useCaseWithoutLoanBuilder = new ChatUseCase(
        languageModelRepository,
        draftRepository,
        Map.of(Chat.Type.ACCOUNT, promptMessageBuilder));

    when(draftRepository.findOne(chat.draftId())).thenReturn(Optional.empty());

    // When
    var result = Context.executeInContext(CONTEXT, () -> useCaseWithoutLoanBuilder.process(chat));

    // Then
    assertFalse(result.isSuccess());
    assertTrue(result.error().isPresent());
    assertEquals(Code.SERVER_ERROR, result.error().get().code());
    assertTrue(result.data().isEmpty());
  }
}