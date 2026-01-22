package io.dkakunsi.bitapp.mongo.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import dev.morphia.Datastore;
import io.dkakunsi.bitapp.common.EntityStatus;
import io.dkakunsi.bitapp.common.Id;
import io.dkakunsi.bitapp.mongo.model.TransactionModel;
import io.dkakunsi.bitapp.transaction.entity.Transaction;

public final class MongoTransactionRepositoryTest {

  private MongoTransactionRepository underTest;

  private Datastore datastore;

  private static final String REQUESTER = "test@email.com";
  private static final String ACCOUNT_ID = "account-123";

  @BeforeEach
  void setUp() {
    datastore = mock(Datastore.class);
    underTest = new MongoTransactionRepository(datastore);
  }

  @Test
  void givenValidDebitTransactionWhenCreatedThenShouldSaveToDatastore() {
    // Given
    var transaction = Transaction.builder()
        .id(Id.generate())
        .user(Id.of(REQUESTER))
        .title("Grocery Shopping")
        .description("Monthly groceries")
        .date(LocalDate.of(2026, 1, 22))
        .time(LocalTime.of(10, 30))
        .source(Id.of(ACCOUNT_ID))
        .amount(50000L)
        .currency("IDR")
        .category(Transaction.Category.FOOD)
        .type(Transaction.Type.DEBIT)
        .status(EntityStatus.ACTIVE)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .createdBy(REQUESTER)
        .updatedBy(REQUESTER)
        .build();

    when(datastore.save(any(TransactionModel.class))).thenReturn(null);

    // When
    var result = underTest.create(transaction);

    // Then
    assertNotNull(result);
    assertEquals(transaction.id(), result.id());
    assertEquals(transaction.title(), result.title());
    assertEquals(transaction.type(), result.type());

    var modelCaptor = ArgumentCaptor.forClass(TransactionModel.class);
    verify(datastore).save(modelCaptor.capture());
    var savedModel = modelCaptor.getValue();

    assertEquals(transaction.id().value(), savedModel.getId());
    assertEquals(transaction.user().value(), savedModel.getUserId());
    assertEquals(transaction.title(), savedModel.getTitle());
    assertEquals(transaction.description(), savedModel.getDescription());
    assertEquals(transaction.date(), savedModel.getDate());
    assertEquals(transaction.time(), savedModel.getTime());
    assertEquals(transaction.source().value(), savedModel.getSource());
    assertEquals(transaction.amount(), savedModel.getAmount());
    assertEquals(transaction.currency(), savedModel.getCurrency());
    assertEquals(transaction.category().name(), savedModel.getCategory());
    assertEquals(transaction.type().name(), savedModel.getType());
    assertEquals(transaction.status().name(), savedModel.getStatus());
  }

  @Test
  void givenValidCreditTransactionWhenCreatedThenShouldSaveToDatastore() {
    // Given
    var transaction = Transaction.builder()
        .id(Id.generate())
        .user(Id.of(REQUESTER))
        .title("Salary")
        .description("Monthly salary")
        .date(LocalDate.of(2026, 1, 22))
        .time(LocalTime.of(8, 0))
        .destination(Id.of(ACCOUNT_ID))
        .amount(5000000L)
        .currency("IDR")
        .category(Transaction.Category.INCOME)
        .type(Transaction.Type.CREDIT)
        .status(EntityStatus.ACTIVE)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .createdBy(REQUESTER)
        .updatedBy(REQUESTER)
        .build();

    when(datastore.save(any(TransactionModel.class))).thenReturn(null);

    // When
    var result = underTest.create(transaction);

    // Then
    assertNotNull(result);
    assertEquals(transaction.id(), result.id());
    assertEquals(transaction.type(), result.type());

    var modelCaptor = ArgumentCaptor.forClass(TransactionModel.class);
    verify(datastore).save(modelCaptor.capture());
    var savedModel = modelCaptor.getValue();

    assertEquals(transaction.destination().value(), savedModel.getDestination());
    assertEquals(transaction.type().name(), savedModel.getType());
  }

  @Test
  void givenValidTransferTransactionWhenCreatedThenShouldSaveToDatastore() {
    // Given
    var sourceId = "account-source";
    var destId = "account-dest";
    var transaction = Transaction.builder()
        .id(Id.generate())
        .user(Id.of(REQUESTER))
        .title("Transfer")
        .description("Transfer to savings")
        .date(LocalDate.of(2026, 1, 22))
        .time(LocalTime.of(12, 0))
        .source(Id.of(sourceId))
        .destination(Id.of(destId))
        .amount(100000L)
        .currency("IDR")
        .category(Transaction.Category.OTHER)
        .type(Transaction.Type.TRANSFER)
        .status(EntityStatus.ACTIVE)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .createdBy(REQUESTER)
        .updatedBy(REQUESTER)
        .build();

    when(datastore.save(any(TransactionModel.class))).thenReturn(null);

    // When
    var result = underTest.create(transaction);

    // Then
    assertNotNull(result);

    var modelCaptor = ArgumentCaptor.forClass(TransactionModel.class);
    verify(datastore).save(modelCaptor.capture());
    var savedModel = modelCaptor.getValue();

    assertEquals(sourceId, savedModel.getSource());
    assertEquals(destId, savedModel.getDestination());
    assertEquals("TRANSFER", savedModel.getType());
  }

  @Test
  void givenTransactionWithLoanWhenCreatedThenShouldSaveLoanId() {
    // Given
    var loanId = "loan-123";
    var transaction = Transaction.builder()
        .id(Id.generate())
        .user(Id.of(REQUESTER))
        .title("Loan Payment")
        .description("Monthly loan payment")
        .date(LocalDate.of(2026, 1, 22))
        .time(LocalTime.of(14, 0))
        .source(Id.of(ACCOUNT_ID))
        .loan(Id.of(loanId))
        .amount(100000L)
        .currency("IDR")
        .category(Transaction.Category.LOAN)
        .type(Transaction.Type.DEBIT)
        .status(EntityStatus.ACTIVE)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .createdBy(REQUESTER)
        .updatedBy(REQUESTER)
        .build();

    when(datastore.save(any(TransactionModel.class))).thenReturn(null);

    // When
    var result = underTest.create(transaction);

    // Then
    assertNotNull(result);

    var modelCaptor = ArgumentCaptor.forClass(TransactionModel.class);
    verify(datastore).save(modelCaptor.capture());
    var savedModel = modelCaptor.getValue();

    assertEquals(loanId, savedModel.getLoan());
  }

  @Test
  void givenTransactionWithNullOptionalFieldsWhenCreatedThenShouldSaveWithNulls() {
    // Given
    var transaction = Transaction.builder()
        .id(Id.generate())
        .user(Id.of(REQUESTER))
        .title("Simple Transaction")
        .date(LocalDate.now())
        .time(LocalTime.now())
        .source(Id.of(ACCOUNT_ID))
        .amount(10000L)
        .currency("IDR")
        .type(Transaction.Type.DEBIT)
        .status(EntityStatus.ACTIVE)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .createdBy(REQUESTER)
        .updatedBy(REQUESTER)
        .build();

    when(datastore.save(any(TransactionModel.class))).thenReturn(null);

    // When
    var result = underTest.create(transaction);

    // Then
    assertNotNull(result);

    var modelCaptor = ArgumentCaptor.forClass(TransactionModel.class);
    verify(datastore).save(modelCaptor.capture());
    var savedModel = modelCaptor.getValue();

    assertEquals(null, savedModel.getDescription());
    assertEquals(null, savedModel.getDestination());
    assertEquals(null, savedModel.getLoan());
    assertEquals(null, savedModel.getCategory());
  }

  @Test
  void givenTransactionWhenCreatedThenShouldPreserveAuditFields() {
    // Given
    var createdAt = LocalDateTime.of(2026, 1, 22, 10, 0);
    var updatedAt = LocalDateTime.of(2026, 1, 22, 11, 0);
    var transaction = Transaction.builder()
        .id(Id.generate())
        .user(Id.of(REQUESTER))
        .title("Test Transaction")
        .date(LocalDate.now())
        .time(LocalTime.now())
        .source(Id.of(ACCOUNT_ID))
        .amount(50000L)
        .currency("IDR")
        .category(Transaction.Category.FOOD)
        .type(Transaction.Type.DEBIT)
        .status(EntityStatus.ACTIVE)
        .createdAt(createdAt)
        .updatedAt(updatedAt)
        .createdBy(REQUESTER)
        .updatedBy(REQUESTER)
        .build();

    when(datastore.save(any(TransactionModel.class))).thenReturn(null);

    // When
    underTest.create(transaction);

    // Then
    var modelCaptor = ArgumentCaptor.forClass(TransactionModel.class);
    verify(datastore).save(modelCaptor.capture());
    var savedModel = modelCaptor.getValue();

    assertEquals(createdAt, savedModel.getCreatedAt());
    assertEquals(updatedAt, savedModel.getUpdatedAt());
    assertEquals(REQUESTER, savedModel.getCreatedBy());
    assertEquals(REQUESTER, savedModel.getUpdatedBy());
    assertEquals(EntityStatus.ACTIVE.name(), savedModel.getStatus());
  }
}
