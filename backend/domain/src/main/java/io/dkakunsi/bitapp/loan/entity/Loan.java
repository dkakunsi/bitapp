package io.dkakunsi.bitapp.loan.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Currency;

import org.apache.commons.lang3.StringUtils;

import io.dkakunsi.bitapp.common.EntityStatus;
import io.dkakunsi.bitapp.common.Id;
import io.dkakunsi.bitapp.loan.dto.CreateLoanInput;
import io.dkakunsi.bitapp.loan.dto.LoanResult;
import io.dkakunsi.bitapp.loan.dto.UpdateLoanInput;
import lombok.Builder;

@Builder
public final record Loan(
    Id id,
    Id user,

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
    String updatedBy) {

  private static final String DEFAULT_CURRENCY = "IDR";
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
      } catch (IllegalArgumentException e) {
        return false;
      }
    }
  }

  public static Loan from(CreateLoanInput input, String requester) {
    final var userId = Id.of(requester);
    final var now = LocalDateTime.now();
    final var executor = requester;

    LocalDate loanDate = (input.date() != null && !input.date().isBlank())
        ? LocalDate.parse(input.date())
        : now.toLocalDate();

    LocalTime loanTime = (input.time() != null && !input.time().isBlank())
        ? LocalTime.parse(input.time())
        : now.toLocalTime();

    Currency curr = (input.currency() != null && !input.currency().isBlank())
        ? Currency.getInstance(input.currency())
        : Currency.getInstance(DEFAULT_CURRENCY);

    return new Loan(
        Id.generate(),
        userId,
        Loan.Type.valueOf(input.type()),
        loanDate,
        loanTime,
        input.partyName(),
        input.title(),
        input.description(),
        input.amount(),
        input.amount(), // remainingAmount equals amount initially
        curr,
        input.interestRate(),
        EntityStatus.ACTIVE,
        now,
        now,
        executor,
        executor);
  }

  public Loan update(UpdateLoanInput input, String requester) {
    final var now = LocalDateTime.now();

    LocalDate updatedDate = this.date;
    if (input.date() != null && !input.date().isBlank()) {
      updatedDate = LocalDate.parse(input.date());
    }

    LocalTime updatedTime = this.time;
    if (input.time() != null && !input.time().isBlank()) {
      updatedTime = LocalTime.parse(input.time());
    }

    Currency updatedCurrency = this.currency;
    if (input.currency() != null && !input.currency().isBlank()) {
      updatedCurrency = Currency.getInstance(input.currency());
    }

    BigDecimal updatedAmount = this.amount;
    BigDecimal updatedRemainingAmount = this.remainingAmount;
    if (input.amount() != null) {
      updatedAmount = input.amount();
      updatedRemainingAmount = input.amount();
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

    return new Loan(
        this.id,
        this.user,
        this.type,
        updatedDate,
        updatedTime,
        updatedPartyName,
        updatedTitle,
        updatedDescription,
        updatedAmount,
        updatedRemainingAmount,
        updatedCurrency,
        updatedInterestRate,
        this.status,
        this.createdAt,
        now,
        this.createdBy,
        requester);
  }

  public LoanResult toResult() {
    String timeStr = this.time().toString();
    if (timeStr.length() > TIME_FORMAT_LENGTH) {
      timeStr = timeStr.substring(0, TIME_FORMAT_LENGTH);
    }
    
    return LoanResult.builder()
        .id(this.id().value())
        .user(this.user().value())
        .type(this.type().name())
        .date(this.date().toString())
        .time(timeStr)
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
