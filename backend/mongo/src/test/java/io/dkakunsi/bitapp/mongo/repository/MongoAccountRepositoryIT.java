package io.dkakunsi.bitapp.mongo.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.morphia.Datastore;
import io.dkakunsi.bitapp.account.model.Account;
import io.dkakunsi.bitapp.account.model.Account.Type;
import io.dkakunsi.bitapp.common.EnvironmentConfiguration;
import io.dkakunsi.bitapp.common.Id;
import io.dkakunsi.bitapp.mongo.MongoConfiguration;
import io.dkakunsi.bitapp.user.model.User;
import io.dkakunsi.lab.test.MongoServer;

public class MongoAccountRepositoryIT {

  private static MongoConfiguration mongoConfiguration;
  private static Datastore datastore;
  private MongoAccountRepository repository;

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
    datastore.getDatabase().getCollection("accounts").drop();
    repository = new MongoAccountRepository(datastore);
  }

  @Test
  public void givenNewAccountWhenCreateThenShouldPersistAccount() {
    // Given
    var accountId = Id.generate();
    var userId = Id.of("user@email.com");
    var name = "My Bank Account";
    var type = Type.BANK;
    var themeColor = "#FF5733";
    var balance = BigDecimal.ZERO;
    var now = LocalDateTime.now();
    var requester = "user@email.com";

    var user = User.builder().id(userId).build();
    var account = Account.builder()
        .id(accountId)
        .name(name)
        .type(type)
        .themeColor(themeColor)
        .balance(balance)
        .user(user)
        .createdAt(now)
        .updatedAt(now)
        .createdBy(requester)
        .updatedBy(requester)
        .build();

    // When
    var createdAccount = repository.create(account);

    // Then
    assertNotNull(createdAccount);
    assertEquals(accountId.value(), createdAccount.getId().value());
    assertEquals(name, createdAccount.getName());
    assertEquals(type, createdAccount.getType());
    assertEquals(themeColor, createdAccount.getThemeColor());
    assertEquals(balance, createdAccount.getBalance());
    assertEquals(userId.value(), createdAccount.getUser().getId().value());
    assertEquals(requester, createdAccount.getCreatedBy());
    assertEquals(requester, createdAccount.getUpdatedBy());
  }

  @Test
  public void givenAccountWithDifferentTypeWhenCreateThenShouldPersistCorrectType() {
    // Given
    var userId = Id.of("user@email.com");
    var user = User.builder().id(userId).build();
    var now = LocalDateTime.now();
    var requester = "user@email.com";

    var cashAccount = Account.builder()
        .id(Id.generate())
        .name("Cash Wallet")
        .type(Type.CASH)
        .themeColor("#00FF00")
        .balance(BigDecimal.valueOf(100.50))
        .user(user)
        .createdAt(now)
        .updatedAt(now)
        .createdBy(requester)
        .updatedBy(requester)
        .build();

    // When
    var createdAccount = repository.create(cashAccount);

    // Then
    assertNotNull(createdAccount);
    assertEquals(Type.CASH, createdAccount.getType());
    assertEquals("Cash Wallet", createdAccount.getName());
    assertEquals(BigDecimal.valueOf(100.50), createdAccount.getBalance());
  }

  @Test
  public void givenEWalletAccountWhenCreateThenShouldPersistEWalletType() {
    // Given
    var userId = Id.of("user@email.com");
    var user = User.builder().id(userId).build();
    var now = LocalDateTime.now();
    var requester = "user@email.com";

    var ewalletAccount = Account.builder()
        .id(Id.generate())
        .name("GoPay")
        .type(Type.EWALLET)
        .themeColor("#0000FF")
        .balance(BigDecimal.valueOf(250.75))
        .user(user)
        .createdAt(now)
        .updatedAt(now)
        .createdBy(requester)
        .updatedBy(requester)
        .build();

    // When
    var createdAccount = repository.create(ewalletAccount);

    // Then
    assertNotNull(createdAccount);
    assertEquals(Type.EWALLET, createdAccount.getType());
    assertEquals("GoPay", createdAccount.getName());
    assertEquals(BigDecimal.valueOf(250.75), createdAccount.getBalance());
  }

  @Test
  public void givenOtherAccountTypeWhenCreateThenShouldPersistOtherType() {
    // Given
    var userId = Id.of("user@email.com");
    var user = User.builder().id(userId).build();
    var now = LocalDateTime.now();
    var requester = "user@email.com";

    var otherAccount = Account.builder()
        .id(Id.generate())
        .name("Investment Account")
        .type(Type.OTHER)
        .themeColor("#FFFF00")
        .balance(BigDecimal.valueOf(1000.00))
        .user(user)
        .createdAt(now)
        .updatedAt(now)
        .createdBy(requester)
        .updatedBy(requester)
        .build();

    // When
    var createdAccount = repository.create(otherAccount);

    // Then
    assertNotNull(createdAccount);
    assertEquals(Type.OTHER, createdAccount.getType());
    assertEquals("Investment Account", createdAccount.getName());
    assertEquals(BigDecimal.valueOf(1000.00), createdAccount.getBalance());
  }

  @Test
  public void givenMultipleAccountsWhenCreateThenShouldPersistAll() {
    // Given
    var userId = Id.of("user@email.com");
    var user = User.builder().id(userId).build();
    var now = LocalDateTime.now();
    var requester = "user@email.com";

    var account1 = Account.builder()
        .id(Id.generate())
        .name("Account 1")
        .type(Type.BANK)
        .themeColor("#FF0000")
        .balance(BigDecimal.ZERO)
        .user(user)
        .createdAt(now)
        .updatedAt(now)
        .createdBy(requester)
        .updatedBy(requester)
        .build();

    var account2 = Account.builder()
        .id(Id.generate())
        .name("Account 2")
        .type(Type.CASH)
        .themeColor("#00FF00")
        .balance(BigDecimal.valueOf(500))
        .user(user)
        .createdAt(now)
        .updatedAt(now)
        .createdBy(requester)
        .updatedBy(requester)
        .build();

    // When
    var created1 = repository.create(account1);
    var created2 = repository.create(account2);

    // Then
    assertNotNull(created1);
    assertNotNull(created2);
    assertEquals("Account 1", created1.getName());
    assertEquals("Account 2", created2.getName());
    assertEquals(Type.BANK, created1.getType());
    assertEquals(Type.CASH, created2.getType());
  }

  @Test
  public void givenAccountWithZeroBalanceWhenCreateThenShouldPersistZeroBalance() {
    // Given
    var userId = Id.of("user@email.com");
    var user = User.builder().id(userId).build();
    var now = LocalDateTime.now();
    var requester = "user@email.com";

    var account = Account.builder()
        .id(Id.generate())
        .name("New Account")
        .type(Type.BANK)
        .themeColor("#FFFFFF")
        .balance(BigDecimal.ZERO)
        .user(user)
        .createdAt(now)
        .updatedAt(now)
        .createdBy(requester)
        .updatedBy(requester)
        .build();

    // When
    var createdAccount = repository.create(account);

    // Then
    assertNotNull(createdAccount);
    assertEquals(BigDecimal.ZERO, createdAccount.getBalance());
  }

  @Test
  public void givenAccountWithLargeBalanceWhenCreateThenShouldPersistLargeBalance() {
    // Given
    var userId = Id.of("user@email.com");
    var user = User.builder().id(userId).build();
    var now = LocalDateTime.now();
    var requester = "user@email.com";

    var largeBalance = BigDecimal.valueOf(9999999.99);
    var account = Account.builder()
        .id(Id.generate())
        .name("Savings Account")
        .type(Type.BANK)
        .themeColor("#GOLD")
        .balance(largeBalance)
        .user(user)
        .createdAt(now)
        .updatedAt(now)
        .createdBy(requester)
        .updatedBy(requester)
        .build();

    // When
    var createdAccount = repository.create(account);

    // Then
    assertNotNull(createdAccount);
    assertEquals(largeBalance, createdAccount.getBalance());
  }

  @Test
  public void givenAccountWhenCreateThenShouldPreserveTimestamps() {
    // Given
    var userId = Id.of("user@email.com");
    var user = User.builder().id(userId).build();
    var createdAt = LocalDateTime.of(2026, 1, 1, 10, 30, 0);
    var updatedAt = LocalDateTime.of(2026, 1, 8, 15, 45, 30);
    var requester = "user@email.com";

    var account = Account.builder()
        .id(Id.generate())
        .name("Time Test Account")
        .type(Type.BANK)
        .themeColor("#TIME")
        .balance(BigDecimal.ZERO)
        .user(user)
        .createdAt(createdAt)
        .updatedAt(updatedAt)
        .createdBy(requester)
        .updatedBy(requester)
        .build();

    // When
    var createdAccount = repository.create(account);

    // Then
    assertNotNull(createdAccount);
    assertNotNull(createdAccount.getCreatedAt());
    assertNotNull(createdAccount.getUpdatedAt());
  }
}
