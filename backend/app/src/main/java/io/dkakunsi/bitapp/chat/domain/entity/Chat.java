package io.dkakunsi.bitapp.chat.domain.entity;

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
