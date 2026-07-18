package io.dkakunsi.bitapp.chat.infrastructure.mongo.model;

import org.bson.Document;
import org.json.JSONObject;

import dev.morphia.annotations.Entity;
import io.dkakunsi.bitapp.Id;
import io.dkakunsi.bitapp.chat.domain.entity.Chat;
import io.dkakunsi.bitapp.chat.domain.entity.Draft;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity("loans")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DraftModel {

  @dev.morphia.annotations.Id
  private String id;

  private String userId;
  private String type;
  private Document data;

  public Draft toDraft() {
    return new Draft(
        Id.of(this.id),
        Id.of(this.userId),
        Chat.Type.valueOf(this.type),
        new JSONObject(this.data.toJson()));
  }

  public static DraftModel from(Draft draft) {
    return new DraftModel(
        draft.id().value(),
        draft.userId().value(),
        draft.type().name(),
        Document.parse(draft.data().toString()));
  }
}
