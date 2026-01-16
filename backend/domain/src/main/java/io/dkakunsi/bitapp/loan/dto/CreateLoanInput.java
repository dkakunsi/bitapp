package io.dkakunsi.bitapp.loan.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Currency;

import io.dkakunsi.bitapp.common.Id;
import io.dkakunsi.bitapp.common.ModelStatus;
import io.dkakunsi.bitapp.loan.model.Loan;
import io.dkakunsi.bitapp.loan.validation.ValidDate;
import io.dkakunsi.bitapp.loan.validation.ValidLoanType;
import io.dkakunsi.bitapp.loan.validation.ValidTime;
import io.dkakunsi.bitapp.user.model.User;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public final record CreateLoanInput(
    @NotBlank @ValidLoanType String type,
    @ValidDate String date,
    @ValidTime String time,
    String partyName,
    @NotBlank String title,
    String description,
    @NotNull @DecimalMin(value = "0.01", message = "must be greater than 0") BigDecimal amount,
    String currency,
    @DecimalMin(value = "0", message = "must be greater than or equal to 0") double interestRate) {

  private static final String DEFAULT_CURRENCY = "IDR";

  public Loan toLoan(String requester) {
    final var userId = Id.of(requester);
    final var user = User.builder().id(userId).build();
    final var now = LocalDateTime.now();
    final var executor = requester;

    LocalDate loanDate = (date != null && !date.isBlank())
        ? LocalDate.parse(date)
        : now.toLocalDate();

    LocalTime loanTime = (time != null && !time.isBlank())
        ? LocalTime.parse(time)
        : now.toLocalTime();

    Currency curr = (currency != null && !currency.isBlank())
        ? Currency.getInstance(currency)
        : Currency.getInstance(DEFAULT_CURRENCY);

    return new Loan(
        Id.generate().value(),
        user,
        Loan.Type.from(type),
        loanDate,
        loanTime,
        partyName,
        title,
        description,
        amount,
        amount, // remainingAmount equals amount initially
        curr,
        interestRate,
        ModelStatus.ACTIVE,
        now,
        now,
        executor,
        executor);
  }
}
