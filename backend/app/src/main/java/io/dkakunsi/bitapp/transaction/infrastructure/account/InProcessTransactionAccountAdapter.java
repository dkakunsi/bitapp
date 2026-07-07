package io.dkakunsi.bitapp.transaction.infrastructure.account;

import java.math.BigDecimal;

import io.dkakunsi.bitapp.Id;
import io.dkakunsi.bitapp.account.application.dto.UpdateBalanceInput;
import io.dkakunsi.bitapp.account.application.usecase.GetAccount;
import io.dkakunsi.bitapp.account.application.usecase.UpdateBalance;
import io.dkakunsi.bitapp.transaction.domain.port.TransactionAccountPort;

public class InProcessTransactionAccountAdapter implements TransactionAccountPort {

  private final UpdateBalance updateBalance;
  private final GetAccount getAccount;

  public InProcessTransactionAccountAdapter(UpdateBalance updateBalance, GetAccount getAccount) {
    this.updateBalance = updateBalance;
    this.getAccount = getAccount;
  }

  @Override
  public void debitBalance(Id source, BigDecimal amount) {
    var input = UpdateBalanceInput.builder()
        .accountId(source.value())
        .balance(amount)
        .isCredit(false)
        .build();
    updateBalance.execute(input);
  }

  @Override
  public void creditBalance(Id destination, BigDecimal amount) {
    var input = UpdateBalanceInput.builder()
        .accountId(destination.value())
        .balance(amount)
        .isCredit(true)
        .build();
    updateBalance.execute(input);
  }

  @Override
  public boolean isExistingAccount(Id accountId) {
    if (accountId == null) {
      return false;
    }

    var result = getAccount.execute(accountId.value());
    if (result.isFailed() || result.isEmpty()) {
      return false;
    }

    return true;
  }
}
