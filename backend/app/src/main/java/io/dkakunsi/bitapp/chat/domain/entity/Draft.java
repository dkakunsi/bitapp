package io.dkakunsi.bitapp.chat.domain.entity;

import java.util.List;

import org.json.JSONObject;

import io.dkakunsi.bitapp.Id;
import io.dkakunsi.bitapp.chat.domain.entity.Chat.Type;

public record Draft(
    Id id,
    Id userId,
    Type type,
    JSONObject data,
    List<ExternalData> externalData) {

  public Draft update(JSONObject newData, List<ExternalData> newExternalData) {
    return new Draft(this.id, this.userId, this.type, newData, newExternalData);
  }

  public static Draft from(Chat chat, String requester) {
    return new Draft(
        Id.generate(),
        Id.of(requester),
        chat.type(),
        new JSONObject(),
        List.of());
  }
}
