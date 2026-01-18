package io.dkakunsi.bitapp.account.dto;

import java.math.BigDecimal;

import io.dkakunsi.bitapp.account.model.Account;
import lombok.Builder;

@Builder
public final record UpdateAccountResult(
    String id,
    String name,
    String type,
    String themeColor,
    BigDecimal balance,
    String user) {

  public static UpdateAccountResult from(Account account) {
    return UpdateAccountResult.builder()
        .id(account.id().value())
        .name(account.name())
        .type(account.type().name())
        .themeColor(account.themeColor())
        .balance(account.balance())
        .user(account.user().value())
        .build();
  }
}
