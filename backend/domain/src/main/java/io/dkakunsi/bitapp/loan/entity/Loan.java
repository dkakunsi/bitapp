package io.dkakunsi.bitapp.loan.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Currency;

import org.apache.commons.lang3.StringUtils;

import io.dkakunsi.bitapp.common.DateTimeConverter;
import io.dkakunsi.bitapp.domain.entity.EntityStatus;
import io.dkakunsi.bitapp.domain.entity.Id;
import io.dkakunsi.bitapp.loan.dto.LoanResult;
import io.dkakunsi.bitapp.loan.dto.UpdateLoanInput;
import lombok.Builder;

@Builder
public final record Loan(
    Id id,
    Id user,
    Id account,

    Type type,
    LocalDate date,
    LocalTime time,
    String partyName,
    String title,
    String description,

    BigDecimal amount,
    BigDecimal remainingAmount,
    Currency currency,
    double interestRate,

    EntityStatus status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    String createdBy,
    String updatedBy) implements DateTimeConverter {

  public static final String DEFAULT_CURRENCY = "IDR";
  private static final int TIME_FORMAT_LENGTH = 5;

  public static enum Type {
    BORROW,
    LEND;

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

  public Loan update(UpdateLoanInput input, String requester) {
    final var now = LocalDateTime.now();

    LocalDate updatedDate = this.date;
    if (input.date() != null) {
      updatedDate = parseDate(input.date());
    }

    LocalTime updatedTime = this.time;
    if (input.time() != null) {
      updatedTime = parseTime(input.time());
    }

    Currency updatedCurrency = this.currency;
    if (input.currency() != null && !input.currency().isBlank()) {
      updatedCurrency = Currency.getInstance(input.currency());
    }

    var updatedAmount = this.amount;
    var updatedRemainingAmount = this.remainingAmount;
    if (input.amount() != null) {
      updatedAmount = input.amount();
      // Calculate repaid amount and adjust remaining amount
      var repaidAmount = this.amount.subtract(this.remainingAmount);
      updatedRemainingAmount = updatedAmount.subtract(repaidAmount);
    }

    double updatedInterestRate = this.interestRate;
    if (input.interestRate() != null) {
      updatedInterestRate = input.interestRate();
    }

    String updatedPartyName = this.partyName;
    if (input.partyName() != null) {
      updatedPartyName = input.partyName();
    }

    String updatedTitle = this.title;
    if (input.title() != null) {
      updatedTitle = input.title();
    }

    String updatedDescription = this.description;
    if (input.description() != null) {
      updatedDescription = input.description();
    }

    return Loan.builder()
        .id(this.id)
        .user(this.user)
        .account(this.account)
        .type(this.type)
        .date(updatedDate)
        .time(updatedTime)
        .partyName(updatedPartyName)
        .title(updatedTitle)
        .description(updatedDescription)
        .amount(updatedAmount)
        .remainingAmount(updatedRemainingAmount)
        .currency(updatedCurrency)
        .interestRate(updatedInterestRate)
        .status(this.status)
        .createdAt(this.createdAt)
        .updatedAt(now)
        .createdBy(this.createdBy)
        .updatedBy(requester)
        .build();
  }

  public Loan updateRemainingAmount(BigDecimal newRemainingAmount) {
    return Loan.builder()
        .id(this.id)
        .user(this.user)
        .account(this.account)
        .type(this.type)
        .date(this.date)
        .time(this.time)
        .partyName(this.partyName)
        .title(this.title)
        .description(this.description)
        .amount(this.amount)
        .remainingAmount(newRemainingAmount)
        .currency(this.currency)
        .interestRate(this.interestRate)
        .status(this.status)
        .createdAt(this.createdAt)
        .updatedAt(this.updatedAt)
        .createdBy(this.createdBy)
        .updatedBy(this.updatedBy)
        .build();
  }

  public LoanResult toResult() {
    return LoanResult.builder()
        .id(this.id().value())
        .user(this.user().value())
        .account(this.account().value())
        .type(this.type().name())
        .date(toEpochMilli(this.date()))
        .time(toMinutesSinceMidnight(this.time()))
        .partyName(this.partyName())
        .title(this.title())
        .description(this.description())
        .amount(this.amount())
        .remainingAmount(this.remainingAmount())
        .currency(this.currency().getCurrencyCode())
        .interestRate(this.interestRate())
        .build();
  }

  public boolean isOwner(String requester) {
    return this.user().value().equals(requester);
  }
}
