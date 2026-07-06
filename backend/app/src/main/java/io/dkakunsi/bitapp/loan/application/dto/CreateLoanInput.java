package io.dkakunsi.bitapp.loan.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Currency;

import org.apache.commons.lang3.StringUtils;

import io.dkakunsi.bitapp.DateTimeConverter;
import io.dkakunsi.bitapp.EntityStatus;
import io.dkakunsi.bitapp.Id;
import io.dkakunsi.bitapp.Validatable;
import io.dkakunsi.bitapp.loan.domain.entity.Loan;
import lombok.Builder;

@Builder
public final record CreateLoanInput(
    String type,
    Long date,
    Integer time,
    String partyName,
    String title,
    String description,
    BigDecimal amount,
    String currency,
    double interestRate,
    String account) implements Validatable, DateTimeConverter {

  @Override
  public void validate() throws IllegalArgumentException {
    var errors = new ArrayList<String>();

    if (StringUtils.isBlank(title)) {
      errors.add("title: invalid value: " + title);
    }
    if (!Loan.Type.isValid(type)) {
      errors.add("type: invalid value: " + type);
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
    if (amount == null || amount.compareTo(new BigDecimal("0.01")) < 0) {
      errors.add("amount: invalid value: " + amount);
    }
    if (StringUtils.isNotBlank(currency)) {
      try {
        java.util.Currency.getInstance(currency);
      } catch (

      IllegalArgumentException e) {
        errors.add("currency: invalid value: " + currency);
      }
    }
    if (interestRate < 0) {
      errors.add("interestRate: invalid value: " + interestRate);
    }
    if (account != null && StringUtils.isBlank(account)) {
      errors.add("account: invalid value: " + account);
    }

    if (!errors.isEmpty()) {
      throw new IllegalArgumentException(String.join(", ", errors));
    }
  }

  public Loan toLoan(String requester) {
    final var userId = Id.of(requester);
    final var now = LocalDateTime.now();
    final var executor = requester;

    Currency curr = (this.currency() != null && !this.currency().isBlank())
        ? Currency.getInstance(this.currency())
        : Currency.getInstance(Loan.DEFAULT_CURRENCY);

    return Loan.builder()
        .id(Id.generate())
        .user(userId)
        .account(Id.of(this.account()))
        .type(Loan.Type.valueOf(this.type()))
        .date(parseDate(this.date()))
        .time(parseTime(this.time()))
        .partyName(this.partyName())
        .title(this.title())
        .description(this.description())
        .amount(this.amount())
        .remainingAmount(this.amount()) // remainingAmount equals amount initially
        .currency(curr)
        .interestRate(this.interestRate())
        .status(EntityStatus.ACTIVE)
        .createdAt(now)
        .updatedAt(now)
        .createdBy(executor)
        .updatedBy(executor)
        .build();
  }
}
