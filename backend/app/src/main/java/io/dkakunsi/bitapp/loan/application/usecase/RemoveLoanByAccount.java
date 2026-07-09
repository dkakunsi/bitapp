package io.dkakunsi.bitapp.loan.application.usecase;

import io.dkakunsi.bitapp.Id;
import io.dkakunsi.bitapp.Result;
import io.dkakunsi.bitapp.UseCase;
import io.dkakunsi.bitapp.loan.application.port.LoanTransactionPort;
import io.dkakunsi.bitapp.loan.domain.repository.LoanRepository;

public class RemoveLoanByAccount implements UseCase<String, Void> {

  private final LoanRepository loanRepository;
  private final LoanTransactionPort loanTransactionAdapter;

  public RemoveLoanByAccount(LoanRepository loanRepository, LoanTransactionPort loanTransactionAdapter) {
    this.loanRepository = loanRepository;
    this.loanTransactionAdapter = loanTransactionAdapter;
  }

  @Override
  public Result<Void> execute(String accountId) {
    loanRepository.findByAccountId(Id.of(accountId))
        .forEach(loan -> {
          loanTransactionAdapter.updateTransactionByLoanRemoval(loan.id());
          loanRepository.deleteById(loan.id());
        });

    return Result.success();
  }
}
