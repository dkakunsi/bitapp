package io.dkakunsi.bitapp.draft.infrastructure.transaction;

import java.math.BigDecimal;

import io.dkakunsi.bitapp.DateTimeConverter;
import io.dkakunsi.bitapp.Result;
import io.dkakunsi.bitapp.draft.application.port.AccountPort.ChatAccount;
import io.dkakunsi.bitapp.draft.application.port.LoanPort.ChatLoan;
import io.dkakunsi.bitapp.draft.application.port.TransactionPort;
import io.dkakunsi.bitapp.draft.domain.entity.Draft;
import io.dkakunsi.bitapp.transaction.application.dto.CreateUserTransactionInput;
import io.dkakunsi.bitapp.transaction.application.usecase.CreateTransaction;

public class InProcessDraftTransactionAdapter implements TransactionPort, DateTimeConverter {

  private final CreateTransaction createTransactionUseCase;

  public InProcessDraftTransactionAdapter(CreateTransaction createTransactionUseCase) {
    this.createTransactionUseCase = createTransactionUseCase;
  }

  @Override
  public Result<Void> createTransaction(Draft draft) {
    var input = toCreateUserTransactionInput(draft);
    var result = createTransactionUseCase.execute(input);
    return result.isSuccess() ? Result.success() : Result.failure(result);
  }

  private CreateUserTransactionInput toCreateUserTransactionInput(Draft draft) {
    var jsonData = draft.modelResult();
    var title = jsonData.optString("title");
    var description = jsonData.optString("description");
    var datetime = jsonData.optString("datetime");
    var source = jsonData.optString("source");
    var destination = jsonData.optString("destination");
    var loan = jsonData.optString("loan");
    var amount = jsonData.optInt("amount");
    var currency = jsonData.optString("currency");
    var category = jsonData.optString("category");
    var type = jsonData.optString("type");

    var localDate = toEpochMilli(datetime);
    var localTime = toMinutesSinceMidnight(datetime);

    var sourceData = draft.getCrossDomainReferenceByName(source, ChatAccount.class);
    var sourceId = sourceData != null ? sourceData.getId().value() : null;

    var destinationData = draft.getCrossDomainReferenceByName(destination, ChatAccount.class);
    var destinationId = destinationData != null ? destinationData.getId().value() : null;

    var loanData = draft.getCrossDomainReferenceByName(loan, ChatLoan.class);
    var loanId = loanData != null ? loanData.getId().value() : null;

    return CreateUserTransactionInput.builder()
        .title(title)
        .description(description)
        .date(localDate)
        .time(localTime)
        .source(sourceId)
        .destination(destinationId)
        .loan(loanId)
        .amount(BigDecimal.valueOf(amount))
        .currency(currency)
        .category(category)
        .type(type)
        .build();
  }
}
