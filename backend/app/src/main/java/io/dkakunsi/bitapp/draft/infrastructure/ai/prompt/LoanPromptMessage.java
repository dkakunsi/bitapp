package io.dkakunsi.bitapp.draft.infrastructure.ai.prompt;

import java.util.List;

import io.dkakunsi.bitapp.CrossDomainReference;
import io.dkakunsi.bitapp.draft.application.port.AccountPort;
import io.dkakunsi.bitapp.draft.application.port.AccountPort.ChatAccount;
import io.dkakunsi.bitapp.draft.domain.entity.Draft;
import io.dkakunsi.bitapp.langchain.PromptMessage;

public class LoanPromptMessage extends PromptMessage<Draft> {

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

  public LoanPromptMessage(Draft data, List<ChatAccount> userAccounts) {
    super(data);
    this.userAccounts = userAccounts;
  }

  @Override
  public List<CrossDomainReference> getCrossDomainReferences() {
    return List.copyOf(userAccounts);
  }

  @Override
  protected String getDataPrompt() {
    var accounts = userAccounts.stream().map(ChatAccount::getName).toList();
    return String.format(DATA_PROMPT, data.getLastChat().message(), data.getLastChat().language(),
        data.modelResult().toString(),
        accounts.toString());
  }

  @Override
  protected String getStructurePrompt() {
    return STRUCTURE_PROMPT;
  }

  public static final class LoanPromptMessageBuilder extends PromptMessageBuilder<Draft> {

    private final AccountPort accountPort;

    public LoanPromptMessageBuilder(AccountPort accountPort) {
      this.accountPort = accountPort;
    }

    @Override
    public PromptMessage<Draft> build(Draft draft) {
      var userAccounts = accountPort.getUserAccounts(draft.userId());
      return new LoanPromptMessage(draft, userAccounts);
    }
  }
}
