package io.dkakunsi.bitapp.loan.dto;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;

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
    String date,
    String time) implements Validatable {

  private static final String DATE_REGEX = "^(\\d{4})-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])$";

  private static final String TIME_REGEX = "^([01]\\d|2[0-3]):([0-5]\\d):([0-5]\\d)?$";

  @Override
  public void validate() throws IllegalArgumentException {
    var errors = new ArrayList<String>();

    if (StringUtils.isBlank(id)) {
      errors.add("id: invalid value");
    }
    if (StringUtils.isBlank(partyName)) {
      errors.add("partyName: invalid value");
    }
    if (StringUtils.isBlank(title)) {
      errors.add("title: invalid value");
    }
    if (date != null && (StringUtils.isBlank(date) || !date.matches(DATE_REGEX))) {
      errors.add("date: invalid value");
    }
    if (time != null && (StringUtils.isBlank(time) || !time.matches(TIME_REGEX))) {
      errors.add("time: invalid value");
    }
    if (amount != null && amount.compareTo(new BigDecimal("0.01")) < 0) {
      errors.add("amount: invalid value");
    }
    if (StringUtils.isNotBlank(currency)) {
      try {
        java.util.Currency.getInstance(currency);
      } catch (IllegalArgumentException e) {
        errors.add("currency: invalid value");
      }
    }
    if (interestRate != null && interestRate < 0) {
      errors.add("interestRate: invalid value");
    }

    if (!errors.isEmpty()) {
      throw new IllegalArgumentException(String.join(", ", errors));
    }
  }
}
