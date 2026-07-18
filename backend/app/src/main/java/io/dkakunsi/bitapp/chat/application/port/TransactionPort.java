package io.dkakunsi.bitapp.chat.application.port;

import io.dkakunsi.bitapp.Result;
import io.dkakunsi.bitapp.chat.domain.entity.Draft;

public interface TransactionPort {

  Result<Void> createTransaction(Draft draft);

}
