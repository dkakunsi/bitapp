package io.dkakunsi.bitapp.draft.domain.entity;

import lombok.Builder;

@Builder
public record Chat(
    Type type,
    String draftId,
    String message,
    String language) {

  public static enum Type {
    ACCOUNT,
    LOAN,
    TRANSACTION,
  }
}
