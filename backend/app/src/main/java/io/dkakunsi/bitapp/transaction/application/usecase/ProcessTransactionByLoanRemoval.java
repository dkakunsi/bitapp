package io.dkakunsi.bitapp.transaction.application.usecase;

import io.dkakunsi.bitapp.Context;
import io.dkakunsi.bitapp.Id;
import io.dkakunsi.bitapp.Result;
import io.dkakunsi.bitapp.UseCase;
import io.dkakunsi.bitapp.transaction.domain.repository.TransactionRepository;

public class ProcessTransactionByLoanRemoval implements UseCase<String, Void> {

  private final TransactionRepository transactionRepository;

  public ProcessTransactionByLoanRemoval(TransactionRepository transactionRepository) {
    this.transactionRepository = transactionRepository;
  }

  @Override
  public Result<Void> execute(String loanId) {
    var context = Context.current();
    var executor = context.requester();
    transactionRepository.findByLoanId(Id.of(loanId))
        .forEach(t -> {
          var ut = t.removeLoanReference(executor);
          transactionRepository.update(ut);
        });
    return Result.success();
  }
}
