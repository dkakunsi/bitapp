package io.dkakunsi.bitapp.account.application.port;

import io.dkakunsi.bitapp.Id;

public interface AccountTransactionPort {

  void removeOrUpdateByAccountId(Id id);

}
