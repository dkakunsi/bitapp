package io.dkakunsi.bitapp.chat.domain.entity.builder;

import io.dkakunsi.bitapp.chat.application.port.AccountPort;
import io.dkakunsi.bitapp.chat.application.port.LoanPort;
import io.dkakunsi.bitapp.chat.domain.entity.Chat;
import io.dkakunsi.bitapp.chat.domain.entity.Draft;
import io.dkakunsi.bitapp.chat.domain.entity.PromptMessage;

public final class TransactionPromptMessageBuilder extends PromptMessage.PromptMessageBuilder {

  private final AccountPort accountPort;
  private final LoanPort loanPort;

  public TransactionPromptMessageBuilder(AccountPort accountPort, LoanPort loanPort) {
    this.accountPort = accountPort;
    this.loanPort = loanPort;
  }

  private static final String TRANSACTION_PROMPT = """
      You are a helpful assistant that helps users to extract transaction data.

      Given this input %s in language %s, our current draft data %s, account data %s, and loan data %s,
      generate a JSON object that fulfill this transaction data structure with null and empty values stripped off:
      {
        "title": "string", // required
        "description": "string", // optional
        "date": "date", // required, use current date if not available
        "time": "time", // required, use current time if not available
        "sourceId": "string", // The source account id of the transaction, required for type DEBIT & TRANSFER
        "sourceName": "string", // The source account name of the transaction, required for type DEBIT & TRANSFER
        "destinationId": "string", // The destination account id of the transaction, required for type CREDIT & TRANSFER
        "destinationName": "string", // The destination account name of the transaction, required for type CREDIT & TRANSFER
        "loanId": "string", // The loan id associated with the transaction
        "loanName": "string", // The loan name associated with the transaction
        "amount": "integer", // required
        "currency": "string", // required, in ISO 4217 format
        "category": "string", // required, ['BONUS', 'BILLS', 'CHARITY', 'EDUCATION', 'ENTERTAINMENT', 'FOOD', 'GIFT', 'HEALTH', 'HOBBIES', 'INTEREST', 'INVESTMENT', 'LOAN', 'LOAN_DISBURSEMENT', 'LOAN_PAYMENT', 'OTHER', 'RENT', 'SALARY', 'SAVINGS', 'SHOPPING', 'SUBSCRIPTION', 'TAX', 'TRANSPORTATION', 'TRAVEL', 'UTILITIES']
        "type": "string" // required, ['CREDIT', 'DEBIT', 'TRANSFER']
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
    var userAccounts = accountPort.getUserAccounts(draft.userId()).toString();
    var userLoans = loanPort.getUserLoans(draft.userId()).toString();
    return new PromptMessage(String.format(TRANSACTION_PROMPT, input, language, draftData, userAccounts, userLoans));
  }

}
