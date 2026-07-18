package io.dkakunsi.bitapp.chat.infrastructure.loan;

import java.util.List;

import io.dkakunsi.bitapp.Id;
import io.dkakunsi.bitapp.chat.application.port.LoanPort;
import io.dkakunsi.bitapp.loan.domain.repository.LoanRepository;

public class LoanAdapter implements LoanPort {

  private final LoanRepository loanRepository;

  public LoanAdapter(LoanRepository loanRepository) {
    this.loanRepository = loanRepository;
  }

  @Override
  public List<ChatLoan> getUserLoans(Id userId) {
    return loanRepository.findByUserId(userId).stream()
        .map(loan -> new ChatLoan(loan.id(), loan.title()))
        .toList();
  }

}
