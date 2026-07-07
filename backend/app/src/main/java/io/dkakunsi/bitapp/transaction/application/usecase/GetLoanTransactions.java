package io.dkakunsi.bitapp.transaction.application.usecase;

import java.util.List;

import io.dkakunsi.bitapp.Id;
import io.dkakunsi.bitapp.Result;
import io.dkakunsi.bitapp.UseCase;
import io.dkakunsi.bitapp.transaction.application.dto.TransactionResult;
import io.dkakunsi.bitapp.transaction.domain.repository.TransactionRepository;

public final class GetLoanTransactions implements UseCase<String, List<TransactionResult>> {

  private final TransactionRepository transactionRepository;

  public GetLoanTransactions(TransactionRepository transactionRepository) {
    this.transactionRepository = transactionRepository;
  }

  @Override
  public Result<List<TransactionResult>> execute(String loanId) {
    var transactions = transactionRepository.findByLoanId(Id.of(loanId));
    var results = transactions.stream()
        .map(TransactionResult::from)
        .toList();
    return Result.success(results);
  }
}
