package io.dkakunsi.bitapp.transaction.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.morphia.Datastore;
import io.dkakunsi.bitapp.account.entity.Account;
import io.dkakunsi.bitapp.account.entity.Account.Type;
import io.dkakunsi.bitapp.account.repository.MongoAccountRepository;
import io.dkakunsi.bitapp.common.EnvironmentConfiguration;
import io.dkakunsi.bitapp.domain.entity.EntityStatus;
import io.dkakunsi.bitapp.domain.entity.Id;
import io.dkakunsi.bitapp.mongo.MongoConfiguration;
import io.dkakunsi.bitapp.mongo.MongoSession;
import io.dkakunsi.bitapp.mongo.MongoSessionManager;
import io.dkakunsi.bitapp.test.MongoServer;
import io.dkakunsi.bitapp.transaction.entity.Transaction;

/**
 * Integration tests to verify that MongoDB transaction sessions work correctly
 * and properly rollback when errors occur.
 */
public class MongoTransactionRollbackIT {

  private static MongoConfiguration mongoConfiguration;
  private static Datastore datastore;
  private static MongoSessionManager sessionManager;

  private MongoTransactionRepository transactionRepository;
  private MongoAccountRepository accountRepository;

  @BeforeAll
  public static void startMongo() throws Exception {
    MongoServer.startDb();
    var dbConfig = MongoServer.getDbConfig();
    var configuration = EnvironmentConfiguration.of(dbConfig::get);

    mongoConfiguration = new MongoConfiguration(configuration);
    datastore = mongoConfiguration.getDatastore();
    sessionManager = mongoConfiguration.getSessionManager();
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
    // Clear the collections before each test
    datastore.getDatabase().getCollection("transactions").drop();
    datastore.getDatabase().getCollection("accounts").drop();

    transactionRepository = new MongoTransactionRepository(datastore);
    accountRepository = new MongoAccountRepository(datastore);
  }

  /**
   * <b>Given</b> a transaction session is created<br>
   * <b>When</b> multiple operations are performed and then committed<br>
   * <b>Then</b> all changes should be persisted to the database
   */
  @Test
  public void whenTransactionCommittedThenAllChangesShouldBePersisted() {
    // Given
    var account = createTestAccount("account-1", "user@test.com", new BigDecimal("1000"));
    var transaction = createTestTransaction("tx-1", "user@test.com", "account-1", null);

    // Create account without session first
    var createdAccount = accountRepository.create(account);
    assertNotNull(createdAccount);

    MongoSession session = null;
    try {
      // When - Use session to create transaction and update account
      session = sessionManager.createSession();

      var createdTransaction = transactionRepository.create(transaction);
      assertNotNull(createdTransaction);

      // Update account balance within session
      var updatedAccount = createdAccount.updateBalance(new BigDecimal("950"));
      accountRepository.update(updatedAccount);

      session.commit();

      // Then - Verify changes are persisted
      var retrievedTransaction = transactionRepository.findById(transaction.id().value());
      assertTrue(retrievedTransaction.isPresent());
      assertEquals(transaction.id().value(), retrievedTransaction.get().id().value());

      var retrievedAccount = accountRepository.findById(account.id().value());
      assertTrue(retrievedAccount.isPresent());
      assertEquals(new BigDecimal("950"), retrievedAccount.get().balance());

    } finally {
      if (session != null) {
        session.close();
      }
    }
  }

  /**
   * <b>Given</b> a transaction session is created<br>
   * <b>When</b> multiple operations are performed and then rolled back<br>
   * <b>Then</b> NO changes should be persisted to the database
   * 
   * This test verifies that when a transaction is rolled back, all changes
   * made within the session are discarded and the database remains unchanged.
   */
  @Test
  public void whenTransactionRolledBackThenNoChangesShouldBePersisted() {
    // Given
    var account = createTestAccount("account-rollback-1", "user@test.com", new BigDecimal("1000"));
    var transaction1 = createTestTransaction("tx-rollback-1", "user@test.com", "account-rollback-1", null);
    var transaction2 = createTestTransaction("tx-rollback-2", "user@test.com", "account-rollback-1", null);

    // Create account without session first
    var createdAccount = accountRepository.create(account);
    assertNotNull(createdAccount);
    var initialBalance = createdAccount.balance();

    MongoSession session = null;
    try {
      // When - Use session to perform operations
      session = sessionManager.createSession();

      // Create transactions within session
      var tx1 = transactionRepository.create(transaction1);
      assertNotNull(tx1);

      var tx2 = transactionRepository.create(transaction2);
      assertNotNull(tx2);

      // Update account balance within session
      var updatedAccount = createdAccount.updateBalance(new BigDecimal("800"));
      accountRepository.update(updatedAccount);

      // Rollback the transaction
      session.rollback();

      // Then - Verify NO changes are persisted
      var retrievedTransaction1 = transactionRepository.findById(transaction1.id().value());
      assertFalse(retrievedTransaction1.isPresent(), "Transaction 1 should NOT be persisted after rollback");

      var retrievedTransaction2 = transactionRepository.findById(transaction2.id().value());
      assertFalse(retrievedTransaction2.isPresent(), "Transaction 2 should NOT be persisted after rollback");

      var retrievedAccount = accountRepository.findById(account.id().value());
      assertTrue(retrievedAccount.isPresent());
      assertEquals(initialBalance, retrievedAccount.get().balance(),
          "Account balance should remain unchanged after rollback");

    } finally {
      if (session != null) {
        session.close();
      }
    }
  }

  /**
   * <b>Given</b> a transaction session is created<br>
   * <b>When</b> operations are performed, an error occurs, and rollback is
   * called<br>
   * <b>Then</b> NO changes should be persisted to the database
   * 
   * This simulates a real-world scenario where an error during processing
   * triggers a rollback to maintain data consistency.
   */
  @Test
  public void whenErrorOccursDuringTransactionThenRollbackShouldPreventDataPersistence() {
    // Given
    var account = createTestAccount("account-error-1", "user@test.com", new BigDecimal("1000"));
    var transaction = createTestTransaction("tx-error-1", "user@test.com", "account-error-1", null);

    // Create account first
    var createdAccount = accountRepository.create(account);
    assertNotNull(createdAccount);
    var initialBalance = createdAccount.balance();

    MongoSession session = null;
    boolean errorOccurred = false;

    try {
      // When - Use session to perform operations
      session = sessionManager.createSession();

      // Create transaction within session
      var createdTransaction = transactionRepository.create(transaction);
      assertNotNull(createdTransaction);

      // Update account balance
      var updatedAccount = createdAccount.updateBalance(new BigDecimal("900"));
      accountRepository.update(updatedAccount);

      // Simulate an error occurring
      throw new RuntimeException("Simulated error during transaction processing");

    } catch (RuntimeException e) {
      // Catch the error and rollback
      errorOccurred = true;
      if (session != null) {
        session.rollback();
      }
    } finally {
      if (session != null) {
        session.close();
      }
    }

    // Then - Verify error occurred and changes were rolled back
    assertTrue(errorOccurred, "Error should have occurred during transaction");

    var retrievedTransaction = transactionRepository.findById(transaction.id().value());
    assertFalse(retrievedTransaction.isPresent(),
        "Transaction should NOT be persisted after error and rollback");

    var retrievedAccount = accountRepository.findById(account.id().value());
    assertTrue(retrievedAccount.isPresent());
    assertEquals(initialBalance, retrievedAccount.get().balance(),
        "Account balance should remain unchanged after error and rollback");
  }

  /**
   * <b>Given</b> a transaction session with multiple related updates<br>
   * <b>When</b> transaction is deleted and account is updated within session<br>
   * <b>Then</b> changes should be committed atomically
   */
  @Test
  public void whenDeletingTransactionWithinSessionThenChangesShouldBeAtomic() {
    // Given
    var account = createTestAccount("account-delete-1", "user@test.com", new BigDecimal("1000"));
    var transaction = createTestTransaction("tx-delete-1", "user@test.com", "account-delete-1", null);

    // Create account and transaction first
    var createdAccount = accountRepository.create(account);
    var createdTransaction = transactionRepository.create(transaction);
    assertNotNull(createdAccount);
    assertNotNull(createdTransaction);

    MongoSession session = null;
    try {
      // When - Use session to delete transaction and update account
      session = sessionManager.createSession();

      transactionRepository.deleteById(session, transaction.id().value());

      var updatedAccount = createdAccount.updateBalance(new BigDecimal("1050"));
      accountRepository.update(updatedAccount);

      session.commit();

      // Then - Verify both changes are persisted atomically
      var retrievedTransaction = transactionRepository.findById(transaction.id().value());
      assertFalse(retrievedTransaction.isPresent(), "Transaction should be deleted");

      var retrievedAccount = accountRepository.findById(account.id().value());
      assertTrue(retrievedAccount.isPresent());
      assertEquals(new BigDecimal("1050"), retrievedAccount.get().balance(),
          "Account balance should be updated");

    } finally {
      if (session != null) {
        session.close();
      }
    }
  }

  /**
   * <b>Given</b> a transaction session with transaction updates<br>
   * <b>When</b> updating a transaction within session and rolling back<br>
   * <b>Then</b> the original transaction should remain unchanged
   */
  @Test
  public void whenUpdatingTransactionWithinSessionAndRollingBackThenOriginalShouldRemain() {
    // Given
    var transaction = createTestTransaction("tx-update-1", "user@test.com", "account-1", null);

    // Create transaction first
    var createdTransaction = transactionRepository.create(transaction);
    assertNotNull(createdTransaction);
    var originalTitle = createdTransaction.title();

    MongoSession session = null;
    try {
      // When - Use session to update transaction
      session = sessionManager.createSession();

      var updatedTransaction = Transaction.builder()
          .id(createdTransaction.id())
          .user(createdTransaction.user())
          .title("UPDATED TITLE")
          .description(createdTransaction.description())
          .date(createdTransaction.date())
          .time(createdTransaction.time())
          .source(createdTransaction.source())
          .destination(createdTransaction.destination())
          .loan(createdTransaction.loan())
          .amount(createdTransaction.amount())
          .currency(createdTransaction.currency())
          .category(createdTransaction.category())
          .type(createdTransaction.type())
          .status(createdTransaction.status())
          .createdAt(createdTransaction.createdAt())
          .updatedAt(LocalDateTime.now())
          .createdBy(createdTransaction.createdBy())
          .updatedBy(createdTransaction.updatedBy())
          .build();

      transactionRepository.update(session, updatedTransaction);

      // Rollback the transaction
      session.rollback();

      // Then - Verify original transaction remains unchanged
      var retrievedTransaction = transactionRepository.findById(transaction.id().value());
      assertTrue(retrievedTransaction.isPresent());
      assertEquals(originalTitle, retrievedTransaction.get().title(),
          "Transaction title should remain unchanged after rollback");

    } finally {
      if (session != null) {
        session.close();
      }
    }
  }

  private Account createTestAccount(String accountId, String userId, BigDecimal balance) {
    return Account.builder()
        .id(Id.of(accountId))
        .user(Id.of(userId))
        .name("Test Account")
        .type(Type.BANK)
        .themeColor("#FF0000")
        .balance(balance)
        .status(EntityStatus.ACTIVE)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .createdBy(userId)
        .updatedBy(userId)
        .build();
  }

  private Transaction createTestTransaction(String transactionId, String userId,
      String sourceAccountId, String destinationAccountId) {
    return Transaction.builder()
        .id(Id.of(transactionId))
        .user(Id.of(userId))
        .title("Test Transaction")
        .description("Test Description")
        .date(LocalDate.now())
        .time(LocalTime.now())
        .source(sourceAccountId != null ? Id.of(sourceAccountId) : null)
        .destination(destinationAccountId != null ? Id.of(destinationAccountId) : null)
        .loan(null)
        .amount(50L)
        .currency("IDR")
        .category(Transaction.Category.OTHER)
        .type(Transaction.Type.DEBIT)
        .status(EntityStatus.ACTIVE)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .createdBy(userId)
        .updatedBy(userId)
        .build();
  }
}
