package io.dkakunsi.bitapp.mongo.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
import io.dkakunsi.bitapp.common.Id;
import io.dkakunsi.bitapp.common.ModelStatus;
import io.dkakunsi.bitapp.loan.entity.Loan;
import io.dkakunsi.bitapp.mongo.MongoConfiguration;
import io.dkakunsi.bitapp.test.MongoServer;

public class MongoLoanRepositoryIT {

  private static MongoConfiguration mongoConfiguration;
  private static Datastore datastore;
  private MongoLoanRepository repository;

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
    datastore.getDatabase().getCollection("loans").drop();
    repository = new MongoLoanRepository(datastore);
  }

  @Test
  public void givenValidBorrowLoanWhenCreateThenShouldPersistLoan() {
    // Given
    var loanId = Id.generate();
    var userId = Id.of("user@email.com");
    var loan = Loan.builder()
        .id(loanId)
        .user(userId)
        .type(Loan.Type.BORROW)
        .date(LocalDate.of(2026, 1, 15))
        .time(LocalTime.of(10, 30))
        .partyName("John Doe")
        .title("Personal Loan")
        .description("Loan for personal expenses")
        .amount(BigDecimal.valueOf(10000))
        .remainingAmount(BigDecimal.valueOf(10000))
        .currency(Currency.getInstance("IDR"))
        .interestRate(5.5)
        .status(ModelStatus.ACTIVE)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .createdBy("user@email.com")
        .updatedBy("user@email.com")
        .build();

    // When
    var createdLoan = repository.create(loan);

    // Then
    assertNotNull(createdLoan);
    assertEquals(loanId.value(), createdLoan.id().value());
    assertEquals(userId.value(), createdLoan.user().value());
    assertEquals(Loan.Type.BORROW, createdLoan.type());
    assertEquals(LocalDate.of(2026, 1, 15), createdLoan.date());
    assertEquals(LocalTime.of(10, 30), createdLoan.time());
    assertEquals("John Doe", createdLoan.partyName());
    assertEquals("Personal Loan", createdLoan.title());
    assertEquals("Loan for personal expenses", createdLoan.description());
    assertEquals(BigDecimal.valueOf(10000), createdLoan.amount());
    assertEquals(BigDecimal.valueOf(10000), createdLoan.remainingAmount());
    assertEquals(Currency.getInstance("IDR"), createdLoan.currency());
    assertEquals(5.5, createdLoan.interestRate());
    assertEquals(ModelStatus.ACTIVE, createdLoan.status());
    assertEquals("user@email.com", createdLoan.createdBy());
    assertEquals("user@email.com", createdLoan.updatedBy());
  }

  @Test
  public void givenValidLendLoanWhenCreateThenShouldPersistLoan() {
    // Given
    var loanId = Id.generate();
    var userId = Id.of("lender@email.com");
    var loan = Loan.builder()
        .id(loanId)
        .user(userId)
        .type(Loan.Type.LEND)
        .date(LocalDate.of(2026, 1, 17))
        .time(LocalTime.of(14, 0))
        .partyName("Jane Smith")
        .title("Business Loan")
        .description("Loan for business expansion")
        .amount(BigDecimal.valueOf(50000))
        .remainingAmount(BigDecimal.valueOf(45000))
        .currency(Currency.getInstance("USD"))
        .interestRate(7.5)
        .status(ModelStatus.ACTIVE)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .createdBy("lender@email.com")
        .updatedBy("lender@email.com")
        .build();

    // When
    var createdLoan = repository.create(loan);

    // Then
    assertNotNull(createdLoan);
    assertEquals(Loan.Type.LEND, createdLoan.type());
    assertEquals("Jane Smith", createdLoan.partyName());
    assertEquals("Business Loan", createdLoan.title());
    assertEquals(BigDecimal.valueOf(50000), createdLoan.amount());
    assertEquals(BigDecimal.valueOf(45000), createdLoan.remainingAmount());
    assertEquals(Currency.getInstance("USD"), createdLoan.currency());
    assertEquals(7.5, createdLoan.interestRate());
  }

  @Test
  public void givenLoanWithZeroInterestRateWhenCreateThenShouldPersistZeroRate() {
    // Given
    var userId = Id.of("user@email.com");
    var loan = Loan.builder()
        .id(Id.generate())
        .user(userId)
        .type(Loan.Type.BORROW)
        .date(LocalDate.now())
        .time(LocalTime.now())
        .partyName("No Interest Party")
        .title("Interest Free Loan")
        .description("Loan without interest")
        .amount(BigDecimal.valueOf(5000))
        .remainingAmount(BigDecimal.valueOf(5000))
        .currency(Currency.getInstance("IDR"))
        .interestRate(0.0)
        .status(ModelStatus.ACTIVE)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .createdBy("user@email.com")
        .updatedBy("user@email.com")
        .build();

    // When
    var createdLoan = repository.create(loan);

    // Then
    assertNotNull(createdLoan);
    assertEquals(0.0, createdLoan.interestRate());
    assertEquals(BigDecimal.valueOf(5000), createdLoan.amount());
  }

  @Test
  public void givenLoanWithHighInterestRateWhenCreateThenShouldPersistHighRate() {
    // Given
    var userId = Id.of("borrower@email.com");
    var loan = Loan.builder()
        .id(Id.generate())
        .user(userId)
        .type(Loan.Type.BORROW)
        .date(LocalDate.now())
        .time(LocalTime.now())
        .partyName("High Interest Lender")
        .title("High Interest Loan")
        .description("Loan with high interest rate")
        .amount(BigDecimal.valueOf(3000))
        .remainingAmount(BigDecimal.valueOf(3000))
        .currency(Currency.getInstance("USD"))
        .interestRate(25.5)
        .status(ModelStatus.ACTIVE)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .createdBy("borrower@email.com")
        .updatedBy("borrower@email.com")
        .build();

    // When
    var createdLoan = repository.create(loan);

    // Then
    assertNotNull(createdLoan);
    assertEquals(25.5, createdLoan.interestRate());
    assertEquals(BigDecimal.valueOf(3000), createdLoan.amount());
  }

  @Test
  public void givenLoanWithPartialPaymentWhenCreateThenShouldPersistRemainingAmount() {
    // Given
    var userId = Id.of("user@email.com");
    var totalAmount = BigDecimal.valueOf(10000);
    var remainingAmount = BigDecimal.valueOf(7500);
    var loan = Loan.builder()
        .id(Id.generate())
        .user(userId)
        .type(Loan.Type.LEND)
        .date(LocalDate.now())
        .time(LocalTime.now())
        .partyName("Partial Payer")
        .title("Partially Paid Loan")
        .description("Loan with partial payment")
        .amount(totalAmount)
        .remainingAmount(remainingAmount)
        .currency(Currency.getInstance("IDR"))
        .interestRate(5.0)
        .status(ModelStatus.ACTIVE)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .createdBy("lender@email.com")
        .updatedBy("lender@email.com")
        .build();

    // When
    var createdLoan = repository.create(loan);

    // Then
    assertNotNull(createdLoan);
    assertEquals(totalAmount, createdLoan.amount());
    assertEquals(remainingAmount, createdLoan.remainingAmount());
  }

  @Test
  public void givenLoanWithNullDescriptionWhenCreateThenShouldPersist() {
    // Given
    var userId = Id.of("user@email.com");
    var loan = Loan.builder()
        .id(Id.generate())
        .user(userId)
        .type(Loan.Type.BORROW)
        .date(LocalDate.now())
        .time(LocalTime.now())
        .partyName("Party Without Description")
        .title("Loan Without Description")
        .description(null)
        .amount(BigDecimal.valueOf(2000))
        .remainingAmount(BigDecimal.valueOf(2000))
        .currency(Currency.getInstance("IDR"))
        .interestRate(3.0)
        .status(ModelStatus.ACTIVE)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .createdBy("user@email.com")
        .updatedBy("user@email.com")
        .build();

    // When
    var createdLoan = repository.create(loan);

    // Then
    assertNotNull(createdLoan);
    assertEquals(null, createdLoan.description());
    assertEquals("Loan Without Description", createdLoan.title());
  }

  @Test
  public void givenMultipleLoanTypesWhenCreateThenShouldPersistAll() {
    // Given
    var userId = Id.of("user@email.com");
    var borrowLoan = Loan.builder()
        .id(Id.generate())
        .user(userId)
        .type(Loan.Type.BORROW)
        .date(LocalDate.now())
        .time(LocalTime.now())
        .partyName("Borrow Party")
        .title("Borrow Loan")
        .description("First loan")
        .amount(BigDecimal.valueOf(1000))
        .remainingAmount(BigDecimal.valueOf(1000))
        .currency(Currency.getInstance("IDR"))
        .interestRate(2.0)
        .status(ModelStatus.ACTIVE)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .createdBy("user@email.com")
        .updatedBy("user@email.com")
        .build();

    var lendLoan = Loan.builder()
        .id(Id.generate())
        .user(userId)
        .type(Loan.Type.LEND)
        .date(LocalDate.now())
        .time(LocalTime.now())
        .partyName("Lend Party")
        .title("Lend Loan")
        .description("Second loan")
        .amount(BigDecimal.valueOf(2000))
        .remainingAmount(BigDecimal.valueOf(2000))
        .currency(Currency.getInstance("USD"))
        .interestRate(4.0)
        .status(ModelStatus.ACTIVE)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .createdBy("user@email.com")
        .updatedBy("user@email.com")
        .build();

    // When
    var createdBorrow = repository.create(borrowLoan);
    var createdLend = repository.create(lendLoan);

    // Then
    assertNotNull(createdBorrow);
    assertNotNull(createdLend);
    assertEquals(Loan.Type.BORROW, createdBorrow.type());
    assertEquals(Loan.Type.LEND, createdLend.type());
    assertEquals("Borrow Loan", createdBorrow.title());
    assertEquals("Lend Loan", createdLend.title());
  }

  @Test
  public void givenDifferentCurrenciesWhenCreateThenShouldPersistCorrectCurrency() {
    // Given
    var userId = Id.of("user@email.com");
    var idrLoan = Loan.builder()
        .id(Id.generate())
        .user(userId)
        .type(Loan.Type.BORROW)
        .date(LocalDate.now())
        .time(LocalTime.now())
        .partyName("IDR Party")
        .title("IDR Loan")
        .description("Loan in IDR")
        .amount(BigDecimal.valueOf(10000000))
        .remainingAmount(BigDecimal.valueOf(10000000))
        .currency(Currency.getInstance("IDR"))
        .interestRate(5.0)
        .status(ModelStatus.ACTIVE)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .createdBy("user@email.com")
        .updatedBy("user@email.com")
        .build();

    var usdLoan = Loan.builder()
        .id(Id.generate())
        .user(userId)
        .type(Loan.Type.LEND)
        .date(LocalDate.now())
        .time(LocalTime.now())
        .partyName("USD Party")
        .title("USD Loan")
        .description("Loan in USD")
        .amount(BigDecimal.valueOf(5000))
        .remainingAmount(BigDecimal.valueOf(5000))
        .currency(Currency.getInstance("USD"))
        .interestRate(3.5)
        .status(ModelStatus.ACTIVE)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .createdBy("user@email.com")
        .updatedBy("user@email.com")
        .build();

    var eurLoan = Loan.builder()
        .id(Id.generate())
        .user(userId)
        .type(Loan.Type.BORROW)
        .date(LocalDate.now())
        .time(LocalTime.now())
        .partyName("EUR Party")
        .title("EUR Loan")
        .description("Loan in EUR")
        .amount(BigDecimal.valueOf(7500))
        .remainingAmount(BigDecimal.valueOf(7500))
        .currency(Currency.getInstance("EUR"))
        .interestRate(4.0)
        .status(ModelStatus.ACTIVE)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .createdBy("user@email.com")
        .updatedBy("user@email.com")
        .build();

    // When
    var createdIdr = repository.create(idrLoan);
    var createdUsd = repository.create(usdLoan);
    var createdEur = repository.create(eurLoan);

    // Then
    assertNotNull(createdIdr);
    assertNotNull(createdUsd);
    assertNotNull(createdEur);
    assertEquals(Currency.getInstance("IDR"), createdIdr.currency());
    assertEquals(Currency.getInstance("USD"), createdUsd.currency());
    assertEquals(Currency.getInstance("EUR"), createdEur.currency());
  }

  @Test
  public void givenLoanWithSpecificTimestampsWhenCreateThenShouldPreserveTimestamps() {
    // Given
    var createdAt = LocalDateTime.of(2026, 1, 1, 10, 0, 0);
    var updatedAt = LocalDateTime.of(2026, 1, 15, 14, 30, 0);
    var userId = Id.of("user@email.com");
    var loan = Loan.builder()
        .id(Id.generate())
        .user(userId)
        .type(Loan.Type.BORROW)
        .date(LocalDate.of(2026, 1, 10))
        .time(LocalTime.of(12, 0))
        .partyName("Timestamp Party")
        .title("Timestamp Loan")
        .description("Loan with specific timestamps")
        .amount(BigDecimal.valueOf(5000))
        .remainingAmount(BigDecimal.valueOf(5000))
        .currency(Currency.getInstance("IDR"))
        .interestRate(5.0)
        .status(ModelStatus.ACTIVE)
        .createdAt(createdAt)
        .updatedAt(updatedAt)
        .createdBy("creator")
        .updatedBy("updater")
        .build();

    // When
    var createdLoan = repository.create(loan);

    // Then
    assertNotNull(createdLoan);
    assertNotNull(createdLoan.createdAt());
    assertNotNull(createdLoan.updatedAt());
    assertEquals("creator", createdLoan.createdBy());
    assertEquals("updater", createdLoan.updatedBy());
  }

  @Test
  public void givenLoanWithLargeAmountWhenCreateThenShouldPersistLargeAmount() {
    // Given
    var userId = Id.of("user@email.com");
    var largeAmount = BigDecimal.valueOf(999999999.99);
    var loan = Loan.builder()
        .id(Id.generate())
        .user(userId)
        .type(Loan.Type.LEND)
        .date(LocalDate.now())
        .time(LocalTime.now())
        .partyName("Large Amount Party")
        .title("Large Amount Loan")
        .description("Loan with large amount")
        .amount(largeAmount)
        .remainingAmount(largeAmount)
        .currency(Currency.getInstance("USD"))
        .interestRate(2.5)
        .status(ModelStatus.ACTIVE)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .createdBy("user@email.com")
        .updatedBy("user@email.com")
        .build();

    // When
    var createdLoan = repository.create(loan);

    // Then
    assertNotNull(createdLoan);
    assertEquals(largeAmount, createdLoan.amount());
    assertEquals(largeAmount, createdLoan.remainingAmount());
  }

  @Test
  public void givenLoanWithSmallAmountWhenCreateThenShouldPersistSmallAmount() {
    // Given
    var userId = Id.of("user@email.com");
    var smallAmount = BigDecimal.valueOf(0.01);
    var loan = Loan.builder()
        .id(Id.generate())
        .user(userId)
        .type(Loan.Type.BORROW)
        .date(LocalDate.now())
        .time(LocalTime.now())
        .partyName("Small Amount Party")
        .title("Small Amount Loan")
        .description("Loan with small amount")
        .amount(smallAmount)
        .remainingAmount(smallAmount)
        .currency(Currency.getInstance("USD"))
        .interestRate(1.0)
        .status(ModelStatus.ACTIVE)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .createdBy("user@email.com")
        .updatedBy("user@email.com")
        .build();

    // When
    var createdLoan = repository.create(loan);

    // Then
    assertNotNull(createdLoan);
    assertEquals(smallAmount, createdLoan.amount());
  }

  @Test
  public void givenMultipleLoansForDifferentUsersWhenCreateThenShouldPersistAll() {
    // Given
    var user1 = Id.of("user1@email.com");
    var user2 = Id.of("user2@email.com");

    var loan1 = Loan.builder()
        .id(Id.generate())
        .user(user1)
        .type(Loan.Type.BORROW)
        .date(LocalDate.now())
        .time(LocalTime.now())
        .partyName("Party 1")
        .title("User1 Loan")
        .description("Loan for user 1")
        .amount(BigDecimal.valueOf(1000))
        .remainingAmount(BigDecimal.valueOf(1000))
        .currency(Currency.getInstance("IDR"))
        .interestRate(3.0)
        .status(ModelStatus.ACTIVE)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .createdBy("user1@email.com")
        .updatedBy("user1@email.com")
        .build();

    var loan2 = Loan.builder()
        .id(Id.generate())
        .user(user2)
        .type(Loan.Type.LEND)
        .date(LocalDate.now())
        .time(LocalTime.now())
        .partyName("Party 2")
        .title("User2 Loan")
        .description("Loan for user 2")
        .amount(BigDecimal.valueOf(2000))
        .remainingAmount(BigDecimal.valueOf(2000))
        .currency(Currency.getInstance("USD"))
        .interestRate(4.0)
        .status(ModelStatus.ACTIVE)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .createdBy("user2@email.com")
        .updatedBy("user2@email.com")
        .build();

    // When
    var created1 = repository.create(loan1);
    var created2 = repository.create(loan2);

    // Then
    assertNotNull(created1);
    assertNotNull(created2);
    assertEquals(user1.value(), created1.user().value());
    assertEquals(user2.value(), created2.user().value());
    assertEquals("User1 Loan", created1.title());
    assertEquals("User2 Loan", created2.title());
  }

  @Test
  public void givenLoanWithSpecialCharactersInFieldsWhenCreateThenShouldPersist() {
    // Given
    var userId = Id.of("user@email.com");
    var loan = Loan.builder()
        .id(Id.generate())
        .user(userId)
        .type(Loan.Type.BORROW)
        .date(LocalDate.now())
        .time(LocalTime.now())
        .partyName("O'Brien & Associates, Inc.")
        .title("Loan with special chars: @#$%")
        .description("Description with special characters: <>&\"'")
        .amount(BigDecimal.valueOf(3000))
        .remainingAmount(BigDecimal.valueOf(3000))
        .currency(Currency.getInstance("IDR"))
        .interestRate(5.5)
        .status(ModelStatus.ACTIVE)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .createdBy("user@email.com")
        .updatedBy("user@email.com")
        .build();

    // When
    var createdLoan = repository.create(loan);

    // Then
    assertNotNull(createdLoan);
    assertEquals("O'Brien & Associates, Inc.", createdLoan.partyName());
    assertEquals("Loan with special chars: @#$%", createdLoan.title());
    assertEquals("Description with special characters: <>&\"'", createdLoan.description());
  }

  @Test
  public void givenUserWithMultipleLoansWhenFindByUserIdThenShouldReturnAllUserLoans() {
    // Given
    var userId = Id.of("user@email.com");
    var requester = "user@email.com";
    var now = LocalDateTime.now();

    var loan1 = Loan.builder()
        .id(Id.generate())
        .user(userId)
        .type(Loan.Type.BORROW)
        .date(LocalDate.of(2024, 6, 15))
        .time(LocalTime.of(14, 30))
        .partyName("Bank ABC")
        .title("Car Loan")
        .description("Loan for purchasing a car")
        .amount(BigDecimal.valueOf(500000000))
        .remainingAmount(BigDecimal.valueOf(500000000))
        .currency(Currency.getInstance("IDR"))
        .interestRate(5.5)
        .status(ModelStatus.ACTIVE)
        .createdAt(now)
        .updatedAt(now)
        .createdBy(requester)
        .updatedBy(requester)
        .build();

    var loan2 = Loan.builder()
        .id(Id.generate())
        .user(userId)
        .type(Loan.Type.LEND)
        .date(LocalDate.of(2024, 6, 20))
        .time(LocalTime.of(10, 0))
        .partyName("John Doe")
        .title("Personal Loan")
        .description("Money lent to friend")
        .amount(BigDecimal.valueOf(10000000))
        .remainingAmount(BigDecimal.valueOf(10000000))
        .currency(Currency.getInstance("IDR"))
        .interestRate(2.0)
        .status(ModelStatus.ACTIVE)
        .createdAt(now)
        .updatedAt(now)
        .createdBy(requester)
        .updatedBy(requester)
        .build();

    repository.create(loan1);
    repository.create(loan2);

    // When
    var loans = repository.findByUserId(userId.value());

    // Then
    assertNotNull(loans);
    assertEquals(2, loans.size());

    // Verify loans belong to correct user
    loans.forEach(loan -> assertEquals(userId.value(), loan.user().value()));
  }

  @Test
  public void givenUserWithNoLoansWhenFindByUserIdThenShouldReturnEmptyList() {
    // Given
    var userId = "nonexistent@email.com";

    // When
    var loans = repository.findByUserId(userId);

    // Then
    assertNotNull(loans);
    assertEquals(0, loans.size());
  }

  @Test
  public void givenMultipleUsersWithLoansWhenFindByUserIdThenShouldReturnOnlyUserLoans() {
    // Given
    var userId1 = Id.of("user1@email.com");
    var userId2 = Id.of("user2@email.com");
    var now = LocalDateTime.now();

    var loan1 = Loan.builder()
        .id(Id.generate())
        .user(userId1)
        .type(Loan.Type.BORROW)
        .date(LocalDate.of(2024, 6, 15))
        .time(LocalTime.of(14, 30))
        .partyName("Bank ABC")
        .title("User 1 Car Loan")
        .description("Loan for user 1")
        .amount(BigDecimal.valueOf(500000000))
        .remainingAmount(BigDecimal.valueOf(500000000))
        .currency(Currency.getInstance("IDR"))
        .interestRate(5.5)
        .status(ModelStatus.ACTIVE)
        .createdAt(now)
        .updatedAt(now)
        .createdBy("user1@email.com")
        .updatedBy("user1@email.com")
        .build();

    var loan2 = Loan.builder()
        .id(Id.generate())
        .user(userId2)
        .type(Loan.Type.LEND)
        .date(LocalDate.of(2024, 6, 20))
        .time(LocalTime.of(10, 0))
        .partyName("Jane Doe")
        .title("User 2 Personal Loan")
        .description("Loan for user 2")
        .amount(BigDecimal.valueOf(10000000))
        .remainingAmount(BigDecimal.valueOf(10000000))
        .currency(Currency.getInstance("IDR"))
        .interestRate(2.0)
        .status(ModelStatus.ACTIVE)
        .createdAt(now)
        .updatedAt(now)
        .createdBy("user2@email.com")
        .updatedBy("user2@email.com")
        .build();

    repository.create(loan1);
    repository.create(loan2);

    // When
    var user1Loans = repository.findByUserId(userId1.value());
    var user2Loans = repository.findByUserId(userId2.value());

    // Then
    assertEquals(1, user1Loans.size());
    assertEquals(1, user2Loans.size());

    assertEquals("User 1 Car Loan", user1Loans.get(0).title());
    assertEquals(userId1.value(), user1Loans.get(0).user().value());

    assertEquals("User 2 Personal Loan", user2Loans.get(0).title());
    assertEquals(userId2.value(), user2Loans.get(0).user().value());
  }
}
