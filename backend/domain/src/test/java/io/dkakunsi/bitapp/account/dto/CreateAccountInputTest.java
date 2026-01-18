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
    var account = Account.from(input, requester);

    // Then
    assertNotNull(account);
    assertNotNull(account.id());
    assertEquals(name, account.name());
    assertEquals(themeColor, account.themeColor());
    assertEquals(Account.Type.BANK, account.type());
    assertNotNull(account.user());
    assertEquals(requester, account.user().value());
    assertEquals(BigDecimal.ZERO, account.balance());
    assertNotNull(account.createdAt());
    assertNotNull(account.updatedAt());
    assertEquals(requester, account.createdBy());
    assertEquals(requester, account.updatedBy());
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
    var account = Account.from(input, requester);

    // Then
    assertNotNull(account);
    assertEquals("#FFFFFF", account.themeColor());
    assertEquals(name, account.name());
    assertEquals(Account.Type.CASH, account.type());
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
    var account = Account.from(input, requester);

    // Then
    assertNotNull(account);
    assertEquals(Account.Type.BANK, account.type());
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
    var account = Account.from(input, requester);

    // Then
    assertNotNull(account);
    assertEquals(Account.Type.CASH, account.type());
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
    var account = Account.from(input, requester);

    // Then
    assertNotNull(account);
    assertEquals(Account.Type.EWALLET, account.type());
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
    var account = Account.from(input, requester);

    // Then
    assertNotNull(account);
    assertEquals(Account.Type.OTHER, account.type());
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
    var account = Account.from(input, requester);

    // Then
    assertNotNull(account);
    assertEquals(BigDecimal.ZERO, account.balance());
    assertEquals(0, account.balance().compareTo(BigDecimal.ZERO));
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
    var account1 = Account.from(input, requester);
    var account2 = Account.from(input, requester);

    // Then
    assertNotNull(account1.id());
    assertNotNull(account2.id());
    assertEquals(false, account1.id().equals(account2.id()));
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
    var account = Account.from(input, requester);

    // Then
    assertNotNull(account.user());
    assertEquals(requester, account.user().value());
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
    var account = Account.from(input, requester);

    // Then
    assertNotNull(account.createdAt());
    assertNotNull(account.updatedAt());
    assertEquals(account.createdAt(), account.updatedAt());
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
    var account = Account.from(input, requester);

    // Then
    assertEquals(requester, account.createdBy());
    assertEquals(requester, account.updatedBy());
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
    var account1 = Account.from(input1, requester);
    var account2 = Account.from(input2, requester);
    var account3 = Account.from(input3, requester);

    // Then
    assertEquals("#FF0000", account1.themeColor());
    assertEquals("#00FF00", account2.themeColor());
    assertEquals("#0000FF", account3.themeColor());
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
    var account = Account.from(input, requester);

    // Then
    assertEquals(expectedName, account.name());
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
    var account1 = Account.from(input, requester1);
    var account2 = Account.from(input, requester2);

    // Then
    assertEquals(requester1, account1.user().value());
    assertEquals(requester2, account2.user().value());
    assertEquals(false, account1.user().equals(account2.user()));
  }
}
