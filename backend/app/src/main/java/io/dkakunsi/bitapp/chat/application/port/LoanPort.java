package io.dkakunsi.bitapp.chat.application.port;

import java.util.List;

import io.dkakunsi.bitapp.Id;
import io.dkakunsi.bitapp.Result;
import io.dkakunsi.bitapp.chat.domain.entity.Draft;
import io.dkakunsi.bitapp.chat.domain.entity.ExternalData;

public interface LoanPort {

  List<ChatLoan> getUserLoans(Id userId);

  Result<Void> createLoan(Draft draft);

  public static final class ChatLoan extends ExternalData {

    public ChatLoan(Id loanId, String loanName) {
      super(loanId, loanName);
    }
  }
}
