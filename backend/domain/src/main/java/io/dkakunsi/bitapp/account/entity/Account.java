package io.dkakunsi.bitapp.account.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.apache.commons.lang3.StringUtils;

import io.dkakunsi.bitapp.account.dto.AccountResult;
import io.dkakunsi.bitapp.account.dto.CreateAccountInput;
import io.dkakunsi.bitapp.account.dto.UpdateAccountInput;
import io.dkakunsi.bitapp.domain.entity.EntityStatus;
import io.dkakunsi.bitapp.domain.entity.Id;
import lombok.Builder;

@Builder
public final record Account(
    Id id,
    Id user,

    String name,
    Type type,
    String themeColor,
    BigDecimal balance,

    EntityStatus status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    String createdBy,
    String updatedBy) {

  private static final String DEFAULT_THEME_COLOR = "#FFFFFF";

  public static enum Type {
    BANK,
    CASH,
    EWALLET,
    OTHER;

    public static boolean isValid(String type) {
      if (StringUtils.isBlank(type)) {
        return false;
      }

      try {
        valueOf(type);
        return true;
      } catch (IllegalArgumentException _) {
        return false;
      }
    }
  }

  public static Account from(CreateAccountInput input, String requester) {
    final var userId = Id.of(requester);
    final var now = LocalDateTime.now();
    final var executor = requester;
    return Account.builder()
        .id(Id.generate())
        .name(input.name())
        .type(Type.valueOf(input.type()))
        .themeColor(input.themeColor() != null ? input.themeColor() : DEFAULT_THEME_COLOR)
        .user(userId)
        .balance(BigDecimal.ZERO)
        .status(EntityStatus.ACTIVE)
        .createdAt(now)
        .updatedAt(now)
        .createdBy(executor)
        .updatedBy(executor)
        .build();
  }

  public Account updateDetails(UpdateAccountInput input, String requester) {
    var updatedName = input.name() != null ? input.name() : this.name;
    var updatedType = input.type() != null ? Type.valueOf(input.type()) : this.type;
    var updatedThemeColor = input.themeColor() != null ? input.themeColor() : this.themeColor;

    return Account.builder()
        .id(this.id)
        .name(updatedName)
        .type(updatedType)
        .themeColor(updatedThemeColor)
        .balance(this.balance)
        .user(this.user)
        .status(this.status)
        .createdAt(this.createdAt)
        .updatedAt(LocalDateTime.now())
        .createdBy(this.createdBy)
        .updatedBy(requester)
        .build();
  }

  public Account updateBalance(BigDecimal newBalance) {
    return Account.builder()
        .id(this.id)
        .name(this.name)
        .type(this.type)
        .themeColor(this.themeColor)
        .balance(newBalance)
        .user(this.user)
        .status(this.status)
        .createdAt(this.createdAt)
        .updatedAt(this.updatedAt)
        .createdBy(this.createdBy)
        .updatedBy(this.updatedBy)
        .build();
  }

  public boolean isOwner(String requester) {
    return this.user.equals(Id.of(requester));
  }

  public AccountResult toResult() {
    return AccountResult.builder()
        .id(this.id().value())
        .name(this.name())
        .type(this.type().name())
        .themeColor(this.themeColor())
        .balance(this.balance())
        .user(this.user().value())
        .build();
  }
}
