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
  private String error;
  private Document data;
  private List<ExternalDataModel> externalData;
  private Boolean success;
  private Boolean confirmed;

  public Draft toDraft() {
    var externalDataModel = this.externalData
        .stream()
        .map(ExternalDataModel::toExternalData)
        .toList();

    return Draft.builder()
        .id(Id.of(this.id))
        .userId(Id.of(this.userId))
        .type(Chat.Type.valueOf(this.type))
        .error(this.error)
        .data(new JSONObject(this.data.toJson()))
        .externalData(externalDataModel)
        .success(this.success)
        .confirmed(this.confirmed)
        .build();
  }

  public static DraftModel from(Draft draft) {
    var externalDataModels = draft.externalData()
        .stream()
        .map(ExternalDataModel::from)
        .toList();

    return DraftModel.builder()
        .id(draft.id().value())
        .userId(draft.userId().value())
        .type(draft.type().name())
        .error(draft.error())
        .data(Document.parse(draft.data().toString()))
        .externalData(externalDataModels)
        .success(draft.success())
        .confirmed(draft.confirmed())
        .build();
  }
}
