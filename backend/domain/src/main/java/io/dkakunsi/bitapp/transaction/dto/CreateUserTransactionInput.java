package io.dkakunsi.bitapp.transaction.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
    LocalDate date,
    LocalTime time,
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
      errors.add("title: invalid value");
    }

    if (!Transaction.Type.isValid(type)) {
      errors.add("type: invalid value");
    }

    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
      errors.add("amount: invalid value");
    }

    if (!Transaction.Category.isValid(category)) {
      errors.add("category: invalid value");
    }

    // Only validate source/destination if type is valid
    if (Transaction.Type.isValid(type)) {
      var transactionType = Transaction.Type.valueOf(type);
      if (transactionType == Transaction.Type.DEBIT && StringUtils.isBlank(source)) {
        errors.add("source: invalid value");
      }

      if (transactionType == Transaction.Type.CREDIT && StringUtils.isBlank(destination)) {
        errors.add("destination: invalid value");
      }

      if (transactionType == Transaction.Type.TRANSFER) {
        if (StringUtils.isBlank(source)) {
          errors.add("source: invalid value");
        }
        if (StringUtils.isBlank(destination)) {
          errors.add("destination: invalid value");
        }
      }
    }

    if (!errors.isEmpty()) {
      throw new IllegalArgumentException(String.join(", ", errors));
    }
  }

  public static CreateUserTransactionInput fromRequest(String title, String description,
      String dateStr, String timeStr, String source, String destination, String loan,
      BigDecimal amount, String currency, String category, String type) {
    LocalDate parsedDate = null;
    LocalTime parsedTime = null;

    if (dateStr != null) {
      try {
        parsedDate = LocalDate.parse(dateStr);
      } catch (DateTimeParseException _) {
        throw new IllegalArgumentException("date: invalid value");
      }
    }

    if (timeStr != null) {
      try {
        parsedTime = LocalTime.parse(timeStr);
      } catch (DateTimeParseException _) {
        throw new IllegalArgumentException("time: invalid value");
      }
    }

    return CreateUserTransactionInput.builder()
        .title(title)
        .description(description)
        .date(parsedDate)
        .time(parsedTime)
        .source(source)
        .destination(destination)
        .loan(loan)
        .amount(amount)
        .currency(currency)
        .category(category)
        .type(type)
        .build();
  }

  @Override
  public Transaction toTransaction(String requester) {
    final var userId = Id.of(requester);
    final var now = LocalDateTime.now();
    final var executor = requester;

    var transactionDate = this.date() != null ? this.date() : LocalDate.now();
    var transactionTime = this.time() != null ? this.time() : LocalTime.now();
    var currency = this.currency() != null ? Currency.getInstance(this.currency()) : Transaction.DEFAULT_CURRENCY;
    var category = this.category() != null ? Transaction.Category.valueOf(this.category()) : null;

    return Transaction.builder()
        .id(Id.generate())
        .user(userId)
        .title(this.title())
        .description(this.description())
        .date(transactionDate)
        .time(transactionTime)
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
}
