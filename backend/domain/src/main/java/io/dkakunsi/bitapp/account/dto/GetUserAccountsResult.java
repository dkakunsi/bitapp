package io.dkakunsi.bitapp.account.dto;

import java.math.BigDecimal;
import java.util.List;

import io.dkakunsi.bitapp.account.model.Account;
import lombok.Builder;

@Builder
public record GetUserAccountsResult(List<AccountItem> accounts) {

  @Builder
  public record AccountItem(
      String id,
      String name,
      Account.Type type,
      String themeColor,
      BigDecimal balance,
      String userId) {
  }

  public static GetUserAccountsResult from(List<Account> accounts) {
    var items = accounts.stream()
        .map(account -> AccountItem.builder()
            .id(account.getId().value())
            .name(account.getName())
            .type(account.getType())
            .themeColor(account.getThemeColor())
            .balance(account.getBalance())
            .userId(account.getUser().getId().value())
            .build())
        .toList();
    return new GetUserAccountsResult(items);
  }
}
