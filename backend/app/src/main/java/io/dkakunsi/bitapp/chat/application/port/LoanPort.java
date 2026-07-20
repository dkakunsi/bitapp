package io.dkakunsi.bitapp.chat.application.port;

import java.util.List;

import io.dkakunsi.bitapp.CrossDomainReference;
import io.dkakunsi.bitapp.Id;
import io.dkakunsi.bitapp.Result;
import io.dkakunsi.bitapp.chat.domain.entity.Draft;

public interface LoanPort {

  List<ChatLoan> getUserLoans(Id userId);

  Result<Void> createLoan(Draft draft);

  public static final class ChatLoan extends CrossDomainReference {

    public ChatLoan(Id loanId, String loanName) {
      super(loanId, loanName);
    }
  }
}
