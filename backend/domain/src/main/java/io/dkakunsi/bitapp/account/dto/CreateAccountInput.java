package io.dkakunsi.bitapp.account.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.dkakunsi.bitapp.account.model.Account;
import io.dkakunsi.bitapp.common.Id;
import io.dkakunsi.bitapp.user.model.User;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public final record CreateAccountInput(
    @NotBlank String name,
    String themeColor,
    Account.Type type) {

  public Account toAccount(String requester) {
    final var userId = Id.of(requester);
    final var user = User.builder().id(userId).build();
    final var now = LocalDateTime.now();
    final var executor = requester;
    return Account.builder()
        .id(Id.generate())
        .name(name)
        .type(type)
        .themeColor(themeColor)
        .user(user)
        .balance(BigDecimal.ZERO)
        .createdAt(now)
        .updatedAt(now)
        .createdBy(executor)
        .updatedBy(executor)
        .build();
  }

}
