package io.dkakunsi.bitapp.chat.application.usecase;

import io.dkakunsi.bitapp.AppError.Code;
import io.dkakunsi.bitapp.Id;
import io.dkakunsi.bitapp.Result;
import io.dkakunsi.bitapp.UseCase;
import io.dkakunsi.bitapp.chat.application.port.AccountPort;
import io.dkakunsi.bitapp.chat.application.port.LoanPort;
import io.dkakunsi.bitapp.chat.application.port.TransactionPort;
import io.dkakunsi.bitapp.chat.domain.repository.DraftRepository;

public class ConfirmChatUseCase implements UseCase<String, Void> {

  private final DraftRepository draftRepository;

  private final AccountPort accountPort;

  private final LoanPort loanPort;

  private final TransactionPort transactionPort;

  public ConfirmChatUseCase(DraftRepository draftRepository,
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
    try {
      var draftOpt = draftRepository.findById(Id.of(input));
      if (draftOpt.isEmpty()) {
        return Result.failure(Code.NOT_FOUND, "Draft not found");
      }

      var draft = draftOpt.get();
      var requester = getRequester();
      if (!requester.equals(draft.userId().value())) {
        return Result.failure(Code.FORBIDDEN, "Requester does not match");
      }

      Result<Void> result = switch (draft.type()) {
        case ACCOUNT -> accountPort.createAccount(draft);
        case LOAN -> loanPort.createLoan(draft);
        case TRANSACTION -> transactionPort.createTransaction(draft);
        default -> Result.failure(Code.BAD_REQUEST, "Unsupported draft type");
      };

      return result;
    } catch (Exception e) {
      return Result.failure(e);
    }
  }
}
