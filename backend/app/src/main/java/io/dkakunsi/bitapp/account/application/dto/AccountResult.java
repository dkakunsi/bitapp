package io.dkakunsi.bitapp.account.application.dto;

import java.math.BigDecimal;

import io.dkakunsi.bitapp.account.domain.entity.Account;
import lombok.Builder;

@Builder
public final record AccountResult(
    String id,
    String name,
    String type,
    String themeColor,
    BigDecimal balance,
    String user) {

    
  public static AccountResult from(Account account) {
    return AccountResult.builder()
        .id(account.id().value())
        .name(account.name())
        .type(account.type().name())
        .themeColor(account.themeColor())
        .balance(account.balance())
        .user(account.user().value())
        .build();
  }

}
