package io.dkakunsi.bitapp.account.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.apache.commons.lang3.StringUtils;

import io.dkakunsi.bitapp.EntityStatus;
import io.dkakunsi.bitapp.Id;
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

  public Account updateDetails(String nameInput, Type typeInput, String colorInput, String requester) {
    var name = nameInput != null ? nameInput : this.name;
    var type = typeInput != null ? typeInput : this.type;
    var themeColor = colorInput != null ? colorInput : this.themeColor;

    return Account.builder()
        .id(this.id)
        .name(name)
        .type(type)
        .themeColor(themeColor)
        .balance(this.balance)
        .user(this.user)
        .status(this.status)
        .createdAt(this.createdAt)
        .updatedAt(LocalDateTime.now())
        .createdBy(this.createdBy)
        .updatedBy(requester)
        .build();
  }

  public boolean isOwner(String requester) {
    return this.user.equals(Id.of(requester));
  }
}
