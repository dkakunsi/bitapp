package io.dkakunsi.bitapp.chat.domain.entity;

public record PromptMessage(String message) {

  public static abstract class PromptMessageBuilder {

    public abstract PromptMessage build(Chat chat, Draft draft);

  }
}
