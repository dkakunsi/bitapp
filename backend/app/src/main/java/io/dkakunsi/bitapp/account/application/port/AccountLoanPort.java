package io.dkakunsi.bitapp.account.application.port;

import io.dkakunsi.bitapp.Id;

public interface AccountLoanPort {

  void removeByAccountId(Id accountId);
}
