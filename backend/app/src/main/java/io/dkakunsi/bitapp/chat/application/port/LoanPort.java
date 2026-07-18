package io.dkakunsi.bitapp.chat.application.port;

import java.util.List;

import io.dkakunsi.bitapp.Id;

public interface LoanPort {

  public List<ChatLoan> getUserLoans(Id userId);

  public static final record ChatLoan(
      String loanId,
      String loanName) {

    @Override
    public final String toString() {
      return """
          {
            "loanId": "%s",
            "loanName": "%s"
          }
          """.formatted(loanId, loanName);
    }
  }

}
