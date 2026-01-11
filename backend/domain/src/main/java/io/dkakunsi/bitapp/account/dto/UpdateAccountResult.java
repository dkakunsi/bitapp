package io.dkakunsi.bitapp.account.dto;

import io.dkakunsi.bitapp.account.model.Account;
import lombok.Builder;

@Builder
public final record UpdateAccountResult(
    String id,
    String name,
    String type,
    String themeColor) {

  public static UpdateAccountResult from(Account account) {
    return UpdateAccountResult.builder()
        .id(account.getId().value())
        .name(account.getName())
        .type(account.getType().name())
        .themeColor(account.getThemeColor())
        .build();
  }
}
