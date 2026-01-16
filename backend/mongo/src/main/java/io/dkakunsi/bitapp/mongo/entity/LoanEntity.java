package io.dkakunsi.bitapp.mongo.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Currency;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import io.dkakunsi.bitapp.common.ModelStatus;
import io.dkakunsi.bitapp.loan.model.Loan;
import io.dkakunsi.bitapp.user.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity("loans")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanEntity {

  @Id
  private String id;
  private String userId;
  private String type;
  private LocalDate date;
  private LocalTime time;
  private String partyName;
  private String title;
  private String description;
  private BigDecimal amount;
  private BigDecimal remainingAmount;
  private String currency;
  private double interestRate;
  private String status;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private String createdBy;
  private String updatedBy;

  /**
   * Converts this entity to a domain Loan model.
   */
  public Loan toLoan() {
    var userIdObj = io.dkakunsi.bitapp.common.Id.of(this.userId);
    var user = User.builder()
        .id(userIdObj)
        .build();

    return new Loan(
        this.id,
        user,
        Loan.Type.valueOf(this.type),
        this.date,
        this.time,
        this.partyName,
        this.title,
        this.description,
        this.amount,
        this.remainingAmount,
        Currency.getInstance(this.currency),
        this.interestRate,
        ModelStatus.valueOf(this.status),
        this.createdAt,
        this.updatedAt,
        this.createdBy,
        this.updatedBy);
  }

  /**
   * Creates an entity from a domain Loan model.
   */
  public static LoanEntity fromLoan(Loan loan) {
    return LoanEntity.builder()
        .id(loan.id())
        .userId(loan.user().getId().value())
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
