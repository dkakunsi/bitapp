package io.dkakunsi.bitapp.transaction.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Currency;

import org.apache.commons.lang3.StringUtils;

import io.dkakunsi.bitapp.common.DateTimeConverter;
import io.dkakunsi.bitapp.domain.entity.EntityStatus;
import io.dkakunsi.bitapp.domain.entity.Id;
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
    BigDecimal amount,
    Currency currency,
    Category category,
    Type type,

    EntityStatus status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    String createdBy,
    String updatedBy) implements DateTimeConverter {

  public static final Currency DEFAULT_CURRENCY = Currency.getInstance("IDR");

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
      } catch (IllegalArgumentException _) {
        return false;
      }
    }
  }

  public static enum Category {
    BONUS,
    BILLS,
    CHARITY,
    EDUCATION,
    ENTERTAINMENT,
    FOOD,
    GIFT,
    HEALTH,
    HOBBIES,
    INTEREST,
    INVESTMENT,
    LOAN,
    LOAN_DISBURSEMENT,
    LOAN_PAYMENT,
    OTHER,
    RENT,
    SALARY,
    SAVINGS,
    SHOPPING,
    SUBSCRIPTION,
    TAX,
    TRANSPORT,
    TRAVEL,
    UTILITIES;

    public static boolean isValid(String category) {
      if (StringUtils.isBlank(category)) {
        return true;
      }

      try {
        valueOf(category);
        return true;
      } catch (IllegalArgumentException _) {
        return false;
      }
    }
  }

  public TransactionResult toResult() {
    return TransactionResult.builder()
        .id(this.id().value())
        .user(this.user().value())
        .title(this.title())
        .description(this.description())
        .date(this.toEpochMilli(this.date()))
        .time(this.toMinutesSinceMidnight(this.time()))
        .source(this.source() != null ? this.source().value() : null)
        .destination(this.destination() != null ? this.destination().value() : null)
        .loan(this.loan() != null ? this.loan().value() : null)
        .amount(this.amount())
        .currency(this.currency().getCurrencyCode())
        .category(this.category() != null ? this.category().name() : null)
        .type(this.type().name())
        .build();
  }

  public Transaction convertFromTransfer(Id accountId, String requester) {
    var isSourceAccountRemoved = this.source() != null && this.source().equals(accountId);
    var newType = isSourceAccountRemoved ? Transaction.Type.CREDIT : Transaction.Type.DEBIT;

    return Transaction.builder()
        .id(this.id())
        .user(this.user())
        .title(this.title())
        .description(this.description())
        .date(this.date())
        .time(this.time())
        .loan(this.loan())
        .amount(this.amount())
        .currency(this.currency())
        .category(this.category())
        .status(this.status())
        .createdAt(this.createdAt())
        .createdBy(this.createdBy())
        .updatedAt(LocalDateTime.now())
        .updatedBy(requester)
        .source(isSourceAccountRemoved ? null : this.source())
        .destination(isSourceAccountRemoved ? this.destination() : null)
        .type(newType)
        .build();
  }
}
