package io.dkakunsi.bitapp.account.validation;

import io.dkakunsi.bitapp.account.model.Account;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator for account type strings.
 */
public class AccountTypeValidator implements ConstraintValidator<ValidAccountType, String> {

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null || value.isBlank()) {
      return true; // @NotBlank handles null/empty
    }

    try {
      var type = Account.Type.from(value);
      return type != null;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }
}
