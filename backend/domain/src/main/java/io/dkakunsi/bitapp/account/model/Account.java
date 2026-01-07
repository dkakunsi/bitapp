package io.dkakunsi.money.account.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.dkakunsi.lab.common.Id;
import io.dkakunsi.money.user.model.User;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public final class Account {

  private Id id;
  private String name;
  private Type type;
  private String themeColor;
  private BigDecimal balance;
  private User user;

  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private String createdBy;
  private String updatedBy;

  public static enum Type {
    BANK,
    CASH,
    EWALLET,
    OTHER
  }
}
