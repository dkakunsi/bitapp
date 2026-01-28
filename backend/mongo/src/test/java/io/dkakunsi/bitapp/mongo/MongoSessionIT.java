package io.dkakunsi.bitapp.mongo;

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
import io.dkakunsi.bitapp.test.MongoServer;
import io.dkakunsi.bitapp.transaction.entity.Transaction;
import io.dkakunsi.bitapp.transaction.repository.MongoTransactionRepository;

/**
 * Integration tests to verify that MongoDB transaction sessions work correctly
 * and properly rollback when errors occur.
 */
public class MongoSessionIT {

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
   * <b>Given</b> an existing account and transaction<br>
   * <b>When</b> account is deleted and transaction is deleted within session<br>
   * <b>Then</b> changes should be committed atomically
   */
  @Test
  public void atomicChangesShouldWork() {
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
      // When - Use session to delete account
      session = sessionManager.createSession();

      accountRepository.deleteById(session, account.id().value());
      transactionRepository.deleteById(session, transaction.id().value());

      session.commit();

      // Then - Verify both changes are persisted atomically
      var retrievedTransaction = transactionRepository.findById(transaction.id().value());
      assertFalse(retrievedTransaction.isPresent(), "Transaction should be deleted");

      var retrievedAccount = accountRepository.findById(account.id().value());
      assertFalse(retrievedAccount.isPresent(), "Account should be deleted");
    } finally {
      if (session != null) {
        session.close();
      }
    }
  }

  /**
   * <b>Given</b> an existing account and transaction<br>
   * <b>When</b> account is deleted and transaction is deleted within session<br>
   * <b>Then</b> changes should be rolled back on error
   */
  @Test
  public void atomicChangesShouldBeRolledBackOnError() {
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
      // When - Use session to delete account
      session = sessionManager.createSession();

      accountRepository.deleteById(session, account.id().value());
      transactionRepository.deleteById(session, transaction.id().value());

      // Then - Verify both changes are persisted atomically
      var retrievedTransaction = transactionRepository.findById(transaction.id().value());
      assertTrue(retrievedTransaction.isPresent(), "Transaction should exist");

      var retrievedAccount = accountRepository.findById(account.id().value());
      assertTrue(retrievedAccount.isPresent(), "Account should exist");
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
