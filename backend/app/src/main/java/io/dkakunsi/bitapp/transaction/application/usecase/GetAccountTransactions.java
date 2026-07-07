package io.dkakunsi.bitapp.transaction.application.usecase;

import java.util.List;

import io.dkakunsi.bitapp.Id;
import io.dkakunsi.bitapp.Result;
import io.dkakunsi.bitapp.UseCase;
import io.dkakunsi.bitapp.transaction.application.dto.TransactionResult;
import io.dkakunsi.bitapp.transaction.domain.repository.TransactionRepository;

public final class GetAccountTransactions implements UseCase<String, List<TransactionResult>> {

  private final TransactionRepository transactionRepository;

  public GetAccountTransactions(TransactionRepository transactionRepository) {
    this.transactionRepository = transactionRepository;
  }

  @Override
  public Result<List<TransactionResult>> execute(String accountId) {
    var transactions = transactionRepository.findByAccountId(Id.of(accountId));
    var results = transactions.stream()
        .map(TransactionResult::from)
        .toList();
    return Result.success(results);
  }
}
