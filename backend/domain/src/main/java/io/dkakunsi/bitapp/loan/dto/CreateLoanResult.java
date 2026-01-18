package io.dkakunsi.bitapp.loan.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import io.dkakunsi.bitapp.loan.entity.Loan;
import lombok.Builder;

@Builder
public final record CreateLoanResult(
    String id,
    String user,
    String type,
    LocalDate date,
    LocalTime time,
    String partyName,
    String title,
    String description,
    BigDecimal amount,
    BigDecimal remainingAmount,
    String currency,
    double interestRate,
    String status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    String createdBy,
    String updatedBy) {

  public static CreateLoanResult from(Loan loan) {
    return CreateLoanResult.builder()
        .id(loan.id().value())
        .user(loan.user().value())
        .type(loan.type().name())
        .date(loan.date())
        .time(loan.time())
        .partyName(loan.partyName())
        .title(loan.title())
        .description(loan.description())
        .amount(loan.amount())
        .remainingAmount(loan.remainingAmount())
        .currency(loan.currency().getCurrencyCode())
        .interestRate(loan.interestRate())
        .status(loan.status().name())
        .createdAt(loan.createdAt())
        .updatedAt(loan.updatedAt())
        .createdBy(loan.createdBy())
        .updatedBy(loan.updatedBy())
        .build();
  }
}
