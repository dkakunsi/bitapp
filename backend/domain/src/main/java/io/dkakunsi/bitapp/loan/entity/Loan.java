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

  public LoanResult toResult() {
    return LoanResult.builder()
        .id(this.id().value())
        .user(this.user().value())
        .type(this.type().name())
        .date(this.date().toString())
        .time(this.time().toString())
        .partyName(this.partyName())
        .title(this.title())
        .description(this.description())
        .amount(this.amount())
        .remainingAmount(this.remainingAmount())
        .currency(this.currency().getCurrencyCode())
        .interestRate(this.interestRate())
        .build();
  }
}
