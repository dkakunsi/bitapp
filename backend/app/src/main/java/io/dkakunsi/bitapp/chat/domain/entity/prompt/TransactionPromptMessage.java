package io.dkakunsi.bitapp.chat.domain.entity.prompt;

import java.util.List;
import java.util.stream.Stream;

import io.dkakunsi.bitapp.chat.application.port.AccountPort;
import io.dkakunsi.bitapp.chat.application.port.AccountPort.ChatAccount;
import io.dkakunsi.bitapp.chat.application.port.LoanPort;
import io.dkakunsi.bitapp.chat.application.port.LoanPort.ChatLoan;
import io.dkakunsi.bitapp.chat.domain.entity.Chat;
import io.dkakunsi.bitapp.chat.domain.entity.Draft;
import io.dkakunsi.bitapp.chat.domain.entity.ExternalData;
import io.dkakunsi.bitapp.chat.domain.entity.PromptMessage;

public final class TransactionPromptMessage extends PromptMessage {

  private static final String DATA_PROMPT = """
      Given this input %s in language %s, our current draft data %s, account data %s, and loan data %s.
      """;

  private static final String STRUCTURE_PROMPT = """
      Generate a JSON object that fulfill this transaction data structure with null and empty values stripped off:
      {
        "title": "string",
        "description": "string",
        "datetime": "datetime", // use current date if not available
        "source": "string", // required for type DEBIT & TRANSFER
        "destination": "string", // required for type CREDIT & TRANSFER
        "loan": "string",
        "amount": "integer",
        "currency": "string", // in ISO 4217 format
        "category": "string", // ['BONUS', 'BILLS', 'CHARITY', 'EDUCATION', 'ENTERTAINMENT', 'FOOD', 'GIFT', 'HEALTH', 'HOBBIES', 'INTEREST', 'INVESTMENT', 'LOAN', 'LOAN_DISBURSEMENT', 'LOAN_PAYMENT', 'OTHER', 'RENT', 'SALARY', 'SAVINGS', 'SHOPPING', 'SUBSCRIPTION', 'TAX', 'TRANSPORTATION', 'TRAVEL', 'UTILITIES']
        "type": "string" // ['CREDIT', 'DEBIT', 'TRANSFER']
      }
      """;

  private final List<ChatAccount> userAccounts;
  private final List<ChatLoan> userLoans;

  public TransactionPromptMessage(Chat chat, Draft draft, List<ChatAccount> userAccounts, List<ChatLoan> userLoans) {
    super(chat, draft);
    this.userAccounts = userAccounts;
    this.userLoans = userLoans;
  }

  @Override
  public List<ExternalData> getExternalData() {
    return Stream.concat(userAccounts.stream(), userLoans.stream()).toList();
  }

  @Override
  protected String getDataPrompt() {
    var accounts = userAccounts.stream().map(ChatAccount::getName).toList();
    var loans = userLoans.stream().map(ChatLoan::getName).toList();
    return String.format(DATA_PROMPT, chat.message(), chat.language(), draft.data().toString(),
        accounts.toString(), loans.toString());
  }

  @Override
  protected String getStructurePrompt() {
    return STRUCTURE_PROMPT;
  }

  public static final class TransactionPromptMessageBuilder extends PromptMessage.PromptMessageBuilder {

    private final AccountPort accountPort;
    private final LoanPort loanPort;

    public TransactionPromptMessageBuilder(AccountPort accountPort, LoanPort loanPort) {
      this.accountPort = accountPort;
      this.loanPort = loanPort;
    }

    @Override
    public PromptMessage build(Chat chat, Draft draft) {
      var userAccounts = accountPort.getUserAccounts(draft.userId());
      var userLoans = loanPort.getUserLoans(draft.userId());
      return new TransactionPromptMessage(chat, draft, userAccounts, userLoans);
    }
  }
}
