package io.dkakunsi.bitapp.draft.application.port;

import io.dkakunsi.bitapp.Result;
import io.dkakunsi.bitapp.draft.domain.entity.Draft;

public interface TransactionPort {

  Result<Void> createTransaction(Draft draft);

}
