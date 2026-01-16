package io.dkakunsi.bitapp.loan.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Currency;

import io.dkakunsi.bitapp.common.ModelStatus;
import io.dkakunsi.bitapp.user.model.User;

public final record Loan(
    String id,
    User user,

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

    ModelStatus status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    String createdBy,
    String updatedBy) {

  public static enum Type {
    BORROW,
    LEND
  }
}
