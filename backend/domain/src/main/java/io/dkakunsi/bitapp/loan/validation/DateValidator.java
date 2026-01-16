package io.dkakunsi.bitapp.loan.validation;

import java.time.LocalDate;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DateValidator implements ConstraintValidator<ValidDate, String> {

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null || value.isBlank()) {
      return true; // Null or blank values are valid (optional field)
    }

    try {
      var parsedDate = LocalDate.parse(value);
      return parsedDate != null;
    } catch (Exception e) {
      return false;
    }
  }
}
