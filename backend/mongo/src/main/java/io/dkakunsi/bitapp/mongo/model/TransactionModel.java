package io.dkakunsi.bitapp.mongo.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import dev.morphia.annotations.Entity;
import io.dkakunsi.bitapp.common.EntityStatus;
import io.dkakunsi.bitapp.common.Id;
import io.dkakunsi.bitapp.transaction.entity.Transaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity("transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionModel {

  @dev.morphia.annotations.Id
  private String id;
  private String userId;
  private String title;
  private String description;
  private LocalDate date;
  private LocalTime time;
  private String source;
  private String destination;
  private String loan;
  private Long amount;
  private String currency;
  private String category;
  private String type;

  private String status;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private String createdBy;
  private String updatedBy;

  public Transaction toTransaction() {
    var transactionId = Id.of(this.id);
    var userIdObj = Id.of(this.userId);

    return Transaction.builder()
        .id(transactionId)
        .user(userIdObj)
        .title(this.title)
        .description(this.description)
        .date(this.date)
        .time(this.time)
        .source(this.source != null ? Id.of(this.source) : null)
        .destination(this.destination != null ? Id.of(this.destination) : null)
        .loan(this.loan != null ? Id.of(this.loan) : null)
        .amount(this.amount)
        .currency(this.currency)
        .category(this.category != null ? Transaction.Category.valueOf(this.category) : null)
        .type(Transaction.Type.valueOf(this.type))
        .status(EntityStatus.valueOf(this.status))
        .createdAt(this.createdAt)
        .updatedAt(this.updatedAt)
        .createdBy(this.createdBy)
        .updatedBy(this.updatedBy)
        .build();
  }

  public static TransactionModel fromTransaction(Transaction transaction) {
    return new TransactionModel(
        transaction.id().value(),
        transaction.user().value(),
        transaction.title(),
        transaction.description(),
        transaction.date(),
        transaction.time(),
        transaction.source() != null ? transaction.source().value() : null,
        transaction.destination() != null ? transaction.destination().value() : null,
        transaction.loan() != null ? transaction.loan().value() : null,
        transaction.amount(),
        transaction.currency(),
        transaction.category() != null ? transaction.category().name() : null,
        transaction.type().name(),
        transaction.status().name(),
        transaction.createdAt(),
        transaction.updatedAt(),
        transaction.createdBy(),
        transaction.updatedBy());
  }
}
