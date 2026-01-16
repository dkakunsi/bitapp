package io.dkakunsi.bitapp.loan.validation;

import java.time.LocalTime;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class TimeValidator implements ConstraintValidator<ValidTime, String> {

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null || value.isBlank()) {
      return true; // Null or blank values are valid (optional field)
    }

    try {
      var parsedTime = LocalTime.parse(value);
      return parsedTime != null;
    } catch (Exception e) {
      return false;
    }
  }
}
