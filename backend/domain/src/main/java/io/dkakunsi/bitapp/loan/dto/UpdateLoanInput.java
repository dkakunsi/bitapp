package io.dkakunsi.bitapp.loan.dto;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;

import io.dkakunsi.bitapp.common.DateTimeConverter;
import io.dkakunsi.bitapp.common.Validatable;
import lombok.Builder;

@Builder
public final record UpdateLoanInput(
    String id,
    String partyName,
    String title,
    String description,
    BigDecimal amount,
    String currency,
    Double interestRate,
    Long date,
    Integer time) implements Validatable, DateTimeConverter {

  @Override
  public void validate() throws IllegalArgumentException {
    var errors = new ArrayList<String>();

    if (StringUtils.isBlank(id)) {
      errors.add("id: invalid value: " + id);
    }
    if (partyName != null && StringUtils.isBlank(partyName)) {
      errors.add("partyName: invalid value: " + partyName);
    }
    if (title != null && StringUtils.isBlank(title)) {
      errors.add("title: invalid value: " + title);
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
    if (amount != null && amount.compareTo(new BigDecimal("0.01")) < 0) {
      errors.add("amount: invalid value: " + amount);
    }
    if (currency != null && StringUtils.isNotBlank(currency)) {
      try {
        java.util.Currency.getInstance(currency);
      } catch (IllegalArgumentException _) {
        errors.add("currency: invalid value: " + currency);
      }
    }
    if (interestRate != null && interestRate < 0) {
      errors.add("interestRate: invalid value: " + interestRate);
    }

    if (!errors.isEmpty()) {
      throw new IllegalArgumentException(String.join(", ", errors));
    }
  }
}
