package io.dkakunsi.bitapp.account.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import io.dkakunsi.bitapp.account.model.Account;

public final class CreateAccountInputTest {

  /**
   * <b>Given</b> a CreateAccountInput with all fields provided<br>
   * <b>When</b> toAccount is called with a requester<br>
   * <b>Then</b> an Account should be created with all provided values and the
   * specified theme color
   */
  @Test
  public void givenCreateAccountInputWithAllFieldsWhenToAccountThenShouldCreateAccountWithProvidedValues() {
    // Given
    var requester = "user@email.com";
    var name = "My Bank Account";
    var themeColor = "#0000FF";
    var type = "BANK";
    var input = CreateAccountInput.builder()
        .name(name)
        .themeColor(themeColor)
        .type(type)
        .build();

    // When
    var account = input.toAccount(requester);

    // Then
    assertNotNull(account);
    assertNotNull(account.getId());
    assertEquals(name, account.getName());
    assertEquals(themeColor, account.getThemeColor());
    assertEquals(Account.Type.BANK, account.getType());
    assertNotNull(account.getUser());
    assertEquals(requester, account.getUser().getId().value());
    assertEquals(BigDecimal.ZERO, account.getBalance());
    assertNotNull(account.getCreatedAt());
    assertNotNull(account.getUpdatedAt());
    assertEquals(requester, account.getCreatedBy());
    assertEquals(requester, account.getUpdatedBy());
  }

  /**
   * <b>Given</b> a CreateAccountInput without themeColor (null)<br>
   * <b>When</b> toAccount is called<br>
   * <b>Then</b> an Account should be created with the default theme color #FFFFFF
   */
  @Test
  public void givenCreateAccountInputWithoutThemeColorWhenToAccountThenShouldUseDefaultThemeColor() {
    // Given
    var requester = "user@email.com";
    var name = "Simple Account";
    var type = "CASH";
    var input = CreateAccountInput.builder()
        .name(name)
        .themeColor(null)
        .type(type)
        .build();

    // When
    var account = input.toAccount(requester);

    // Then
    assertNotNull(account);
    assertEquals("#FFFFFF", account.getThemeColor());
    assertEquals(name, account.getName());
    assertEquals(Account.Type.CASH, account.getType());
  }

  /**
   * <b>Given</b> a CreateAccountInput with BANK type<br>
   * <b>When</b> toAccount is called<br>
   * <b>Then</b> an Account should be created with BANK type
   */
  @Test
  public void givenCreateAccountInputWithBankTypeWhenToAccountThenShouldCreateBankAccount() {
    // Given
    var requester = "user@email.com";
    var input = CreateAccountInput.builder()
        .name("Bank Account")
        .themeColor("#FF0000")
        .type("BANK")
        .build();

    // When
    var account = input.toAccount(requester);

    // Then
    assertNotNull(account);
    assertEquals(Account.Type.BANK, account.getType());
  }

  /**
   * <b>Given</b> a CreateAccountInput with CASH type<br>
   * <b>When</b> toAccount is called<br>
   * <b>Then</b> an Account should be created with CASH type
   */
  @Test
  public void givenCreateAccountInputWithCashTypeWhenToAccountThenShouldCreateCashAccount() {
    // Given
    var requester = "user@email.com";
    var input = CreateAccountInput.builder()
        .name("Cash Wallet")
        .themeColor("#00FF00")
        .type("CASH")
        .build();

    // When
    var account = input.toAccount(requester);

    // Then
    assertNotNull(account);
    assertEquals(Account.Type.CASH, account.getType());
  }

  /**
   * <b>Given</b> a CreateAccountInput with EWALLET type<br>
   * <b>When</b> toAccount is called<br>
   * <b>Then</b> an Account should be created with EWALLET type
   */
  @Test
  public void givenCreateAccountInputWithEWalletTypeWhenToAccountThenShouldCreateEWalletAccount() {
    // Given
    var requester = "user@email.com";
    var input = CreateAccountInput.builder()
        .name("Digital Wallet")
        .themeColor("#0000FF")
        .type("EWALLET")
        .build();

    // When
    var account = input.toAccount(requester);

    // Then
    assertNotNull(account);
    assertEquals(Account.Type.EWALLET, account.getType());
  }

  /**
   * <b>Given</b> a CreateAccountInput with OTHER type<br>
   * <b>When</b> toAccount is called<br>
   * <b>Then</b> an Account should be created with OTHER type
   */
  @Test
  public void givenCreateAccountInputWithOtherTypeWhenToAccountThenShouldCreateOtherAccount() {
    // Given
    var requester = "user@email.com";
    var input = CreateAccountInput.builder()
        .name("Other Account")
        .type("OTHER")
        .build();

    // When
    var account = input.toAccount(requester);

    // Then
    assertNotNull(account);
    assertEquals(Account.Type.OTHER, account.getType());
  }

  /**
   * <b>Given</b> a CreateAccountInput<br>
   * <b>When</b> toAccount is called<br>
   * <b>Then</b> the created Account should have balance initialized to zero
   */
  @Test
  public void givenCreateAccountInputWhenToAccountThenShouldInitializeBalanceToZero() {
    // Given
    var requester = "user@email.com";
    var input = CreateAccountInput.builder()
        .name("New Account")
        .type("BANK")
        .build();

    // When
    var account = input.toAccount(requester);

    // Then
    assertNotNull(account);
    assertEquals(BigDecimal.ZERO, account.getBalance());
    assertEquals(0, account.getBalance().compareTo(BigDecimal.ZERO));
  }

  /**
   * <b>Given</b> a CreateAccountInput<br>
   * <b>When</b> toAccount is called<br>
   * <b>Then</b> the created Account should have a generated unique ID
   */
  @Test
  public void givenCreateAccountInputWhenToAccountThenShouldGenerateUniqueId() {
    // Given
    var requester = "user@email.com";
    var input = CreateAccountInput.builder()
        .name("Account 1")
        .type("BANK")
        .build();

    // When
    var account1 = input.toAccount(requester);
    var account2 = input.toAccount(requester);

    // Then
    assertNotNull(account1.getId());
    assertNotNull(account2.getId());
    assertEquals(false, account1.getId().equals(account2.getId()));
  }

  /**
   * <b>Given</b> a CreateAccountInput<br>
   * <b>When</b> toAccount is called with a requester<br>
   * <b>Then</b> the created Account should have user set to the requester
   */
  @Test
  public void givenCreateAccountInputWhenToAccountThenShouldSetUserFromRequester() {
    // Given
    var requester = "john.doe@example.com";
    var input = CreateAccountInput.builder()
        .name("John's Account")
        .type("CASH")
        .build();

    // When
    var account = input.toAccount(requester);

    // Then
    assertNotNull(account.getUser());
    assertEquals(requester, account.getUser().getId().value());
  }

  /**
   * <b>Given</b> a CreateAccountInput<br>
   * <b>When</b> toAccount is called<br>
   * <b>Then</b> the created Account should have createdAt and updatedAt set to
   * current time
   */
  @Test
  public void givenCreateAccountInputWhenToAccountThenShouldSetTimestamps() {
    // Given
    var requester = "user@email.com";
    var input = CreateAccountInput.builder()
        .name("Timestamped Account")
        .type("BANK")
        .build();

    // When
    var account = input.toAccount(requester);

    // Then
    assertNotNull(account.getCreatedAt());
    assertNotNull(account.getUpdatedAt());
    assertEquals(account.getCreatedAt(), account.getUpdatedAt());
  }

  /**
   * <b>Given</b> a CreateAccountInput<br>
   * <b>When</b> toAccount is called with a requester<br>
   * <b>Then</b> the created Account should have createdBy and updatedBy set to
   * the requester
   */
  @Test
  public void givenCreateAccountInputWhenToAccountThenShouldSetAuditFields() {
    // Given
    var requester = "admin@example.com";
    var input = CreateAccountInput.builder()
        .name("Audited Account")
        .type("EWALLET")
        .build();

    // When
    var account = input.toAccount(requester);

    // Then
    assertEquals(requester, account.getCreatedBy());
    assertEquals(requester, account.getUpdatedBy());
  }

  /**
   * <b>Given</b> CreateAccountInput instances with different theme colors<br>
   * <b>When</b> toAccount is called for each<br>
   * <b>Then</b> each Account should have its respective theme color
   */
  @Test
  public void givenCreateAccountInputWithDifferentThemeColorsWhenToAccountThenShouldPreserveThemeColors() {
    // Given
    var requester = "user@email.com";
    var input1 = CreateAccountInput.builder()
        .name("Red Account")
        .themeColor("#FF0000")
        .type("BANK")
        .build();
    var input2 = CreateAccountInput.builder()
        .name("Green Account")
        .themeColor("#00FF00")
        .type("CASH")
        .build();
    var input3 = CreateAccountInput.builder()
        .name("Blue Account")
        .themeColor("#0000FF")
        .type("EWALLET")
        .build();

    // When
    var account1 = input1.toAccount(requester);
    var account2 = input2.toAccount(requester);
    var account3 = input3.toAccount(requester);

    // Then
    assertEquals("#FF0000", account1.getThemeColor());
    assertEquals("#00FF00", account2.getThemeColor());
    assertEquals("#0000FF", account3.getThemeColor());
  }

  /**
   * <b>Given</b> a CreateAccountInput with a specific name<br>
   * <b>When</b> toAccount is called<br>
   * <b>Then</b> the created Account should preserve the exact name
   */
  @Test
  public void givenCreateAccountInputWithNameWhenToAccountThenShouldPreserveName() {
    // Given
    var requester = "user@email.com";
    var expectedName = "My Special Savings Account";
    var input = CreateAccountInput.builder()
        .name(expectedName)
        .type("BANK")
        .build();

    // When
    var account = input.toAccount(requester);

    // Then
    assertEquals(expectedName, account.getName());
  }

  /**
   * <b>Given</b> CreateAccountInput instances for different requesters<br>
   * <b>When</b> toAccount is called for each requester<br>
   * <b>Then</b> each Account should belong to its respective requester
   */
  @Test
  public void givenCreateAccountInputForDifferentRequestersWhenToAccountThenShouldCreateAccountsForEachRequester() {
    // Given
    var requester1 = "user1@email.com";
    var requester2 = "user2@email.com";
    var input = CreateAccountInput.builder()
        .name("Shared Account Template")
        .type("BANK")
        .build();

    // When
    var account1 = input.toAccount(requester1);
    var account2 = input.toAccount(requester2);

    // Then
    assertEquals(requester1, account1.getUser().getId().value());
    assertEquals(requester2, account2.getUser().getId().value());
    assertEquals(false, account1.getUser().getId().equals(account2.getUser().getId()));
  }
}
