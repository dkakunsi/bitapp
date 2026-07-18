package io.dkakunsi.bitapp.chat.domain.entity.builder;

import io.dkakunsi.bitapp.chat.domain.entity.Chat;
import io.dkakunsi.bitapp.chat.domain.entity.Draft;
import io.dkakunsi.bitapp.chat.domain.entity.PromptMessage;

public final class AccountPromptMessageBuilder extends PromptMessage.PromptMessageBuilder {

  private static final String ACCOUNT_PROMPT = """
      You are a helpful assistant that helps users to extract account data.

      Given this input %s in language %s and our draft data %s,
      generate a JSON object that fulfill this account data structure with null and empty values stripped off:
      {
        "name": "string", // required
        "type": "string", // required, ['BANK', 'CASH', 'EWALLET', 'OTHER']
        "themeColor": "string" // optional, in HEX format default to WHITE
      }

      Your reply should be in this format:
      {
        "success": boolean, // true if there is no error in the extraction
        "message": string, // error message in the given language if success is false
        "data": json // the extracted data in JSON format, null if not success
      }
      """;

  @Override
  public PromptMessage build(Chat chat, Draft draft) {
    var input = chat.message();
    var language = chat.language();
    var draftData = draft.data().toString();
    return new PromptMessage(String.format(ACCOUNT_PROMPT, input, language, draftData));
  }

}
