package io.dkakunsi.bitapp.account.domain.port;

import io.dkakunsi.bitapp.Id;

public interface AccountTransactionPort {

  void removeOrUpdateByAccountId(Id id);
  
}
