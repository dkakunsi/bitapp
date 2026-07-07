package io.dkakunsi.bitapp.loan.application.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;

import org.apache.commons.lang3.StringUtils;

import io.dkakunsi.bitapp.DateTimeConverter;
import io.dkakunsi.bitapp.Validatable;
import io.dkakunsi.bitapp.loan.domain.entity.Loan;
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

  public Loan toLoan() {
    var updatedDate = date != null ? parseDate(date) : null;
    var updatedTime = time != null ? parseTime(time) : null;
    var updatedCurrency = currency != null && !currency.isBlank() ? Currency.getInstance(currency) : null;
    var updatedAmount = amount != null ? amount : null;
    var updatedInterestRate = interestRate != null ? interestRate : null;
    var updatedPartyName = partyName != null ? partyName : null;
    var updatedTitle = title != null ? title : null;
    var updatedDescription = description != null ? description : null;

    return Loan.builder()
        .date(updatedDate)
        .time(updatedTime)
        .partyName(updatedPartyName)
        .title(updatedTitle)
        .description(updatedDescription)
        .amount(updatedAmount)
        .currency(updatedCurrency)
        .interestRate(updatedInterestRate)
        .build();
  }
}
