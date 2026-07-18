package io.dkakunsi.bitapp.chat.domain.entity.prompt;

import io.dkakunsi.bitapp.chat.domain.entity.Chat;
import io.dkakunsi.bitapp.chat.domain.entity.Draft;
import io.dkakunsi.bitapp.chat.domain.entity.PromptMessage;

public class AccountPromptMessage extends PromptMessage {

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

  public AccountPromptMessage(Chat chat, Draft draft) {
    super(chat, draft);
  }

  @Override
  protected String getDataPrompt() {
    return String.format(DATA_PROMPT, chat.message(), chat.language(), draft.data().toString());
  }

  @Override
  protected String getStructurePrompt() {
    return STRUCTURE_PROMPT;
  }

  public static final class AccountPromptMessageBuilder extends PromptMessage.PromptMessageBuilder {

    @Override
    public PromptMessage build(Chat chat, Draft draft) {
      return new AccountPromptMessage(chat, draft);
    }
  }
}
