package io.dkakunsi.bitapp.chat.domain.entity.builder;

import io.dkakunsi.bitapp.chat.application.port.AccountPort;
import io.dkakunsi.bitapp.chat.domain.entity.Chat;
import io.dkakunsi.bitapp.chat.domain.entity.Draft;
import io.dkakunsi.bitapp.chat.domain.entity.PromptMessage;
import io.dkakunsi.bitapp.chat.domain.entity.PromptMessage.PromptMessageBuilder;

public final class LoanPromptMessageBuilder extends PromptMessageBuilder {

  private static final String LOAN_PROMPT = """
      You are a helpful assistant that helps users to extract loan data.

      Given this input %s in language %s, our draft data %s, and account data %s
      generate a JSON object that fulfill this loan data structure with null and empty values stripped off:
      {
        "type": "string", // required, ['BORROW', 'LEND']
        "date": "date", // required, use current date if not available
        "time": "time", // required, use current time if not available
        "partyName": "string", // optional
        "title": "string", // required
        "description": "string", // optional
        "amount": "integer", // required
        "currency": "string", // required, in ISO 4217 format
        "interestRate": "double", // required
        "account": "string" // optional
      }

      Your reply should be in this format:
      {
        "success": boolean, // true if there is no error in the extraction
        "message": string, // error message in the given language if success is false
        "data": json // the extracted data in JSON format, null if not success
      }
      """;

  private final AccountPort accountPort;

  public LoanPromptMessageBuilder(AccountPort accountPort) {
    this.accountPort = accountPort;
  }

  @Override
  public PromptMessage build(Chat chat, Draft draft) {
    var input = chat.message();
    var language = chat.language();
    var draftData = draft.data().toString();
    var userAccounts = accountPort.getUserAccounts(draft.userId()).toString();
    return new PromptMessage(String.format(LOAN_PROMPT, input, language, draftData, userAccounts));
  }

}
