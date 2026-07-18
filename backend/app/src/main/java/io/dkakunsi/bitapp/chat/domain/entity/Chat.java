package io.dkakunsi.bitapp.chat.domain.entity;

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
