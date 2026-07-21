package io.dkakunsi.bitapp.draft.infrastructure.loan;

import java.math.BigDecimal;
import java.util.List;

import io.dkakunsi.bitapp.DateTimeConverter;
import io.dkakunsi.bitapp.Id;
import io.dkakunsi.bitapp.Result;
import io.dkakunsi.bitapp.draft.application.port.AccountPort.ChatAccount;
import io.dkakunsi.bitapp.draft.application.port.LoanPort;
import io.dkakunsi.bitapp.draft.domain.entity.Draft;
import io.dkakunsi.bitapp.loan.application.dto.CreateLoanInput;
import io.dkakunsi.bitapp.loan.application.usecase.CreateLoan;
import io.dkakunsi.bitapp.loan.domain.repository.LoanRepository;

public class InProcessDraftLoanAdapter implements LoanPort, DateTimeConverter {

  private final LoanRepository loanRepository;

  private final CreateLoan createLoanUseCase;

  public InProcessDraftLoanAdapter(LoanRepository loanRepository, CreateLoan createLoanUseCase) {
    this.loanRepository = loanRepository;
    this.createLoanUseCase = createLoanUseCase;
  }

  @Override
  public List<ChatLoan> getUserLoans(Id userId) {
    return loanRepository.findByUserId(userId).stream()
        .map(loan -> new ChatLoan(loan.id(), loan.title()))
        .toList();
  }

  @Override
  public Result<Void> createLoan(Draft draft) {
    var input = toCreateLoanInput(draft);
    var result = createLoanUseCase.execute(input);
    return result.isSuccess() ? Result.success() : Result.failure(result);
  }

  private CreateLoanInput toCreateLoanInput(Draft draft) {
    var jsonData = draft.modelResult();
    var type = jsonData.optString("type");
    var datetime = jsonData.optString("datetime");
    var partyName = jsonData.optString("partyName");
    var title = jsonData.optString("title");
    var description = jsonData.optString("description");
    var amount = jsonData.optInt("amount");
    var currency = jsonData.optString("currency");
    var interestRate = jsonData.optDouble("interestRate");
    var account = jsonData.optString("account");

    var localDate = toEpochMilli(datetime);
    var localTime = toMinutesSinceMidnight(datetime);
    var accountData = draft.getCrossDomainReferenceByName(account, ChatAccount.class);
    var accountId = accountData != null ? accountData.getId().value() : null;

    return CreateLoanInput.builder()
        .type(type)
        .date(localDate)
        .time(localTime)
        .partyName(partyName)
        .title(title)
        .description(description)
        .amount(BigDecimal.valueOf(amount))
        .currency(currency)
        .interestRate(interestRate)
        .account(accountId)
        .build();
  }

}
