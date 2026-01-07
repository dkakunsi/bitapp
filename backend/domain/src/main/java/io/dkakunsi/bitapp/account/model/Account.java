package io.dkakunsi.bitapp.account.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
}
