package io.dkakunsi.bitapp.chat.application.usecase;

import io.dkakunsi.bitapp.Id;
import io.dkakunsi.bitapp.Result;
import io.dkakunsi.bitapp.UseCase;
import io.dkakunsi.bitapp.chat.application.port.AccountPort;
import io.dkakunsi.bitapp.chat.application.port.LoanPort;
import io.dkakunsi.bitapp.chat.application.port.TransactionPort;
import io.dkakunsi.bitapp.chat.domain.repository.DraftRepository;

public class ConfirmDraft implements UseCase<String, Void> {

  private final DraftRepository draftRepository;

  private final AccountPort accountPort;

  private final LoanPort loanPort;

  private final TransactionPort transactionPort;

  public ConfirmDraft(DraftRepository draftRepository,
      AccountPort accountPort,
      LoanPort loanPort,
      TransactionPort transactionPort) {
    this.draftRepository = draftRepository;
    this.accountPort = accountPort;
    this.loanPort = loanPort;
    this.transactionPort = transactionPort;
  }

  @Override
  public Result<Void> execute(String input) {
    var draftOpt = draftRepository.findByIdAndNotConfirmed(Id.of(input));
    if (draftOpt.isEmpty()) {
      return Result.notFound("Draft not found");
    }

    var draft = draftOpt.get();
    var requester = getRequester();
    if (!requester.equals(draft.userId().value())) {
      return Result.forbidden("Requester does not match");
    }

    Result<Void> result = switch (draft.type()) {
      case ACCOUNT -> accountPort.createAccount(draft);
      case LOAN -> loanPort.createLoan(draft);
      case TRANSACTION -> transactionPort.createTransaction(draft);
      default -> Result.badRequest("Unsupported draft type");
    };

    draft = draft.confirm(result.isSuccess());
    draftRepository.save(draft);

    return result;
  }
}
