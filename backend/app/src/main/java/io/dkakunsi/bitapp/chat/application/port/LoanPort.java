package io.dkakunsi.bitapp.chat.application.port;

import java.util.List;

import io.dkakunsi.bitapp.Id;
import io.dkakunsi.bitapp.chat.domain.entity.ExternalData;

public interface LoanPort {

  public List<ChatLoan> getUserLoans(Id userId);

  public static final class ChatLoan extends ExternalData {

    public ChatLoan(Id loanId, String loanName) {
      super(loanId, loanName);
    }
  }
}
