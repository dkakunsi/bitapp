package io.dkakunsi.bitapp.mongo.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Currency;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.morphia.Datastore;
import io.dkakunsi.bitapp.common.Id;
import io.dkakunsi.bitapp.common.ModelStatus;
import io.dkakunsi.bitapp.loan.model.Loan;
import io.dkakunsi.bitapp.mongo.entity.LoanEntity;

public class MongoLoanRepositoryTest {

  private Datastore datastore;
  private MongoLoanRepository underTest;

  @BeforeEach
  public void setUp() {
    datastore = mock(Datastore.class);
    underTest = new MongoLoanRepository(datastore);
  }

  @Test
  public void givenValidLoanWhenCreateThenShouldSaveLoanEntity() {
    // Given
    var loanId = Id.of("loan-123");
    var userId = Id.of("user-456");
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
        .createdBy("admin")
        .updatedBy("admin")
        .build();

    when(datastore.save(any(LoanEntity.class))).thenReturn(null);

    // When
    var result = underTest.create(loan);

    // Then
    assertNotNull(result);
    assertEquals(loan, result);
    verify(datastore).save(any(LoanEntity.class));
  }

  @Test
  public void givenLoanWithLendTypeWhenCreateThenShouldSaveLoanEntity() {
    // Given
    var loanId = Id.of("loan-789");
    var userId = Id.of("user-101");
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
        .createdBy("admin")
        .updatedBy("admin")
        .build();

    when(datastore.save(any(LoanEntity.class))).thenReturn(null);

    // When
    var result = underTest.create(loan);

    // Then
    assertNotNull(result);
    assertEquals(loan, result);
    verify(datastore).save(any(LoanEntity.class));
  }

  @Test
  public void givenLoanWithMinimalDataWhenCreateThenShouldSaveLoanEntity() {
    // Given
    var loanId = Id.of("loan-minimal");
    var userId = Id.of("user-minimal");
    var loan = Loan.builder()
        .id(loanId)
        .user(userId)
        .type(Loan.Type.BORROW)
        .date(LocalDate.now())
        .time(LocalTime.now())
        .partyName("Test Party")
        .title("Test Loan")
        .description("Test Description")
        .amount(BigDecimal.valueOf(1000))
        .remainingAmount(BigDecimal.valueOf(1000))
        .currency(Currency.getInstance("IDR"))
        .interestRate(0.0)
        .status(ModelStatus.ACTIVE)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .createdBy("system")
        .updatedBy("system")
        .build();

    when(datastore.save(any(LoanEntity.class))).thenReturn(null);

    // When
    var result = underTest.create(loan);

    // Then
    assertNotNull(result);
    assertEquals(loan, result);
    verify(datastore).save(any(LoanEntity.class));
  }

  @Test
  public void givenDifferentCurrenciesWhenCreateThenShouldSaveLoanEntity() {
    // Given
    var loanId = Id.of("loan-eur");
    var userId = Id.of("user-eur");
    var loan = Loan.builder()
        .id(loanId)
        .user(userId)
        .type(Loan.Type.LEND)
        .date(LocalDate.of(2026, 1, 17))
        .time(LocalTime.of(16, 45))
        .partyName("European Partner")
        .title("EUR Loan")
        .description("Loan in Euro")
        .amount(BigDecimal.valueOf(25000))
        .remainingAmount(BigDecimal.valueOf(20000))
        .currency(Currency.getInstance("EUR"))
        .interestRate(3.5)
        .status(ModelStatus.ACTIVE)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .createdBy("admin")
        .updatedBy("admin")
        .build();

    when(datastore.save(any(LoanEntity.class))).thenReturn(null);

    // When
    var result = underTest.create(loan);

    // Then
    assertNotNull(result);
    assertEquals(loan, result);
    verify(datastore).save(any(LoanEntity.class));
  }
}
