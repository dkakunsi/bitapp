package io.dkakunsi.bitapp.account.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.dkakunsi.bitapp.account.dto.CreateAccountInput;
import io.dkakunsi.bitapp.common.Id;
import io.dkakunsi.bitapp.user.model.User;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@EqualsAndHashCode
@ToString
public final class Account {

  private final Id id;
  private final String name;
  private final Type type;
  private final String themeColor;
  private final BigDecimal balance;
  private final User user;

  private final LocalDateTime createdAt;
  private final LocalDateTime updatedAt;
  private final String createdBy;
  private final String updatedBy;

  public static enum Type {
    BANK,
    CASH,
    EWALLET,
    OTHER
  }

  public static Account from(CreateAccountInput input, String requester) {
    final var userId = Id.of(requester);
    final var user = User.builder().id(userId).build();
    final var now = LocalDateTime.now();
    final var executor = requester;
    return Account.builder()
        .id(Id.generate())
        .name(input.name())
        .type(input.type())
        .themeColor(input.themeColor())
        .user(user)
        .balance(BigDecimal.ZERO)
        .createdAt(now)
        .updatedAt(now)
        .createdBy(executor)
        .updatedBy(executor)
        .build();
  }
}
