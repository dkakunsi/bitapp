package io.dkakunsi.lab.javalin.validation;

import java.util.List;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

public class JakartaValidation implements io.dkakunsi.bitapp.common.Validator {

  private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

  @Override
  public <T> List<Violation> validate(T input) {
    return VALIDATOR.validate(input)
        .stream()
        .map(v -> new Violation(v.getPropertyPath().toString(), v.getMessage()))
        .toList();
  }
}
