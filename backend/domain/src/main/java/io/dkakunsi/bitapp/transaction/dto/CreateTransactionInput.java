package io.dkakunsi.bitapp.transaction.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;

import io.dkakunsi.bitapp.common.Validatable;
import io.dkakunsi.bitapp.transaction.entity.Transaction;
import lombok.Builder;

@Builder
public final record CreateTransactionInput(
    String title,
    String description,
    LocalDate date,
    LocalTime time,
    String source,
    String destination,
    String loan,
    Long amount,
    String currency,
    String category,
    String type) implements Validatable {

  @Override
  public void validate() {
    var errors = new ArrayList<String>();

    if (StringUtils.isBlank(title)) {
      errors.add("title: invalid value");
    }

    if (!Transaction.Type.isValid(type)) {
      errors.add("type: invalid value");
    }

    if (amount == null || amount <= 0) {
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

  public static CreateTransactionInput fromRequest(String title, String description, 
      String dateStr, String timeStr, String source, String destination, String loan,
      Long amount, String currency, String category, String type) {
    LocalDate parsedDate = null;
    LocalTime parsedTime = null;

    if (dateStr != null) {
      try {
        parsedDate = LocalDate.parse(dateStr);
      } catch (DateTimeParseException e) {
        throw new IllegalArgumentException("date: invalid value");
      }
    }

    if (timeStr != null) {
      try {
        parsedTime = LocalTime.parse(timeStr);
      } catch (DateTimeParseException e) {
        throw new IllegalArgumentException("time: invalid value");
      }
    }

    return CreateTransactionInput.builder()
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
}
