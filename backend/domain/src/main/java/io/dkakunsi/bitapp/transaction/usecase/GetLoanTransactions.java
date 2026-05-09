package io.dkakunsi.bitapp.transaction.usecase;

import java.util.List;

import io.dkakunsi.bitapp.domain.entity.Id;
import io.dkakunsi.bitapp.domain.usecase.Result;
import io.dkakunsi.bitapp.domain.usecase.UseCase;
import io.dkakunsi.bitapp.transaction.dto.TransactionResult;
import io.dkakunsi.bitapp.transaction.entity.Transaction;
import io.dkakunsi.bitapp.transaction.repository.TransactionRepository;

public final class GetLoanTransactions implements UseCase<String, List<TransactionResult>> {

  private final TransactionRepository transactionRepository;

  public GetLoanTransactions(TransactionRepository transactionRepository) {
    this.transactionRepository = transactionRepository;
  }

  @Override
  public Result<List<TransactionResult>> execute(String loanId) {
    var transactions = transactionRepository.findByLoanId(Id.of(loanId));
    var results = transactions.stream()
        .map(Transaction::toResult)
        .toList();
    return Result.success(results);
  }
}
