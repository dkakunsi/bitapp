package io.dkakunsi.bitapp.transaction.application.usecase;

import java.util.ArrayList;

import io.dkakunsi.bitapp.Id;
import io.dkakunsi.bitapp.Result;
import io.dkakunsi.bitapp.UseCase;
import io.dkakunsi.bitapp.transaction.domain.entity.Transaction;
import io.dkakunsi.bitapp.transaction.domain.repository.TransactionRepository;

public class ProcessTransactionByAccountRemoval implements UseCase<String, Void> {

  private final TransactionRepository transactionRepository;

  public ProcessTransactionByAccountRemoval(TransactionRepository transactionRepository) {
    this.transactionRepository = transactionRepository;
  }

  @Override
  public Result<Void> execute(String accountId) {
    var requester = getRequester();
    var id = Id.of(accountId);
    var deleteTransactions = new ArrayList<String>();
    var updateTransactions = new ArrayList<Transaction>();

    transactionRepository.findByAccountId(id).forEach(t -> {
      switch (t.type()) {
        case DEBIT, CREDIT: {
          deleteTransactions.add(t.id().value());
          break;
        }
        case TRANSFER: {
          var updatedTransaction = t.convertFromTransfer(id, requester);
          updateTransactions.add(updatedTransaction);
          break;
        }
      }
    });

    transactionRepository.delete(deleteTransactions);
    transactionRepository.update(updateTransactions);
    return Result.success();
  }
}
