package io.dkakunsi.bitapp.loan.validation;

import io.dkakunsi.bitapp.loan.model.Loan;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator for loan type strings.
 */
public class LoanTypeValidator implements ConstraintValidator<ValidLoanType, String> {

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null || value.isBlank()) {
      return true; // @NotBlank handles null/empty
    }

    try {
      var type = Loan.Type.from(value);
      return type != null;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }
}
