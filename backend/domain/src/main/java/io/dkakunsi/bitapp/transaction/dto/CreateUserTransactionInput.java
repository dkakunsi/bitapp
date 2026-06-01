package io.dkakunsi.bitapp.transaction.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Currency;

import org.apache.commons.lang3.StringUtils;

import io.dkakunsi.bitapp.common.Validatable;
import io.dkakunsi.bitapp.domain.entity.EntityStatus;
import io.dkakunsi.bitapp.domain.entity.Id;
import io.dkakunsi.bitapp.transaction.entity.Transaction;
import lombok.Builder;

@Builder
public record CreateUserTransactionInput(
    String title,
    String description,
    Long date,
    Integer time,
    String source,
    String destination,
    String loan,
    BigDecimal amount,
    String currency,
    String category,
    String type) implements Validatable, CreateTransactionInput {

  @Override
  public void validate() {
    var errors = new ArrayList<String>();

    if (StringUtils.isBlank(title)) {
      errors.add("title: invalid value: " + title);
    }

    if (!Transaction.Type.isValid(type)) {
      errors.add("type: invalid value: " + type);
    }

    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
      errors.add("amount: invalid value: " + amount);
    }

    if (!Transaction.Category.isValid(category)) {
      errors.add("category: invalid value: " + category);
    }

    if (date != null) {
      try {
        parseDate(date);
      } catch (IllegalArgumentException e) {
        errors.add("date: invalid value: " + date);
      }
    }

    if (time != null) {
      try {
        parseTime(time);
      } catch (IllegalArgumentException e) {
        errors.add("time: invalid value: " + time);
      }
    }

    // Only validate source/destination if type is valid
    if (Transaction.Type.isValid(type)) {
      var transactionType = Transaction.Type.valueOf(type);
      if (transactionType == Transaction.Type.DEBIT && StringUtils.isBlank(source)) {
        errors.add("source: invalid value: " + source);
      }

      if (transactionType == Transaction.Type.CREDIT && StringUtils.isBlank(destination)) {
        errors.add("destination: invalid value: " + destination);
      }

      if (transactionType == Transaction.Type.TRANSFER) {
        if (StringUtils.isBlank(source)) {
          errors.add("source: invalid value: " + source);
        }
        if (StringUtils.isBlank(destination)) {
          errors.add("destination: invalid value: " + destination);
        }
      }
    }

    if (!errors.isEmpty()) {
      throw new IllegalArgumentException(String.join(", ", errors));
    }
  }

  @Override
  public Transaction toTransaction(String requester) {
    final var userId = Id.of(requester);
    final var now = LocalDateTime.now();
    final var executor = requester;

    var currency = this.currency() != null ? Currency.getInstance(this.currency()) : Transaction.DEFAULT_CURRENCY;
    var category = this.category() != null ? Transaction.Category.valueOf(this.category()) : null;

    return Transaction.builder()
        .id(Id.generate())
        .user(userId)
        .title(this.title())
        .description(this.description())
        .date(parseDate(this.date()))
        .time(parseTime(this.time()))
        .source(this.source() != null ? Id.of(this.source()) : null)
        .destination(this.destination() != null ? Id.of(this.destination()) : null)
        .loan(this.loan() != null ? Id.of(this.loan()) : null)
        .amount(this.amount())
        .currency(currency)
        .category(category)
        .type(Transaction.Type.valueOf(this.type()))
        .status(EntityStatus.ACTIVE)
        .createdAt(now)
        .updatedAt(now)
        .createdBy(executor)
        .updatedBy(executor)
        .build();
  }

  private LocalDate parseDate(Long epochMilli) {
    if (epochMilli == null) {
      return Instant.now().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    try {
      return Instant.ofEpochMilli(epochMilli).atZone(ZoneId.systemDefault()).toLocalDate();
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("date: invalid value: " + epochMilli);
    }
  }

  private LocalTime parseTime(Integer timeSinceMidnight) {
    if (timeSinceMidnight == null) {
      return Instant.now().atZone(ZoneId.systemDefault()).toLocalTime();
    }

    var hour = timeSinceMidnight / 60;
    var minute = timeSinceMidnight % 60;
    try {
      return LocalTime.of(hour, minute);
    } catch (DateTimeException e) {
      throw new IllegalArgumentException("time: invalid value: " + timeSinceMidnight);
    }
  }
}
