package io.dkakunsi.bitapp.user.validation;

import io.dkakunsi.bitapp.user.model.User;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator for language strings.
 */
public class LanguageValidator implements ConstraintValidator<ValidLanguage, String> {

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null || value.isBlank()) {
      return true; // @NotBlank handles null/empty
    }

    try {
      var language = User.Language.from(value);
      return language != null;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }
}
