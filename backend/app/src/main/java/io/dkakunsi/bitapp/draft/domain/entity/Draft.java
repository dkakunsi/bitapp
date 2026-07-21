package io.dkakunsi.bitapp.draft.domain.entity;

import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import io.dkakunsi.bitapp.CrossDomainReference;
import io.dkakunsi.bitapp.Id;
import io.dkakunsi.bitapp.draft.domain.entity.Chat.Type;
import io.dkakunsi.bitapp.langchain.PromptResult;
import lombok.Builder;

@Builder
public record Draft(
    Id id,
    Id userId,
    Type type,
    List<Chat> chats,
    String modelError,
    JSONObject modelResult,
    List<CrossDomainReference> crossDomainReferences,
    Boolean success,
    Boolean confirmed) {

  public Chat getLastChat() {
    if (chats == null || chats.isEmpty()) {
      return null;
    }
    return chats.getLast();
  }

  public static Draft from(Chat chat, String requester) {
    return Draft.builder()
        .id(Id.generate())
        .userId(Id.of(requester))
        .type(chat.type())
        .chats(List.of(chat))
        .modelResult(new JSONObject())
        .crossDomainReferences(List.of())
        .confirmed(false)
        .build();
  }

  public Draft addChat(Chat chat) {
    var updatedChats = Stream.concat(this.chats.stream(), Stream.of(chat)).toList();

    return Draft.builder()
        .id(this.id)
        .userId(this.userId)
        .type(this.type)
        .chats(updatedChats)
        .modelError(this.modelError)
        .modelResult(this.modelResult)
        .crossDomainReferences(this.crossDomainReferences)
        .success(this.success)
        .confirmed(this.confirmed)
        .build();
  }

  public Draft update(PromptResult promptResult) {
    var crossDomainReferences = promptResult.crossDomainReferences();
    var promptResultData = new JSONObject(promptResult.data());
    var promptResultError = promptResult.error();

    var success = StringUtils.isBlank(promptResultError);
    var confirmed = (success == false);

    return Draft.builder()
        .id(this.id)
        .userId(this.userId)
        .type(this.type)
        .modelError(promptResultError)
        .modelResult(promptResultData)
        .crossDomainReferences(crossDomainReferences)
        .success(success)
        .confirmed(confirmed)
        .build();
  }

  public CrossDomainReference getCrossDomainReferenceByName(String name, Class<? extends CrossDomainReference> type) {
    return crossDomainReferences.stream()
        .filter(data -> data.getName().equals(name) && type.isInstance(data))
        .findFirst()
        .orElse(null);
  }

  public Draft confirm(boolean success) {
    return Draft.builder()
        .id(this.id)
        .userId(this.userId)
        .type(this.type)
        .modelError(this.modelError)
        .modelResult(this.modelResult)
        .crossDomainReferences(this.crossDomainReferences)
        .success(success)
        .confirmed(true)
        .build();
  }
}
