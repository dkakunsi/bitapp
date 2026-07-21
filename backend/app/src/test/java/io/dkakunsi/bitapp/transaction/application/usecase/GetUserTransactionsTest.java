package io.dkakunsi.bitapp.transaction.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Currency;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.dkakunsi.bitapp.Context;
import io.dkakunsi.bitapp.Id;
import io.dkakunsi.bitapp.transaction.domain.entity.Transaction;
import io.dkakunsi.bitapp.transaction.domain.repository.TransactionRepository;

public final class GetUserTransactionsTest {

  private static final String REQUESTER = "test@email.com";

  private static final Context context = Context.builder().requester(REQUESTER).build();

  private static final String USER_ID = "user@email.com";
  private static final Id USER = Id.of(USER_ID);
  private static final String ACCOUNT_ID_1 = "account-1";
  private static final String ACCOUNT_ID_2 = "account-2";
  private static final String LOAN_ID = "loan-1";

  private GetUserTransactions underTest;

  private TransactionRepository transactionRepository;

  @BeforeEach
  void setUp() {
    transactionRepository = mock(TransactionRepository.class);
    underTest = new GetUserTransactions(transactionRepository);
  }

  @Test
  void givenValidUserIdWhenTransactionsExistThenShouldReturnTransactionsList() {
    // Given
    var input = USER_ID;

    var transaction1 = createTransaction("trans1", "DEBIT", "Grocery Shopping", ACCOUNT_ID_1, null, null);
    var transaction2 = createTransaction("trans2", "CREDIT", "Salary", null, ACCOUNT_ID_2, null);
    var transaction3 = createTransaction("trans3", "TRANSFER", "Transfer to Savings", ACCOUNT_ID_1, ACCOUNT_ID_2,
        null);

    var transactions = Arrays.asList(transaction1, transaction2, transaction3);
    when(transactionRepository.findByUserId(USER)).thenReturn(transactions);

    // When
    var result = Context.executeInContext(context, () -> underTest.process(input));

    // Then
    assertTrue(result.isSuccess());
    assertTrue(result.data().isPresent());

    var resultData = result.data().get();
    assertNotNull(resultData);
    assertEquals(3, resultData.size());

    // Verify first transaction
    var firstTransaction = resultData.get(0);
    assertEquals("trans1", firstTransaction.id());
    assertEquals(USER_ID, firstTransaction.user());
    assertEquals("DEBIT", firstTransaction.type());
    assertEquals("Grocery Shopping", firstTransaction.title());
    assertEquals(ACCOUNT_ID_1, firstTransaction.source());

    // Verify second transaction
    var secondTransaction = resultData.get(1);
    assertEquals("trans2", secondTransaction.id());
    assertEquals("CREDIT", secondTransaction.type());
    assertEquals("Salary", secondTransaction.title());
    assertEquals(ACCOUNT_ID_2, secondTransaction.destination());

    // Verify third transaction
    var thirdTransaction = resultData.get(2);
    assertEquals("trans3", thirdTransaction.id());
    assertEquals("TRANSFER", thirdTransaction.type());
    assertEquals("Transfer to Savings", thirdTransaction.title());
    assertEquals(ACCOUNT_ID_1, thirdTransaction.source());
    assertEquals(ACCOUNT_ID_2, thirdTransaction.destination());
  }

  @Test
  void givenValidUserIdWhenNoTransactionsThenShouldReturnEmptyList() {
    // Given
    var input = USER_ID;

    when(transactionRepository.findByUserId(USER)).thenReturn(List.of());

    // When
    var result = Context.executeInContext(context, () -> underTest.process(input));

    // Then
    assertTrue(result.isSuccess());
    assertTrue(result.data().isPresent());

    var resultData = result.data().get();
    assertNotNull(resultData);
    assertEquals(0, resultData.size());
  }

  @Test
  void givenValidUserIdWhenSingleTransactionThenShouldReturnSingletonList() {
    // Given
    var input = USER_ID;

    var transaction = createTransaction("trans1", "DEBIT", "Shopping", ACCOUNT_ID_1, null, null);
    when(transactionRepository.findByUserId(USER)).thenReturn(List.of(transaction));

    // When
    var result = Context.executeInContext(context, () -> underTest.process(input));

    // Then
    assertTrue(result.isSuccess());
    assertTrue(result.data().isPresent());

    var resultData = result.data().get();
    assertNotNull(resultData);
    assertEquals(1, resultData.size());

    var firstTransaction = resultData.get(0);
    assertEquals("trans1", firstTransaction.id());
    assertEquals("Shopping", firstTransaction.title());
  }

  @Test
  void givenValidUserIdWhenTransactionWithLoanThenShouldIncludeLoanId() {
    // Given
    var input = USER_ID;

    var transaction = createTransaction("trans1", "CREDIT", "Loan Disbursement", null, ACCOUNT_ID_2, LOAN_ID);
    when(transactionRepository.findByUserId(USER)).thenReturn(List.of(transaction));

    // When
    var result = Context.executeInContext(context, () -> underTest.process(input));

    // Then
    assertTrue(result.isSuccess());
    assertTrue(result.data().isPresent());

    var resultData = result.data().get();
    assertEquals(1, resultData.size());

    var firstTransaction = resultData.get(0);
    assertEquals(LOAN_ID, firstTransaction.loan());
    assertEquals("Loan Disbursement", firstTransaction.title());
  }

  private Transaction createTransaction(String id, String type, String title, String source, String destination,
      String loan) {
    return Transaction.builder()
        .id(Id.of(id))
        .user(USER)
        .title(title)
        .description("Test description")
        .date(LocalDate.of(2026, 1, 23))
        .time(LocalTime.of(10, 30))
        .source(source != null ? Id.of(source) : null)
        .destination(destination != null ? Id.of(destination) : null)
        .loan(loan != null ? Id.of(loan) : null)
        .amount(BigDecimal.valueOf(100000))
        .currency(Currency.getInstance("IDR"))
        .category(Transaction.Category.FOOD)
        .type(Transaction.Type.valueOf(type))
        .active(true)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .createdBy(REQUESTER)
        .updatedBy(REQUESTER)
        .build();
  }
}
