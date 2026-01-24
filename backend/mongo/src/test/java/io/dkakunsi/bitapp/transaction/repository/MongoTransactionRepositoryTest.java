package io.dkakunsi.bitapp.transaction.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import dev.morphia.Datastore;
import dev.morphia.query.MorphiaQuery;
import dev.morphia.query.filters.Filter;
import io.dkakunsi.bitapp.domain.entity.EntityStatus;
import io.dkakunsi.bitapp.domain.entity.Id;
import io.dkakunsi.bitapp.transaction.entity.Transaction;
import io.dkakunsi.bitapp.transaction.model.TransactionModel;

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

  @Test
  @SuppressWarnings("unchecked")
  void givenUserIdWithMultipleTransactionsWhenFindByUserIdThenShouldReturnAllUserTransactions() {
    // Given
    var userId = Id.of(REQUESTER);
    var transaction1Model = TransactionModel.builder()
        .id("trans-1")
        .userId(userId.value())
        .title("Grocery Shopping")
        .description("Monthly groceries")
        .date(LocalDate.of(2026, 1, 22))
        .time(LocalTime.of(10, 30))
        .source(ACCOUNT_ID)
        .amount(50000L)
        .currency("IDR")
        .category("FOOD")
        .type("DEBIT")
        .status(EntityStatus.ACTIVE.name())
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .createdBy(REQUESTER)
        .updatedBy(REQUESTER)
        .build();

    var transaction2Model = TransactionModel.builder()
        .id("trans-2")
        .userId(userId.value())
        .title("Salary")
        .description("Monthly salary")
        .date(LocalDate.of(2026, 1, 22))
        .time(LocalTime.of(8, 0))
        .destination(ACCOUNT_ID)
        .amount(5000000L)
        .currency("IDR")
        .category("INCOME")
        .type("CREDIT")
        .status(EntityStatus.ACTIVE.name())
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .createdBy(REQUESTER)
        .updatedBy(REQUESTER)
        .build();

    var query = mock(MorphiaQuery.class);
    when(datastore.find(TransactionModel.class)).thenReturn(query);
    when(query.filter(any(Filter.class))).thenReturn(query);
    when(query.stream()).thenReturn(Stream.of(transaction1Model, transaction2Model));

    // When
    var transactions = underTest.findByUserId(userId.value());

    // Then
    assertNotNull(transactions);
    assertEquals(2, transactions.size());

    var firstTransaction = transactions.get(0);
    assertEquals("trans-1", firstTransaction.id().value());
    assertEquals(userId.value(), firstTransaction.user().value());
    assertEquals("Grocery Shopping", firstTransaction.title());
    assertEquals(Transaction.Type.DEBIT, firstTransaction.type());

    var secondTransaction = transactions.get(1);
    assertEquals("trans-2", secondTransaction.id().value());
    assertEquals("Salary", secondTransaction.title());
    assertEquals(Transaction.Type.CREDIT, secondTransaction.type());
  }

  @Test
  @SuppressWarnings("unchecked")
  void givenUserIdWithNoTransactionsWhenFindByUserIdThenShouldReturnEmptyList() {
    // Given
    var userId = "user-no-transactions@email.com";

    var query = mock(MorphiaQuery.class);
    when(datastore.find(TransactionModel.class)).thenReturn(query);
    when(query.filter(any(Filter.class))).thenReturn(query);
    when(query.stream()).thenReturn(Stream.empty());

    // When
    var transactions = underTest.findByUserId(userId);

    // Then
    assertNotNull(transactions);
    assertTrue(transactions.isEmpty());
  }

  @Test
  @SuppressWarnings("unchecked")
  void givenMultipleUsersWithTransactionsWhenFindByUserIdThenShouldReturnOnlyUserTransactions() {
    // Given
    var userId1 = Id.of("user1@email.com");

    var user1Transaction = TransactionModel.builder()
        .id("trans-user1")
        .userId(userId1.value())
        .title("User 1 Transaction")
        .date(LocalDate.now())
        .time(LocalTime.now())
        .source(ACCOUNT_ID)
        .amount(10000L)
        .currency("IDR")
        .type("DEBIT")
        .status(EntityStatus.ACTIVE.name())
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .createdBy(userId1.value())
        .updatedBy(userId1.value())
        .build();

    var query = mock(MorphiaQuery.class);
    when(datastore.find(TransactionModel.class)).thenReturn(query);
    when(query.filter(any(Filter.class))).thenReturn(query);
    when(query.stream()).thenReturn(Stream.of(user1Transaction));

    // When
    var user1Transactions = underTest.findByUserId(userId1.value());

    // Then
    assertNotNull(user1Transactions);
    assertEquals(1, user1Transactions.size());
    assertEquals(userId1.value(), user1Transactions.get(0).user().value());
    assertEquals("User 1 Transaction", user1Transactions.get(0).title());
  }

  @Test
  @SuppressWarnings("unchecked")
  void givenTransactionIdWhenDeletedThenShouldRemoveFromDatastore() {
    // Given
    var transactionId = "trans-delete";
    var query = mock(MorphiaQuery.class);
    when(datastore.find(TransactionModel.class)).thenReturn(query);
    when(query.filter(any(Filter.class))).thenReturn(query);

    // When
    underTest.deleteById(transactionId);

    // Then
    verify(query).delete();
  }
}
