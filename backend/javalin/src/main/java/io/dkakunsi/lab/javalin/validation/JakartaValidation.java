package io.dkakunsi.lab.javalin.validation;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

public class JakartaValidation implements io.dkakunsi.bitapp.common.Validator {

  @Override
  public boolean validate(Object input) {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    Validator validator = factory.getValidator();
    var violations = validator.validate(input);
    return violations.isEmpty();
  }
}
