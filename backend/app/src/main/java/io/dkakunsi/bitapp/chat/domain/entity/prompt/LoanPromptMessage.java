package io.dkakunsi.bitapp.chat.domain.entity.prompt;

import java.util.List;

import io.dkakunsi.bitapp.chat.application.port.AccountPort;
import io.dkakunsi.bitapp.chat.application.port.AccountPort.ChatAccount;
import io.dkakunsi.bitapp.chat.domain.entity.Chat;
import io.dkakunsi.bitapp.chat.domain.entity.Draft;
import io.dkakunsi.bitapp.chat.domain.entity.ExternalData;
import io.dkakunsi.bitapp.chat.domain.entity.PromptMessage;

public class LoanPromptMessage extends PromptMessage {

  private static final String DATA_PROMPT = """
      Given this input %s in language %s, our draft data %s, and account data %s
      """;

  private static final String STRUCTURE_PROMPT = """
      Generate a JSON object that fulfill this loan data structure with null and empty values stripped off:
      {
        "type": "string", // ['BORROW', 'LEND']
        "datetime": "datetime", // use current date if not available
        "partyName": "string",
        "title": "string",
        "description": "string",
        "amount": "integer",
        "currency": "string", // in ISO 4217 format
        "interestRate": "double",
        "account": "string"
      }
      """;

  private final List<ChatAccount> userAccounts;

  public LoanPromptMessage(Chat chat, Draft draft, List<ChatAccount> userAccounts) {
    super(chat, draft);
    this.userAccounts = userAccounts;
  }

  @Override
  public List<ExternalData> getExternalData() {
    return List.copyOf(userAccounts);
  }

  @Override
  protected String getDataPrompt() {
    var accounts = userAccounts.stream().map(ChatAccount::getName).toList();
    return String.format(DATA_PROMPT, chat.message(), chat.language(), draft.data().toString(),
        accounts.toString());
  }

  @Override
  protected String getStructurePrompt() {
    return STRUCTURE_PROMPT;
  }

  public static final class LoanPromptMessageBuilder extends PromptMessageBuilder {

    private final AccountPort accountPort;

    public LoanPromptMessageBuilder(AccountPort accountPort) {
      this.accountPort = accountPort;
    }

    @Override
    public PromptMessage build(Chat chat, Draft draft) {
      var userAccounts = accountPort.getUserAccounts(draft.userId());
      return new LoanPromptMessage(chat, draft, userAccounts);
    }
  }
}
