package io.dkakunsi.bitapp.langchain;

import java.util.List;

import io.dkakunsi.bitapp.CrossDomainReference;

public abstract class PromptMessage<T> {

  protected static final String RESULT_PROMPT = """
      Your reply should be a valid JSON object in this format:
      {
        "data": json,
        "error": string // when data cannot be generated
      }

      All datetime values should be in UTC timezone and ISO 8601 format.
      """;

  protected final T data;

  protected PromptMessage(T data) {
    this.data = data;
  }

  public List<CrossDomainReference> getCrossDomainReferences() {
    return List.of();
  }

  public String getPrompt() {
    return String.format("%s. %s. %s", getDataPrompt(), getStructurePrompt(), RESULT_PROMPT);
  }

  protected abstract String getDataPrompt();

  protected abstract String getStructurePrompt();

  public static abstract class PromptMessageBuilder<T> {

    public abstract PromptMessage<T> build(T data);

  }
}
