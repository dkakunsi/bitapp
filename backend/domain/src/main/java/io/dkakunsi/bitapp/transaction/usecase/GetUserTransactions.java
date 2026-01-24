package io.dkakunsi.bitapp.transaction.usecase;

import java.util.List;

import io.dkakunsi.bitapp.common.Context;
import io.dkakunsi.bitapp.domain.usecase.Result;
import io.dkakunsi.bitapp.domain.usecase.UseCase;
import io.dkakunsi.bitapp.transaction.dto.TransactionResult;
import io.dkakunsi.bitapp.transaction.entity.Transaction;
import io.dkakunsi.bitapp.transaction.repository.TransactionRepository;

public final class GetUserTransactions implements UseCase<String, List<TransactionResult>> {

  private final TransactionRepository transactionRepository;

  public GetUserTransactions(TransactionRepository transactionRepository) {
    this.transactionRepository = transactionRepository;
  }

  @Override
  public Result<List<TransactionResult>> execute(Context context, String userId) {
    var transactions = transactionRepository.findByUserId(userId);
    var results = transactions.stream()
        .map(Transaction::toResult)
        .toList();
    return Result.success(results);
  }
}
