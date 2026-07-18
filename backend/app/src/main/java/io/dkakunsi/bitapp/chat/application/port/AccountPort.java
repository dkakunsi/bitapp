package io.dkakunsi.bitapp.chat.application.port;

import java.util.List;

import io.dkakunsi.bitapp.Id;

public interface AccountPort {

  List<ChatAccount> getUserAccounts(Id userId);

  public static final record ChatAccount(
      String accountId,
      String accountName) {

    @Override
    public final String toString() {
      return """
          {
            "accountId": "%s",
            "accountName": "%s"
          }
          """.formatted(accountId, accountName);
    }
  }
}
