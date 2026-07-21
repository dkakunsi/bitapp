package io.dkakunsi.bitapp.draft.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import io.dkakunsi.bitapp.Context;
import io.dkakunsi.bitapp.Id;
import io.dkakunsi.bitapp.Result.ErrorCode;
import io.dkakunsi.bitapp.draft.domain.entity.Chat;
import io.dkakunsi.bitapp.draft.domain.entity.Draft;
import io.dkakunsi.bitapp.draft.domain.repository.DraftRepository;
import io.dkakunsi.bitapp.draft.domain.repository.LanguageModelRepository;
import io.dkakunsi.bitapp.langchain.PromptMessage;
import io.dkakunsi.bitapp.langchain.PromptResult;

public final class CreateDraftTest {

  private static final String REQUESTER = "user@email.com";
  private static final Context CONTEXT = Context.builder().requester(REQUESTER).build();

  private CreateDraft underTest;
  private LanguageModelRepository languageModelRepository;
  private DraftRepository draftRepository;
  private PromptMessage.PromptMessageBuilder<Draft> promptMessageBuilder;

  @BeforeEach
  void setUp() {
    languageModelRepository = mock(LanguageModelRepository.class);
    draftRepository = mock(DraftRepository.class);
    promptMessageBuilder = mock(PromptMessage.PromptMessageBuilder.class);
    // noinspection unchecked,rawtypes

    Map<Chat.Type, PromptMessage.PromptMessageBuilder<Draft>> promptMessageBuilders = new EnumMap<>(Chat.Type.class);
    promptMessageBuilders.put(Chat.Type.ACCOUNT, promptMessageBuilder);

    underTest = new CreateDraft(languageModelRepository, draftRepository, promptMessageBuilders);
  }

  @Test
  void createDraftWhenNotFoundThenPersistUpdatedData() {
    // Given
    var chat = new Chat(Chat.Type.ACCOUNT, "draft-1", "create account for food", "en");
    var promptMessage = mock(PromptMessage.class);
    var promptResult = new PromptResult(null, "{\"name\":\"Food Account\",\"balance\":50000}", List.of());

    when(draftRepository.findByIdAndNotConfirmed(Id.of(chat.draftId()))).thenReturn(Optional.empty());
    when(promptMessageBuilder.build(any(Draft.class))).thenReturn(promptMessage);
    when(languageModelRepository.prompt(promptMessage)).thenReturn(promptResult);

    // When
    var result = Context.executeInContext(CONTEXT, () -> underTest.execute(chat));

    // Then
    assertTrue(result.isSuccess());
    assertTrue(result.data().isPresent());

    var builtDraftCaptor = ArgumentCaptor.forClass(Draft.class);
    verify(promptMessageBuilder).build(builtDraftCaptor.capture());

    var builtDraft = builtDraftCaptor.getValue();
    assertEquals(Chat.Type.ACCOUNT, builtDraft.type());
    assertEquals(Id.of(REQUESTER), builtDraft.userId());

    var savedDraftCaptor = ArgumentCaptor.forClass(Draft.class);
    verify(draftRepository).save(savedDraftCaptor.capture());

    var savedDraft = savedDraftCaptor.getValue();
    assertEquals("Food Account", savedDraft.modelResult().getString("name"));
    assertEquals(50000, savedDraft.modelResult().getInt("balance"));
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
        List.of(new Chat(Chat.Type.ACCOUNT, "draft-2", "existing draft", "en")),
        null,
        new JSONObject("{\"existing\":\"value\"}"),
        List.of(),
        false,
        false);
    var promptMessage = mock(PromptMessage.class);
    var promptResult = new PromptResult(null, "{\"title\":\"Updated Draft\"}", List.of());

    when(draftRepository.findByIdAndNotConfirmed(Id.of(chat.draftId()))).thenReturn(Optional.of(existingDraft));
    when(promptMessageBuilder.build(any(Draft.class))).thenReturn(promptMessage);
    when(languageModelRepository.prompt(promptMessage)).thenReturn(promptResult);

    // When
    var result = Context.executeInContext(CONTEXT, () -> underTest.execute(chat));

    // Then
    assertTrue(result.isSuccess());
    assertTrue(result.data().isPresent());

    var builtDraftCaptor = ArgumentCaptor.forClass(Draft.class);
    verify(promptMessageBuilder).build(builtDraftCaptor.capture());

    var builtDraft = builtDraftCaptor.getValue();
    assertEquals(existingDraft.id(), builtDraft.id());
    assertEquals(existingDraft.userId(), builtDraft.userId());

    var savedDraftCaptor = ArgumentCaptor.forClass(Draft.class);
    verify(draftRepository).save(savedDraftCaptor.capture());

    var savedDraft = savedDraftCaptor.getValue();
    assertEquals(existingDraft.id(), savedDraft.id());
    assertEquals(existingDraft.userId(), savedDraft.userId());
    assertEquals(existingDraft.type(), savedDraft.type());
    assertEquals("Updated Draft", savedDraft.modelResult().getString("title"));
  }

  @Test
  void processReturnServerErrorWhenPromptBuilderIsMissing() {
    // Given
    var chat = new Chat(Chat.Type.LOAN, "draft-3", "create loan", "en");
    var useCaseWithoutLoanBuilder = new CreateDraft(
        languageModelRepository,
        draftRepository,
        Map.of(Chat.Type.ACCOUNT, promptMessageBuilder));

    when(draftRepository.findByIdAndNotConfirmed(Id.of(chat.draftId()))).thenReturn(Optional.empty());

    // When
    var result = Context.executeInContext(CONTEXT, () -> useCaseWithoutLoanBuilder.process(chat));

    // Then
    assertFalse(result.isSuccess());
    assertTrue(result.errorCode().isPresent());
    assertEquals(ErrorCode.INTERNAL_ERROR, result.errorCode().get());
    assertTrue(result.data().isEmpty());
  }
}