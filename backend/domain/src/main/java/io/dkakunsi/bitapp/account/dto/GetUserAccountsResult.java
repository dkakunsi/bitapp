package io.dkakunsi.bitapp.account.dto;

import java.math.BigDecimal;

import io.dkakunsi.bitapp.account.model.Account;
import lombok.Builder;

@Builder
public record GetUserAccountsResult(String id,
    String name,
    Account.Type type,
    String themeColor,
    BigDecimal balance,
    String userId) {

  public static GetUserAccountsResult from(Account account) {
    return GetUserAccountsResult.builder()
        .id(account.id().value())
        .name(account.name())
        .type(account.type())
        .themeColor(account.themeColor())
        .balance(account.balance())
        .userId(account.user().value())
        .build();
  }
}
