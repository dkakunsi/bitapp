package io.dkakunsi.bitapp.loan.infrastructure.mongo.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Currency;

import dev.morphia.annotations.Entity;
import io.dkakunsi.bitapp.Id;
import io.dkakunsi.bitapp.loan.domain.entity.Loan;
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
  private String accountId;
  private String type;
  private LocalDate date;
  private LocalTime time;
  private String partyName;
  private String title;
  private String description;
  private Double amount;
  private Double remainingAmount;
  private String currency;
  private double interestRate;
  private Boolean active;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private String createdBy;
  private String updatedBy;

  /**
   * Converts this entity to a domain Loan model.
   */
  public Loan toLoan() {
    return Loan.builder()
        .id(Id.of(this.id))
        .user(Id.of(this.userId))
        .account(Id.of(accountId))
        .type(Loan.Type.valueOf(this.type))
        .date(this.date)
        .time(this.time)
        .partyName(this.partyName)
        .title(this.title)
        .description(this.description)
        .amount(BigDecimal.valueOf(this.amount))
        .remainingAmount(BigDecimal.valueOf(this.remainingAmount))
        .currency(Currency.getInstance(this.currency))
        .interestRate(this.interestRate)
        .active(this.active)
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
    // Morphia is not working properly with builder pattern, so we have to use the
    // constructor
    return new LoanModel(
        loan.id().value(),
        loan.user().value(),
        loan.account().value(),
        loan.type().name(),
        loan.date(),
        loan.time(),
        loan.partyName(),
        loan.title(),
        loan.description(),
        loan.amount().doubleValue(),
        loan.remainingAmount().doubleValue(),
        loan.currency().getCurrencyCode(),
        loan.interestRate(),
        loan.active(),
        loan.createdAt(),
        loan.updatedAt(),
        loan.createdBy(),
        loan.updatedBy());
  }
}
