package io.dkakunsi.bitapp.loan.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Currency;

import org.apache.commons.lang3.StringUtils;

import io.dkakunsi.bitapp.DateTimeConverter;
import io.dkakunsi.bitapp.EntityStatus;
import io.dkakunsi.bitapp.Id;
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
    Double interestRate,

    EntityStatus status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    String createdBy,
    String updatedBy) implements DateTimeConverter {

  public static final String DEFAULT_CURRENCY = "IDR";

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

  public Long dateInEpochMillis() {
    return toEpochMilli(this.date);
  }

  public Integer timeInMinutesSinceMidnight() {
    return toMinutesSinceMidnight(this.time);
  }

  public Loan update(Loan update, String requester) {
    var updatedDate = update.date() != null ? update.date() : this.date;
    var updatedTime = update.time() != null ? update.time() : this.time;
    var updatedCurrency = update.currency() != null ? update.currency() : this.currency;
    var updatedInterestRate = update.interestRate() != null ? update.interestRate() : this.interestRate;
    var updatedPartyName = update.partyName() != null ? update.partyName() : this.partyName;
    var updatedTitle = update.title() != null ? update.title() : this.title;
    var updatedDescription = update.description() != null ? update.description() : this.description;

    var updatedAmount = this.amount;
    var updatedRemainingAmount = this.remainingAmount;
    if (update.amount() != null && amount.compareTo(update.amount()) != 0) {
      updatedAmount = update.amount();
      // Calculate repaid amount and adjust remaining amount
      var repaidAmount = this.amount.subtract(this.remainingAmount);
      updatedRemainingAmount = updatedAmount.subtract(repaidAmount);
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
        .updatedAt(LocalDateTime.now())
        .createdBy(this.createdBy)
        .updatedBy(requester)
        .build();
  }

  public boolean isOwner(String requester) {
    return this.user().value().equals(requester);
  }
}
