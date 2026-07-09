package io.dkakunsi.bitapp.loan.infrastructure.account;

import java.util.Optional;

import io.dkakunsi.bitapp.Id;
import io.dkakunsi.bitapp.account.domain.repository.AccountRepository;
import io.dkakunsi.bitapp.loan.application.port.LoanAccountPort;

public class InProcessLoanAccountAdapter implements LoanAccountPort {

  private final AccountRepository accountRepository;

  public InProcessLoanAccountAdapter(AccountRepository accountRepository) {
    this.accountRepository = accountRepository;
  }

  @Override
  public Optional<LoanAccount> findById(Id accountId) {
    return accountRepository.findById(accountId)
        .map(account -> LoanAccount.builder()
            .userId(account.user())
            .build());
  }
}
