package io.dkakunsi.bitapp.transaction.application.usecase;

import io.dkakunsi.bitapp.AppError.Code;
import io.dkakunsi.bitapp.Id;
import io.dkakunsi.bitapp.Result;
import io.dkakunsi.bitapp.UseCase;
import io.dkakunsi.bitapp.transaction.application.dto.TransactionResult;
import io.dkakunsi.bitapp.transaction.domain.repository.TransactionRepository;

public final class GetTransaction implements UseCase<String, TransactionResult> {

  private final TransactionRepository transactionRepository;

  public GetTransaction(TransactionRepository transactionRepository) {
    this.transactionRepository = transactionRepository;
  }

  @Override
  public Result<TransactionResult> execute(String transactionId) {
    return transactionRepository.findById(Id.of(transactionId))
        .filter(transaction -> transaction.user().value().equals(getRequester()))
        .map(transaction -> Result.success(TransactionResult.from(transaction)))
        .orElse(Result.failure(Code.NOT_FOUND, "Transaction not found"));
  }
}
