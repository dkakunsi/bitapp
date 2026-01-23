package io.dkakunsi.bitapp.transaction.usecase;

import io.dkakunsi.bitapp.common.AppError.Code;
import io.dkakunsi.bitapp.common.Context;
import io.dkakunsi.bitapp.common.usecase.Result;
import io.dkakunsi.bitapp.common.usecase.UseCase;
import io.dkakunsi.bitapp.transaction.dto.TransactionResult;
import io.dkakunsi.bitapp.transaction.repository.TransactionRepository;

public final class GetTransaction implements UseCase<String, TransactionResult> {

  private final TransactionRepository transactionRepository;

  public GetTransaction(TransactionRepository transactionRepository) {
    this.transactionRepository = transactionRepository;
  }

  @Override
  public Result<TransactionResult> execute(Context context, String transactionId) {
    return transactionRepository.findById(transactionId)
        .filter(transaction -> transaction.user().value().equals(context.requester()))
        .map(transaction -> Result.success(transaction.toResult()))
        .orElse(Result.failure(Code.NOT_FOUND, "Transaction not found"));
  }
}
