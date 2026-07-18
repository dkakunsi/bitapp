package io.dkakunsi.bitapp.chat.domain.entity;

import java.util.List;

public abstract class PromptMessage {

  protected static final String RESULT_PROMPT = """
      Your reply should be in this format:
      {
        "data": json,
        "error": string // when data cannot be generated
      }

      All datetime values should be in UTC timezone and ISO 8601 format.
      """;

  protected final Chat chat;

  protected final Draft draft;

  protected PromptMessage(Chat chat, Draft draft) {
    this.chat = chat;
    this.draft = draft;
  }

  public List<ExternalData> getExternalData() {
    return List.of();
  }

  public String getPrompt() {
    return String.format("%s. %s. %s", getDataPrompt(), getStructurePrompt(), RESULT_PROMPT);
  }

  protected abstract String getDataPrompt();

  protected abstract String getStructurePrompt();

  public static abstract class PromptMessageBuilder {

    public abstract PromptMessage build(Chat chat, Draft draft);

  }
}
