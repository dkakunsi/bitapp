package io.dkakunsi.bitapp.account.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.dkakunsi.bitapp.account.model.Account;
import io.dkakunsi.bitapp.account.validation.ValidAccountType;
import io.dkakunsi.bitapp.common.Id;
import io.dkakunsi.bitapp.user.model.User;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public final record CreateAccountInput(
    @NotBlank String name,
    @NotBlank @ValidAccountType String type,
    String themeColor) {

  private static final String DEFAULT_THEME_COLOR = "#FFFFFF";

  public Account toAccount(String requester) {
    final var userId = Id.of(requester);
    final var user = User.builder().id(userId).build();
    final var now = LocalDateTime.now();
    final var executor = requester;
    return Account.builder()
        .id(Id.generate())
        .name(name)
        .type(Account.Type.from(type))
        .themeColor(themeColor != null ? themeColor : DEFAULT_THEME_COLOR)
        .user(user)
        .balance(BigDecimal.ZERO)
        .createdAt(now)
        .updatedAt(now)
        .createdBy(executor)
        .updatedBy(executor)
        .build();
  }

}
