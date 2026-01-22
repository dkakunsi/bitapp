package io.dkakunsi.bitapp.account.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import dev.morphia.annotations.Entity;
import io.dkakunsi.bitapp.account.entity.Account;
import io.dkakunsi.bitapp.common.EntityStatus;
import io.dkakunsi.bitapp.common.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity("accounts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountModel {

  @dev.morphia.annotations.Id
  private String id;
  private String name;
  private String type;
  private String themeColor;
  private Double balance;
  private String userId;

  private String status;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private String createdBy;
  private String updatedBy;

  /**
   * Converts this entity to a domain Account model.
   */
  public Account toAccount() {
    var accountId = Id.of(this.id);
    var userIdObj = Id.of(this.userId);

    return Account.builder()
        .id(accountId)
        .name(this.name)
        .type(Account.Type.valueOf(this.type))
        .themeColor(this.themeColor)
        .balance(BigDecimal.valueOf(this.balance))
        .user(userIdObj)
        .status(EntityStatus.valueOf(this.status))
        .createdAt(this.createdAt)
        .updatedAt(this.updatedAt)
        .createdBy(this.createdBy)
        .updatedBy(this.updatedBy)
        .build();
  }

  /**
   * Creates an entity from a domain Account model.
   */
  public static AccountModel fromAccount(Account account) {
    // Morphia is not working properly with builder pattern, so we have to use the
    // constructor
    return new AccountModel(
        account.id().value(),
        account.name(),
        account.type().name(),
        account.themeColor(),
        account.balance().doubleValue(),
        account.user().value(),
        account.status().name(),
        account.createdAt(),
        account.updatedAt(),
        account.createdBy(),
        account.updatedBy());
  }
}
