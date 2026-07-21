package io.dkakunsi.bitapp.draft.infrastructure.mongo.model;

import java.util.List;

import org.bson.Document;
import org.json.JSONObject;

import dev.morphia.annotations.Entity;
import io.dkakunsi.bitapp.Id;
import io.dkakunsi.bitapp.draft.domain.entity.Chat;
import io.dkakunsi.bitapp.draft.domain.entity.Draft;
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
  private List<Chat> chats;
  private String modelError;
  private Document modelResult;
  private List<CrossDomainReferenceModel> crossDomainReferences;
  private Boolean success;
  private Boolean confirmed;

  public Draft toDraft() {
    var crossDomainReferences = this.crossDomainReferences
        .stream()
        .map(CrossDomainReferenceModel::toCrossDomainReference)
        .toList();

    return Draft.builder()
        .id(Id.of(this.id))
        .userId(Id.of(this.userId))
        .type(Chat.Type.valueOf(this.type))
        .chats(chats)
        .modelError(this.modelError)
        .modelResult(new JSONObject(this.modelResult.toJson()))
        .crossDomainReferences(crossDomainReferences)
        .success(this.success)
        .confirmed(this.confirmed)
        .build();
  }

  public static DraftModel from(Draft draft) {
    var crossDomainReferenceModels = draft.crossDomainReferences()
        .stream()
        .map(CrossDomainReferenceModel::from)
        .toList();

    return DraftModel.builder()
        .id(draft.id().value())
        .userId(draft.userId().value())
        .type(draft.type().name())
        .chats(draft.chats())
        .modelError(draft.modelError())
        .modelResult(Document.parse(draft.modelResult().toString()))
        .crossDomainReferences(crossDomainReferenceModels)
        .success(draft.success())
        .confirmed(draft.confirmed())
        .build();
  }
}
