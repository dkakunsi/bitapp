package io.dkakunsi.bitapp.transaction.domain.port;

import java.math.BigDecimal;

import io.dkakunsi.bitapp.Id;

public interface TransactionAccountPort {

  void debitBalance(Id source, BigDecimal amount);

  void creditBalance(Id destination, BigDecimal amount);

  boolean isExistingAccount(Id accountId);
  
}
