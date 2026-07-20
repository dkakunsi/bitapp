package io.dkakunsi.bitapp.chat.infrastructure.ai.prompt;

import io.dkakunsi.bitapp.chat.domain.entity.Draft;
import io.dkakunsi.bitapp.langchain.PromptMessage;

public class AccountPromptMessage extends PromptMessage<Draft> {

  private static final String DATA_PROMPT = """
      Given this input %s in language %s and our draft data %s,
      """;

  private static final String STRUCTURE_PROMPT = """
      Generate a JSON object that fulfill this account data structure with null and empty values stripped off:
      {
        "name": "string",
        "type": "string", // ['BANK', 'CASH', 'EWALLET', 'OTHER']
        "themeColor": "string" // in HEX format default to WHITE
      }
      """;

  public AccountPromptMessage(Draft draft) {
    super(draft);
  }

  @Override
  protected String getDataPrompt() {
    return String.format(DATA_PROMPT, data.getLastChat().message(), data.getLastChat().language(),
        data.modelResult().toString());
  }

  @Override
  protected String getStructurePrompt() {
    return STRUCTURE_PROMPT;
  }

  public static final class AccountPromptMessageBuilder extends PromptMessage.PromptMessageBuilder<Draft> {

    @Override
    public PromptMessage<Draft> build(Draft draft) {
      return new AccountPromptMessage(draft);
    }
  }
}
