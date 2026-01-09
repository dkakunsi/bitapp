package io.dkakunsi.bitapp.mongo.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import io.dkakunsi.bitapp.account.model.Account;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity("accounts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountEntity {

  @Id
  private String id;
  private String name;
  private String type;
  private String themeColor;
  private Double balance;
  private String userId;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private String createdBy;
  private String updatedBy;

  /**
   * Converts this entity to a domain Account model.
   */
  public Account toAccount() {
    var accountId = io.dkakunsi.bitapp.common.Id.of(this.id);
    var userIdObj = io.dkakunsi.bitapp.common.Id.of(this.userId);
    var user = io.dkakunsi.bitapp.user.model.User.builder()
        .id(userIdObj)
        .build();

    return Account.builder()
        .id(accountId)
        .name(this.name)
        .type(Account.Type.valueOf(this.type))
        .themeColor(this.themeColor)
        .balance(BigDecimal.valueOf(this.balance))
        .user(user)
        .createdAt(this.createdAt)
        .updatedAt(this.updatedAt)
        .createdBy(this.createdBy)
        .updatedBy(this.updatedBy)
        .build();
  }

  /**
   * Creates an entity from a domain Account model.
   */
  public static AccountEntity fromAccount(Account account) {
    return AccountEntity.builder()
        .id(account.getId().value())
        .name(account.getName())
        .type(account.getType().name())
        .themeColor(account.getThemeColor())
        .balance(account.getBalance().doubleValue())
        .userId(account.getUser().getId().value())
        .createdAt(account.getCreatedAt())
        .updatedAt(account.getUpdatedAt())
        .createdBy(account.getCreatedBy())
        .updatedBy(account.getUpdatedBy())
        .build();
  }
}
