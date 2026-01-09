package io.dkakunsi.bitapp.account.dto;

import java.math.BigDecimal;

import io.dkakunsi.bitapp.account.model.Account;
import lombok.Builder;

@Builder
public final record CreateAccountResult(
    String id,
    String name,
    Account.Type type,
    String themeColor,
    BigDecimal balance,
    String user) {

  public static CreateAccountResult from(Account account) {
    return CreateAccountResult.builder()
        .id(account.getId().value())
        .name(account.getName())
        .type(account.getType())
        .themeColor(account.getThemeColor())
        .balance(account.getBalance())
        .user(account.getUser().getId().value())
        .build();
  }
}
