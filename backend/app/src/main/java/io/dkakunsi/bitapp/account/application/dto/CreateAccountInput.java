package io.dkakunsi.bitapp.account.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;

import io.dkakunsi.bitapp.Id;
import io.dkakunsi.bitapp.Validatable;
import io.dkakunsi.bitapp.account.domain.entity.Account;
import lombok.Builder;

@Builder
public final record CreateAccountInput(
    String name,
    String type,
    String themeColor) implements Validatable {

  private static final String DEFAULT_THEME_COLOR = "#FFFFFF";

  @Override
  public void validate() {
    var errors = new ArrayList<String>();
    if (StringUtils.isBlank(name)) {
      errors.add("name: invalid value: " + name);
    }
    if (!Account.Type.isValid(type)) {
      errors.add("type: invalid value: " + type);
    }

    if (!errors.isEmpty()) {
      throw new IllegalArgumentException(String.join(", ", errors));
    }
  }

  public Account toAccount(String requester) {
    final var userId = Id.of(requester);
    final var now = LocalDateTime.now();
    final var executor = requester;
    return Account.builder()
        .id(Id.generate())
        .name(this.name())
        .type(Account.Type.valueOf(this.type()))
        .themeColor(this.themeColor() != null ? this.themeColor() : DEFAULT_THEME_COLOR)
        .user(userId)
        .balance(BigDecimal.ZERO)
        .active(true)
        .createdAt(now)
        .updatedAt(now)
        .createdBy(executor)
        .updatedBy(executor)
        .build();
  }
}
