package io.dkakunsi.bitapp.loan.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Currency;

import dev.morphia.annotations.Entity;
import io.dkakunsi.bitapp.common.EntityStatus;
import io.dkakunsi.bitapp.common.Id;
import io.dkakunsi.bitapp.loan.entity.Loan;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity("loans")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanModel {

  @dev.morphia.annotations.Id
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
    var userIdObj = Id.of(this.userId);

    return Loan.builder()
        .id(Id.of(this.id))
        .user(userIdObj)
        .type(Loan.Type.valueOf(this.type))
        .date(this.date)
        .time(this.time)
        .partyName(this.partyName)
        .title(this.title)
        .description(this.description)
        .amount(this.amount)
        .remainingAmount(this.remainingAmount)
        .currency(Currency.getInstance(this.currency))
        .interestRate(this.interestRate)
        .status(EntityStatus.valueOf(this.status))
        .createdAt(this.createdAt)
        .updatedAt(this.updatedAt)
        .createdBy(this.createdBy)
        .updatedBy(this.updatedBy)
        .build();
  }

  /**
   * Creates an entity from a domain Loan model.
   */
  public static LoanModel fromLoan(Loan loan) {
    // Mrophia is not working properly with builder pattern, so we have to use the
    // constructor
    return new LoanModel(
        loan.id().value(),
        loan.user().value(),
        loan.type().name(),
        loan.date(),
        loan.time(),
        loan.partyName(),
        loan.title(),
        loan.description(),
        loan.amount(),
        loan.remainingAmount(),
        loan.currency().getCurrencyCode(),
        loan.interestRate(),
        loan.status().name(),
        loan.createdAt(),
        loan.updatedAt(),
        loan.createdBy(),
        loan.updatedBy());
  }
}
