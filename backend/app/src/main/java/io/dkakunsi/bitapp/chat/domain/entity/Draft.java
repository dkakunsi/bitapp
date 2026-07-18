package io.dkakunsi.bitapp.chat.domain.entity;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import io.dkakunsi.bitapp.Id;
import io.dkakunsi.bitapp.chat.domain.entity.Chat.Type;
import lombok.Builder;

@Builder
public record Draft(
    Id id,
    Id userId,
    Type type,
    String error,
    JSONObject data,
    List<ExternalData> externalData,
    Boolean success,
    Boolean confirmed) {

  public Draft update(PromptResult promptResult, PromptMessage promptMessage) {
    var externalData = promptMessage.getExternalData();
    var promptResultData = new JSONObject(promptResult.data());
    var promptResultError = promptResult.error();

    var success = StringUtils.isBlank(promptResultError);
    var confirmed = (success == false);

    return Draft.builder()
        .id(this.id)
        .userId(this.userId)
        .type(this.type)
        .error(promptResultError)
        .data(promptResultData)
        .externalData(externalData)
        .success(success)
        .confirmed(confirmed)
        .build();
  }

  public static Draft from(Chat chat, String requester) {
    return Draft.builder()
        .id(Id.generate())
        .userId(Id.of(requester))
        .type(chat.type())
        .data(new JSONObject())
        .externalData(List.of())
        .confirmed(false)
        .build();
  }

  public ExternalData getExternalDataByName(String name, Class<? extends ExternalData> type) {
    return externalData.stream()
        .filter(data -> data.getName().equals(name) && type.isInstance(data))
        .findFirst()
        .orElse(null);
  }

  public Draft confirm(boolean success) {
    return Draft.builder()
        .id(this.id)
        .userId(this.userId)
        .type(this.type)
        .error(this.error)
        .data(this.data)
        .externalData(this.externalData)
        .success(success)
        .confirmed(true)
        .build();
  }
}
