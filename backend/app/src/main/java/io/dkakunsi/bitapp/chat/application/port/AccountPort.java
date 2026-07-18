package io.dkakunsi.bitapp.chat.application.port;

import java.util.List;

import io.dkakunsi.bitapp.Id;
import io.dkakunsi.bitapp.chat.domain.entity.ExternalData;

public interface AccountPort {

  List<ChatAccount> getUserAccounts(Id userId);

  public static final class ChatAccount extends ExternalData {

    public ChatAccount(Id accountId, String accountName) {
      super(accountId, accountName);
    }
  }
}
