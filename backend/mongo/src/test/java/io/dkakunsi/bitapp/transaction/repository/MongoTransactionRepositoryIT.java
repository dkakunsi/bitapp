package io.dkakunsi.bitapp.transaction.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Currency;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.morphia.Datastore;
import io.dkakunsi.bitapp.common.EnvironmentConfiguration;
import io.dkakunsi.bitapp.domain.entity.EntityStatus;
import io.dkakunsi.bitapp.domain.entity.Id;
import io.dkakunsi.bitapp.mongo.MongoConfiguration;
import io.dkakunsi.bitapp.test.MongoServer;
import io.dkakunsi.bitapp.transaction.entity.Transaction;

public final class MongoTransactionRepositoryIT {

  private static MongoConfiguration mongoConfiguration;
  private static Datastore datastore;

  private MongoTransactionRepository repository;

  private static final String REQUESTER = "test@email.com";
  private static final String ACCOUNT_ID = "account-123";

  @BeforeAll
  public static void startMongo() throws Exception {
    MongoServer.startDb();
    var dbConfig = MongoServer.getDbConfig();
    var configuration = EnvironmentConfiguration.of(dbConfig::get);

    mongoConfiguration = new MongoConfiguration(configuration);
    datastore = mongoConfiguration.getDatastore();
  }

  @AfterAll
  public static void stopMongo() throws Exception {
    if (mongoConfiguration != null) {
      mongoConfiguration.close();
    }
    MongoServer.stopDb();
  }

  @BeforeEach
  public void setUp() {
    // Clear the collection before each test
    datastore.getDatabase().getCollection("transactions").drop();
    repository = new MongoTransactionRepository(datastore);
  }

  @Test
  public void givenValidDebitTransactionWhenCreatedThenShouldSaveToDatastore() {
    // Given
    var transactionId = Id.generate();
    var transaction = Transaction.builder()
        .id(transactionId)
        .user(Id.of(REQUESTER))
        .title("Grocery Shopping")
        .description("Monthly groceries")
        .date(LocalDate.of(2026, 1, 22))
        .time(LocalTime.of(10, 30))
        .source(Id.of(ACCOUNT_ID))
        .amount(BigDecimal.valueOf(50000))
        .currency(Currency.getInstance("IDR"))
        .category(Transaction.Category.FOOD)
        .type(Transaction.Type.DEBIT)
        .status(EntityStatus.ACTIVE)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .createdBy(REQUESTER)
        .updatedBy(REQUESTER)
        .build();

    // When
    var result = repository.create(transaction);

    // Then
    assertNotNull(result);
    assertEquals(transactionId, result.id());
    assertEquals(REQUESTER, result.user().value());
    assertEquals("Grocery Shopping", result.title());
    assertEquals("Monthly groceries", result.description());
    assertEquals(LocalDate.of(2026, 1, 22), result.date());
    assertEquals(LocalTime.of(10, 30), result.time());
    assertEquals(ACCOUNT_ID, result.source().value());
    assertEquals(BigDecimal.valueOf(50000).doubleValue(), result.amount().doubleValue());
    assertEquals(Currency.getInstance("IDR"), result.currency());
    assertEquals(Transaction.Category.FOOD, result.category());
    assertEquals(Transaction.Type.DEBIT, result.type());
    assertEquals(EntityStatus.ACTIVE, result.status());
    assertEquals(REQUESTER, result.createdBy());
    assertEquals(REQUESTER, result.updatedBy());
  }

  @Test
  public void givenValidCreditTransactionWhenCreatedThenShouldSaveToDatastore() {
    // Given
    var transactionId = Id.generate();
    var transaction = Transaction.builder()
        .id(transactionId)
        .user(Id.of(REQUESTER))
        .title("Salary")
        .description("Monthly salary")
        .date(LocalDate.of(2026, 1, 22))
        .time(LocalTime.of(8, 0))
        .destination(Id.of(ACCOUNT_ID))
        .amount(BigDecimal.valueOf(5000000))
        .currency(Currency.getInstance("IDR"))
        .category(Transaction.Category.INCOME)
        .type(Transaction.Type.CREDIT)
        .status(EntityStatus.ACTIVE)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .createdBy(REQUESTER)
        .updatedBy(REQUESTER)
        .build();

    // When
    var result = repository.create(transaction);

    // Then
    assertNotNull(result);
    assertEquals(transactionId, result.id());
    assertEquals(Transaction.Type.CREDIT, result.type());
    assertEquals(ACCOUNT_ID, result.destination().value());
    assertEquals("Salary", result.title());
    assertEquals("Monthly salary", result.description());
  }

  @Test
  public void givenValidTransferTransactionWhenCreatedThenShouldSaveToDatastore() {
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
        .amount(BigDecimal.valueOf(100000))
        .currency(Currency.getInstance("IDR"))
        .category(Transaction.Category.OTHER)
        .type(Transaction.Type.TRANSFER)
        .status(EntityStatus.ACTIVE)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .createdBy(REQUESTER)
        .updatedBy(REQUESTER)
        .build();

    // When
    var result = repository.create(transaction);

    // Then
    assertNotNull(result);
    assertEquals(sourceId, result.source().value());
    assertEquals(destId, result.destination().value());
    assertEquals(Transaction.Type.TRANSFER, result.type());
  }

  @Test
  public void givenTransactionWithLoanWhenCreatedThenShouldSaveLoanId() {
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
        .amount(BigDecimal.valueOf(100000))
        .currency(Currency.getInstance("IDR"))
        .category(Transaction.Category.LOAN)
        .type(Transaction.Type.DEBIT)
        .status(EntityStatus.ACTIVE)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .createdBy(REQUESTER)
        .updatedBy(REQUESTER)
        .build();

    // When
    var result = repository.create(transaction);

    // Then
    assertNotNull(result);
    assertEquals(loanId, result.loan().value());
    assertEquals(Transaction.Category.LOAN, result.category());
  }

  @Test
  public void givenTransactionWithNullOptionalFieldsWhenCreatedThenShouldSaveWithNulls() {
    // Given
    var transaction = Transaction.builder()
        .id(Id.generate())
        .user(Id.of(REQUESTER))
        .title("Simple Transaction")
        .date(LocalDate.now())
        .time(LocalTime.now())
        .source(Id.of(ACCOUNT_ID))
        .amount(BigDecimal.valueOf(10000))
        .currency(Currency.getInstance("IDR"))
        .type(Transaction.Type.DEBIT)
        .status(EntityStatus.ACTIVE)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .createdBy(REQUESTER)
        .updatedBy(REQUESTER)
        .build();

    // When
    var result = repository.create(transaction);

    // Then
    assertNotNull(result);
    assertEquals(null, result.description());
    assertEquals(null, result.destination());
    assertEquals(null, result.loan());
    assertEquals(null, result.category());
  }

  @Test
  public void givenTransactionWhenCreatedThenShouldPreserveAuditFields() {
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
        .amount(BigDecimal.valueOf(50000))
        .currency(Currency.getInstance("IDR"))
        .category(Transaction.Category.FOOD)
        .type(Transaction.Type.DEBIT)
        .status(EntityStatus.ACTIVE)
        .createdAt(createdAt)
        .updatedAt(updatedAt)
        .createdBy(REQUESTER)
        .updatedBy(REQUESTER)
        .build();

    // When
    var result = repository.create(transaction);

    // Then
    assertNotNull(result);
    assertNotNull(result.createdAt());
    assertNotNull(result.updatedAt());
    assertEquals(REQUESTER, result.createdBy());
    assertEquals(REQUESTER, result.updatedBy());
    assertEquals(EntityStatus.ACTIVE, result.status());
  }

  @Test
  public void givenUserIdWithMultipleTransactionsWhenFindByUserIdThenShouldReturnAllUserTransactions() {
    // Given
    var userId = Id.of(REQUESTER);
    var transaction1 = Transaction.builder()
        .id(Id.of("trans-1"))
        .user(userId)
        .title("Grocery Shopping")
        .description("Monthly groceries")
        .date(LocalDate.of(2026, 1, 22))
        .time(LocalTime.of(10, 30))
        .source(Id.of(ACCOUNT_ID))
        .amount(BigDecimal.valueOf(50000))
        .currency(Currency.getInstance("IDR"))
        .category(Transaction.Category.FOOD)
        .type(Transaction.Type.DEBIT)
        .status(EntityStatus.ACTIVE)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .createdBy(REQUESTER)
        .updatedBy(REQUESTER)
        .build();

    var transaction2 = Transaction.builder()
        .id(Id.of("trans-2"))
        .user(userId)
        .title("Salary")
        .description("Monthly salary")
        .date(LocalDate.of(2026, 1, 22))
        .time(LocalTime.of(8, 0))
        .destination(Id.of(ACCOUNT_ID))
        .amount(BigDecimal.valueOf(5000000))
        .currency(Currency.getInstance("IDR"))
        .category(Transaction.Category.INCOME)
        .type(Transaction.Type.CREDIT)
        .status(EntityStatus.ACTIVE)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .createdBy(REQUESTER)
        .updatedBy(REQUESTER)
        .build();

    repository.create(transaction1);
    repository.create(transaction2);

    // When
    var transactions = repository.findByUserId(userId);

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
  public void givenUserIdWithNoTransactionsWhenFindByUserIdThenShouldReturnEmptyList() {
    // Given
    var userId = Id.of("user-no-transactions@email.com");

    // When
    var transactions = repository.findByUserId(userId);

    // Then
    assertNotNull(transactions);
    assertTrue(transactions.isEmpty());
  }

  @Test
  public void givenMultipleUsersWithTransactionsWhenFindByUserIdThenShouldReturnOnlyUserTransactions() {
    // Given
    var userId1 = Id.of("user1@email.com");
    var userId2 = Id.of("user2@email.com");

    var user1Transaction = Transaction.builder()
        .id(Id.of("trans-user1"))
        .user(userId1)
        .title("User 1 Transaction")
        .date(LocalDate.now())
        .time(LocalTime.now())
        .source(Id.of(ACCOUNT_ID))
        .amount(BigDecimal.valueOf(10000))
        .currency(Currency.getInstance("IDR"))
        .type(Transaction.Type.DEBIT)
        .status(EntityStatus.ACTIVE)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .createdBy(userId1.value())
        .updatedBy(userId1.value())
        .build();

    var user2Transaction = Transaction.builder()
        .id(Id.of("trans-user2"))
        .user(userId2)
        .title("User 2 Transaction")
        .date(LocalDate.now())
        .time(LocalTime.now())
        .source(Id.of(ACCOUNT_ID))
        .amount(BigDecimal.valueOf(20000))
        .currency(Currency.getInstance("IDR"))
        .type(Transaction.Type.DEBIT)
        .status(EntityStatus.ACTIVE)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .createdBy(userId2.value())
        .updatedBy(userId2.value())
        .build();

    repository.create(user1Transaction);
    repository.create(user2Transaction);

    // When
    var user1Transactions = repository.findByUserId(userId1);

    // Then
    assertNotNull(user1Transactions);
    assertEquals(1, user1Transactions.size());
    assertEquals(userId1, user1Transactions.get(0).user());
    assertEquals("User 1 Transaction", user1Transactions.get(0).title());
  }

  @Test
  public void givenTransactionIdWhenDeletedThenShouldRemoveFromDatastore() {
    // Given
    var transactionId = Id.of("trans-delete");
    var transaction = Transaction.builder()
        .id(transactionId)
        .user(Id.of(REQUESTER))
        .title("Transaction to Delete")
        .date(LocalDate.now())
        .time(LocalTime.now())
        .source(Id.of(ACCOUNT_ID))
        .amount(BigDecimal.valueOf(10000))
        .currency(Currency.getInstance("IDR"))
        .type(Transaction.Type.DEBIT)
        .status(EntityStatus.ACTIVE)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .createdBy(REQUESTER)
        .updatedBy(REQUESTER)
        .build();

    repository.create(transaction);

    // When
    repository.deleteById(transactionId);

    // Then
    var transactions = repository.findByUserId(Id.of(REQUESTER));
    assertTrue(transactions.isEmpty());
  }
}
