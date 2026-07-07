package io.dkakunsi.bitapp.account.domain.port;

import io.dkakunsi.bitapp.Id;

public interface AccountLoanPort {

  void removeByAccountId(Id accountId);
}
