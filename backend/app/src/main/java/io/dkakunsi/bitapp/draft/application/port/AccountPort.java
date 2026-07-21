package io.dkakunsi.bitapp.draft.application.port;

import java.util.List;

import io.dkakunsi.bitapp.CrossDomainReference;
import io.dkakunsi.bitapp.Id;
import io.dkakunsi.bitapp.Result;
import io.dkakunsi.bitapp.draft.domain.entity.Draft;

public interface AccountPort {

  List<ChatAccount> getUserAccounts(Id userId);

  Result<Void> createAccount(Draft draft);

  public static final class ChatAccount extends CrossDomainReference {

    public ChatAccount(Id accountId, String accountName) {
      super(accountId, accountName);
    }
  }
}
