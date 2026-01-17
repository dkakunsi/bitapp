package io.dkakunsi.bitapp.account.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.dkakunsi.bitapp.account.dto.UpdateAccountInput;
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
    OTHER;

    public static Type from(String type) {
      if (type == null) {
        return null;
      }
      try {
        return valueOf(type);
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException("Invalid account type: " + type);
      }
    }
  }

  public Account updateDetails(UpdateAccountInput input, String requester) {
    var updatedName = input.name() != null ? input.name() : this.name;
    var updatedType = input.type() != null ? Type.from(input.type()) : this.type;
    var updatedThemeColor = input.themeColor() != null ? input.themeColor() : this.themeColor;

    return Account.builder()
        .id(this.id)
        .name(updatedName)
        .type(updatedType)
        .themeColor(updatedThemeColor)
        .balance(this.balance)
        .user(this.user)
        .createdAt(this.createdAt)
        .updatedAt(LocalDateTime.now())
        .createdBy(this.createdBy)
        .updatedBy(requester)
        .build();
  }

  public boolean isOwner(String requester) {
    return this.user.getId().equals(Id.of(requester));
  }
}
