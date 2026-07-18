package io.dkakunsi.bitapp.chat.infrastructure.mongo.model;

import java.util.List;

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

@Entity("drafts")
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
  private List<ExternalDataModel> externalData;

  public Draft toDraft() {
    var externalDataModel = this.externalData
        .stream()
        .map(ExternalDataModel::toExternalData)
        .toList();

    return new Draft(
        Id.of(this.id),
        Id.of(this.userId),
        Chat.Type.valueOf(this.type),
        new JSONObject(this.data.toJson()),
        externalDataModel);
  }

  public static DraftModel from(Draft draft) {
    var externalDataModels = draft.externalData()
        .stream()
        .map(ExternalDataModel::from)
        .toList();

    return new DraftModel(
        draft.id().value(),
        draft.userId().value(),
        draft.type().name(),
        Document.parse(draft.data().toString()),
        externalDataModels);
  }
}
