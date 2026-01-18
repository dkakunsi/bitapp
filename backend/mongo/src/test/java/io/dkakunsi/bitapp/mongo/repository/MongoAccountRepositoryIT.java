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
import io.dkakunsi.bitapp.account.entity.Account;
import io.dkakunsi.bitapp.account.entity.Account.Type;
import io.dkakunsi.bitapp.common.EnvironmentConfiguration;
import io.dkakunsi.bitapp.common.Id;
import io.dkakunsi.bitapp.common.ModelStatus;
import io.dkakunsi.bitapp.mongo.MongoConfiguration;
import io.dkakunsi.bitapp.test.MongoServer;

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

    var account = Account.builder()
        .id(accountId)
        .name(name)
        .type(type)
        .themeColor(themeColor)
        .balance(balance)
        .user(userId)
        .status(ModelStatus.ACTIVE)
        .createdAt(now)
        .updatedAt(now)
        .createdBy(requester)
        .updatedBy(requester)
        .build();

    // When
    var createdAccount = repository.create(account);

    // Then
    assertNotNull(createdAccount);
    assertEquals(accountId.value(), createdAccount.id().value());
    assertEquals(name, createdAccount.name());
    assertEquals(type, createdAccount.type());
    assertEquals(themeColor, createdAccount.themeColor());
    assertEquals(balance, createdAccount.balance());
    assertEquals(userId.value(), createdAccount.user().value());
    assertEquals(requester, createdAccount.createdBy());
    assertEquals(requester, createdAccount.updatedBy());
  }

  @Test
  public void givenAccountWithDifferentTypeWhenCreateThenShouldPersistCorrectType() {
    // Given
    var userId = Id.of("user@email.com");
    var now = LocalDateTime.now();
    var requester = "user@email.com";

    var cashAccount = Account.builder()
        .id(Id.generate())
        .name("Cash Wallet")
        .type(Type.CASH)
        .themeColor("#00FF00")
        .balance(BigDecimal.valueOf(100.50))
        .user(userId)
        .status(ModelStatus.ACTIVE)
        .createdAt(now)
        .updatedAt(now)
        .createdBy(requester)
        .updatedBy(requester)
        .build();

    // When
    var createdAccount = repository.create(cashAccount);

    // Then
    assertNotNull(createdAccount);
    assertEquals(Type.CASH, createdAccount.type());
    assertEquals("Cash Wallet", createdAccount.name());
    assertEquals(BigDecimal.valueOf(100.50), createdAccount.balance());
  }

  @Test
  public void givenEWalletAccountWhenCreateThenShouldPersistEWalletType() {
    // Given
    var userId = Id.of("user@email.com");
    var now = LocalDateTime.now();
    var requester = "user@email.com";

    var ewalletAccount = Account.builder()
        .id(Id.generate())
        .name("GoPay")
        .type(Type.EWALLET)
        .themeColor("#0000FF")
        .balance(BigDecimal.valueOf(250.75))
        .user(userId)
        .status(ModelStatus.ACTIVE)
        .createdAt(now)
        .updatedAt(now)
        .createdBy(requester)
        .updatedBy(requester)
        .build();

    // When
    var createdAccount = repository.create(ewalletAccount);

    // Then
    assertNotNull(createdAccount);
    assertEquals(Type.EWALLET, createdAccount.type());
    assertEquals("GoPay", createdAccount.name());
    assertEquals(BigDecimal.valueOf(250.75), createdAccount.balance());
  }

  @Test
  public void givenOtherAccountTypeWhenCreateThenShouldPersistOtherType() {
    // Given
    var userId = Id.of("user@email.com");
    var now = LocalDateTime.now();
    var requester = "user@email.com";

    var otherAccount = Account.builder()
        .id(Id.generate())
        .name("Investment Account")
        .type(Type.OTHER)
        .themeColor("#FFFF00")
        .balance(BigDecimal.valueOf(1000.00))
        .user(userId)
        .status(ModelStatus.ACTIVE)
        .createdAt(now)
        .updatedAt(now)
        .createdBy(requester)
        .updatedBy(requester)
        .build();

    // When
    var createdAccount = repository.create(otherAccount);

    // Then
    assertNotNull(createdAccount);
    assertEquals(Type.OTHER, createdAccount.type());
    assertEquals("Investment Account", createdAccount.name());
    assertEquals(BigDecimal.valueOf(1000.00), createdAccount.balance());
  }

  @Test
  public void givenMultipleAccountsWhenCreateThenShouldPersistAll() {
    // Given
    var userId = Id.of("user@email.com");
    var now = LocalDateTime.now();
    var requester = "user@email.com";

    var account1 = Account.builder()
        .id(Id.generate())
        .name("Account 1")
        .type(Type.BANK)
        .themeColor("#FF0000")
        .balance(BigDecimal.ZERO)
        .user(userId)
        .status(ModelStatus.ACTIVE)
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
        .user(userId)
        .status(ModelStatus.ACTIVE)
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
    assertEquals("Account 1", created1.name());
    assertEquals("Account 2", created2.name());
    assertEquals(Type.BANK, created1.type());
    assertEquals(Type.CASH, created2.type());
  }

  @Test
  public void givenAccountWithZeroBalanceWhenCreateThenShouldPersistZeroBalance() {
    // Given
    var userId = Id.of("user@email.com");
    var now = LocalDateTime.now();
    var requester = "user@email.com";

    var account = Account.builder()
        .id(Id.generate())
        .name("New Account")
        .type(Type.BANK)
        .themeColor("#FFFFFF")
        .balance(BigDecimal.ZERO)
        .user(userId)
        .status(ModelStatus.ACTIVE)
        .createdAt(now)
        .updatedAt(now)
        .createdBy(requester)
        .updatedBy(requester)
        .build();

    // When
    var createdAccount = repository.create(account);

    // Then
    assertNotNull(createdAccount);
    assertEquals(BigDecimal.ZERO, createdAccount.balance());
  }

  @Test
  public void givenAccountWithLargeBalanceWhenCreateThenShouldPersistLargeBalance() {
    // Given
    var userId = Id.of("user@email.com");
    var now = LocalDateTime.now();
    var requester = "user@email.com";

    var largeBalance = BigDecimal.valueOf(9999999.99);
    var account = Account.builder()
        .id(Id.generate())
        .name("Savings Account")
        .type(Type.BANK)
        .themeColor("#GOLD")
        .balance(largeBalance)
        .user(userId)
        .status(ModelStatus.ACTIVE)
        .createdAt(now)
        .updatedAt(now)
        .createdBy(requester)
        .updatedBy(requester)
        .build();

    // When
    var createdAccount = repository.create(account);

    // Then
    assertNotNull(createdAccount);
    assertEquals(largeBalance, createdAccount.balance());
  }

  @Test
  public void givenAccountWhenCreateThenShouldPreserveTimestamps() {
    // Given
    var userId = Id.of("user@email.com");
    var createdAt = LocalDateTime.of(2026, 1, 1, 10, 30, 0);
    var updatedAt = LocalDateTime.of(2026, 1, 8, 15, 45, 30);
    var requester = "user@email.com";

    var account = Account.builder()
        .id(Id.generate())
        .name("Time Test Account")
        .type(Type.BANK)
        .themeColor("#TIME")
        .balance(BigDecimal.ZERO)
        .user(userId)
        .status(ModelStatus.ACTIVE)
        .createdAt(createdAt)
        .updatedAt(updatedAt)
        .createdBy(requester)
        .updatedBy(requester)
        .build();

    // When
    var createdAccount = repository.create(account);

    // Then
    assertNotNull(createdAccount);
    assertNotNull(createdAccount.createdAt());
    assertNotNull(createdAccount.updatedAt());
  }

  @Test
  public void givenExistingAccountWhenUpdateThenShouldUpdateFields() {
    // Given
    var userId = Id.of("user@email.com");
    var now = LocalDateTime.now();
    var requester = "user@email.com";

    var account = Account.builder()
        .id(Id.generate())
        .name("Original Name")
        .type(Type.BANK)
        .themeColor("#FF0000")
        .balance(BigDecimal.valueOf(100))
        .user(userId)
        .status(ModelStatus.ACTIVE)
        .createdAt(now)
        .updatedAt(now)
        .createdBy(requester)
        .updatedBy(requester)
        .build();

    repository.create(account);

    // When
    var updatedAccount = Account.builder()
        .id(account.id())
        .name("Updated Name")
        .type(Type.CASH)
        .themeColor("#00FF00")
        .balance(account.balance())
        .user(userId)
        .status(ModelStatus.ACTIVE)
        .createdAt(account.createdAt())
        .updatedAt(LocalDateTime.now())
        .createdBy(requester)
        .updatedBy("admin@email.com")
        .build();

    var result = repository.update(updatedAccount);

    // Then
    assertNotNull(result);
    assertEquals("Updated Name", result.name());
    assertEquals(Type.CASH, result.type());
    assertEquals("#00FF00", result.themeColor());
    assertEquals("admin@email.com", result.updatedBy());
  }

  @Test
  public void givenNonExistingAccountWhenUpdateThenShouldReturnNull() {
    // Given
    var userId = Id.of("user@email.com");
    var now = LocalDateTime.now();
    var requester = "user@email.com";

    var nonExistingAccount = Account.builder()
        .id(Id.generate())
        .name("Non-existing Account")
        .type(Type.BANK)
        .themeColor("#FF0000")
        .balance(BigDecimal.valueOf(500))
        .user(userId)
        .status(ModelStatus.ACTIVE)
        .createdAt(now)
        .updatedAt(now)
        .createdBy(requester)
        .updatedBy(requester)
        .build();

    // When
    var result = repository.update(nonExistingAccount);

    // Then
    assertNotNull(result);
    assertEquals(nonExistingAccount.id().value(), result.id().value());
    assertEquals(nonExistingAccount.name(), result.name());
    assertEquals(nonExistingAccount.type(), result.type());
    assertEquals(nonExistingAccount.themeColor(), result.themeColor());
    assertEquals(nonExistingAccount.balance().toBigInteger(), result.balance().toBigInteger());
    assertEquals(nonExistingAccount.user().value(), result.user().value());
    assertEquals(nonExistingAccount.createdBy(), result.createdBy());
    assertEquals(nonExistingAccount.updatedBy(), result.updatedBy());
  }

  @Test
  public void givenExistingAccountIdWhenFindByIdThenShouldReturnAccount() {
    // Given
    var userId = Id.of("user@email.com");
    var now = LocalDateTime.now();
    var requester = "user@email.com";

    var account = Account.builder()
        .id(Id.generate())
        .name("Find Me")
        .type(Type.EWALLET)
        .themeColor("#0000FF")
        .balance(BigDecimal.valueOf(500))
        .user(userId)
        .status(ModelStatus.ACTIVE)
        .createdAt(now)
        .updatedAt(now)
        .createdBy(requester)
        .updatedBy(requester)
        .build();

    repository.create(account);

    // When
    var result = repository.findById(account.id().value());

    // Then
    assertNotNull(result);
    assertEquals(true, result.isPresent());
    assertEquals(account.id().value(), result.get().id().value());
    assertEquals("Find Me", result.get().name());
    assertEquals(Type.EWALLET, result.get().type());
  }

  @Test
  public void givenNonExistingAccountIdWhenFindByIdThenShouldReturnEmpty() {
    // Given
    var nonExistingId = "non-existing-account-id";

    // When
    var result = repository.findById(nonExistingId);

    // Then
    assertNotNull(result);
    assertEquals(false, result.isPresent());
  }

  @Test
  public void givenUserIdWithAccountsWhenFindByUserIdThenShouldReturnAllUserAccounts() {
    // Given
    var userId = Id.of("user@email.com");
    var now = LocalDateTime.now();
    var requester = "user@email.com";

    var account1 = Account.builder()
        .id(Id.generate())
        .name("User Account 1")
        .type(Type.BANK)
        .themeColor("#FF0000")
        .balance(BigDecimal.valueOf(100))
        .user(userId)
        .status(ModelStatus.ACTIVE)
        .createdAt(now)
        .updatedAt(now)
        .createdBy(requester)
        .updatedBy(requester)
        .build();

    var account2 = Account.builder()
        .id(Id.generate())
        .name("User Account 2")
        .type(Type.CASH)
        .themeColor("#00FF00")
        .balance(BigDecimal.valueOf(200))
        .user(userId)
        .status(ModelStatus.ACTIVE)
        .createdAt(now)
        .updatedAt(now)
        .createdBy(requester)
        .updatedBy(requester)
        .build();

    var account3 = Account.builder()
        .id(Id.generate())
        .name("User Account 3")
        .type(Type.EWALLET)
        .themeColor("#0000FF")
        .balance(BigDecimal.valueOf(300))
        .user(userId)
        .status(ModelStatus.ACTIVE)
        .createdAt(now)
        .updatedAt(now)
        .createdBy(requester)
        .updatedBy(requester)
        .build();

    repository.create(account1);
    repository.create(account2);
    repository.create(account3);

    // When
    var result = repository.findByUserId(userId.value());

    // Then
    assertNotNull(result);
    assertEquals(3, result.size());
    assertEquals(true, result.stream().anyMatch(a -> a.name().equals("User Account 1")));
    assertEquals(true, result.stream().anyMatch(a -> a.name().equals("User Account 2")));
    assertEquals(true, result.stream().anyMatch(a -> a.name().equals("User Account 3")));
  }

  @Test
  public void givenUserIdWithNoAccountsWhenFindByUserIdThenShouldReturnEmptyList() {
    // Given
    var userId = "user-with-no-accounts@email.com";

    // When
    var result = repository.findByUserId(userId);

    // Then
    assertNotNull(result);
    assertEquals(0, result.size());
  }

  @Test
  public void givenMultipleUsersWhenFindByUserIdThenShouldReturnOnlySpecificUserAccounts() {
    // Given
    var userId1 = Id.of("user1@email.com");
    var userId2 = Id.of("user2@email.com");
    var now = LocalDateTime.now();

    var account1 = Account.builder()
        .id(Id.generate())
        .name("User1 Account")
        .type(Type.BANK)
        .themeColor("#FF0000")
        .balance(BigDecimal.valueOf(100))
        .user(userId1)
        .status(ModelStatus.ACTIVE)
        .createdAt(now)
        .updatedAt(now)
        .createdBy("user1@email.com")
        .updatedBy("user1@email.com")
        .build();

    var account2 = Account.builder()
        .id(Id.generate())
        .name("User2 Account")
        .type(Type.CASH)
        .themeColor("#00FF00")
        .balance(BigDecimal.valueOf(200))
        .user(userId2)
        .status(ModelStatus.ACTIVE)
        .createdAt(now)
        .updatedAt(now)
        .createdBy("user2@email.com")
        .updatedBy("user2@email.com")
        .build();

    repository.create(account1);
    repository.create(account2);

    // When
    var result = repository.findByUserId(userId1.value());

    // Then
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("User1 Account", result.get(0).name());
    assertEquals(userId1.value(), result.get(0).user().value());
  }

  @Test
  public void givenAccountWhenUpdateMultipleTimesThenShouldReflectLatestChanges() {
    // Given
    var userId = Id.of("user@email.com");
    var now = LocalDateTime.now();
    var requester = "user@email.com";

    var account = Account.builder()
        .id(Id.generate())
        .name("Original")
        .type(Type.BANK)
        .themeColor("#000000")
        .balance(BigDecimal.ZERO)
        .user(userId)
        .status(ModelStatus.ACTIVE)
        .createdAt(now)
        .updatedAt(now)
        .createdBy(requester)
        .updatedBy(requester)
        .build();

    repository.create(account);

    // When - First update
    var firstUpdate = Account.builder()
        .id(account.id())
        .name("First Update")
        .type(Type.CASH)
        .themeColor("#111111")
        .balance(account.balance())
        .user(userId)
        .status(ModelStatus.ACTIVE)
        .createdAt(account.createdAt())
        .updatedAt(LocalDateTime.now())
        .createdBy(requester)
        .updatedBy("updater1@email.com")
        .build();

    repository.update(firstUpdate);

    // When - Second update
    var secondUpdate = Account.builder()
        .id(account.id())
        .name("Second Update")
        .type(Type.EWALLET)
        .themeColor("#222222")
        .balance(account.balance())
        .user(userId)
        .status(ModelStatus.ACTIVE)
        .createdAt(account.createdAt())
        .updatedAt(LocalDateTime.now())
        .createdBy(requester)
        .updatedBy("updater2@email.com")
        .build();

    var result = repository.update(secondUpdate);

    // Then
    assertNotNull(result);
    assertEquals("Second Update", result.name());
    assertEquals(Type.EWALLET, result.type());
    assertEquals("#222222", result.themeColor());
    assertEquals("updater2@email.com", result.updatedBy());
  }
}
