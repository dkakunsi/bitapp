package io.dkakunsi.bitapp.chat.domain.entity;

import org.json.JSONObject;

import io.dkakunsi.bitapp.Id;
import io.dkakunsi.bitapp.chat.domain.entity.Chat.Type;

public record Draft(
    Id id,
    Id userId,
    Type type,
    JSONObject data) {

  public static Draft of(String userId, Type type, String message, String language) {
    return new Draft(
        Id.generate(),
        Id.of(userId),
        type,
        new JSONObject());
  }

  public Draft updateData(JSONObject newData) {
    return new Draft(this.id, this.userId, this.type, newData);
  }

  public static Draft from(Chat chat, String requester) {
    return Draft.of(requester, chat.type(), chat.message(), chat.language());
  }

}
