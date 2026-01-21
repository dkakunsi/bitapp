package io.dkakunsi.bitapp.transaction.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.apache.commons.lang3.StringUtils;

import io.dkakunsi.bitapp.common.EntityStatus;
import io.dkakunsi.bitapp.common.Id;
import io.dkakunsi.bitapp.transaction.dto.CreateTransactionInput;
import io.dkakunsi.bitapp.transaction.dto.TransactionResult;
import lombok.Builder;

@Builder
public final record Transaction(
    Id id,
    Id user,

    String title,
    String description,
    LocalDate date,
    LocalTime time,
    Id source,
    Id destination,
    Id loan,
    Long amount,
    String currency,
    Category category,
    Type type,

    EntityStatus status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    String createdBy,
    String updatedBy) {

  private static final String DEFAULT_CURRENCY = "IDR";

  public static enum Type {
    CREDIT,
    DEBIT,
    TRANSFER;

    public static boolean isValid(String type) {
      if (StringUtils.isBlank(type)) {
        return false;
      }

      try {
        valueOf(type);
        return true;
      } catch (IllegalArgumentException e) {
        return false;
      }
    }
  }

  public static enum Category {
    FOOD,
    TRANSPORT,
    SHOPPING,
    ENTERTAINMENT,
    HEALTH,
    EDUCATION,
    INCOME,
    LOAN,
    OTHER;

    public static boolean isValid(String category) {
      if (StringUtils.isBlank(category)) {
        return true;
      }

      try {
        valueOf(category);
        return true;
      } catch (IllegalArgumentException e) {
        return false;
      }
    }
  }

  public static Transaction from(CreateTransactionInput input, String requester) {
    final var userId = Id.of(requester);
    final var now = LocalDateTime.now();
    final var executor = requester;

    var transactionDate = input.date() != null ? input.date() : LocalDate.now();
    var transactionTime = input.time() != null ? input.time() : LocalTime.now();
    var currency = input.currency() != null ? input.currency() : DEFAULT_CURRENCY;
    var category = input.category() != null ? Category.valueOf(input.category()) : null;

    return Transaction.builder()
        .id(Id.generate())
        .user(userId)
        .title(input.title())
        .description(input.description())
        .date(transactionDate)
        .time(transactionTime)
        .source(input.source() != null ? Id.of(input.source()) : null)
        .destination(input.destination() != null ? Id.of(input.destination()) : null)
        .loan(input.loan() != null ? Id.of(input.loan()) : null)
        .amount(input.amount())
        .currency(currency)
        .category(category)
        .type(Type.valueOf(input.type()))
        .status(EntityStatus.ACTIVE)
        .createdAt(now)
        .updatedAt(now)
        .createdBy(executor)
        .updatedBy(executor)
        .build();
  }

  public TransactionResult toResult() {
    return TransactionResult.builder()
        .id(this.id().value())
        .user(this.user().value())
        .title(this.title())
        .description(this.description())
        .date(this.date().toString())
        .time(this.time().toString().substring(0, 5))
        .source(this.source() != null ? this.source().value() : null)
        .destination(this.destination() != null ? this.destination().value() : null)
        .loan(this.loan() != null ? this.loan().value() : null)
        .amount(this.amount())
        .currency(this.currency())
        .category(this.category() != null ? this.category().name() : null)
        .type(this.type().name())
        .build();
  }
}
