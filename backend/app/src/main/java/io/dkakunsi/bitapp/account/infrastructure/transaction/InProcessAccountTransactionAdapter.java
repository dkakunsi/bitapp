package io.dkakunsi.bitapp.account.infrastructure.transaction;

import io.dkakunsi.bitapp.Id;
import io.dkakunsi.bitapp.account.domain.port.AccountTransactionPort;
import io.dkakunsi.bitapp.transaction.application.usecase.ProcessTransactionByAccountRemoval;

public class InProcessAccountTransactionAdapter implements AccountTransactionPort {

  private final ProcessTransactionByAccountRemoval processTransactionByAccountRemoval;

  public InProcessAccountTransactionAdapter(ProcessTransactionByAccountRemoval processTransactionByAccountRemoval) {
    this.processTransactionByAccountRemoval = processTransactionByAccountRemoval;
  }

  @Override
  public void removeOrUpdateByAccountId(Id id) {
    processTransactionByAccountRemoval.execute(id.value());
  }
}
