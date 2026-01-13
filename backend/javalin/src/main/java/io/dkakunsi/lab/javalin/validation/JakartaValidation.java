package io.dkakunsi.lab.javalin.validation;

import java.util.List;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

public class JakartaValidation implements io.dkakunsi.bitapp.common.Validator {

  @Override
  public <T> List<Violation> validate(T input) {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    Validator validator = factory.getValidator();
    return validator.validate(input)
        .stream()
        .map(v -> new Violation(v.getPropertyPath().toString(), v.getMessage()))
        .toList();
  }
}
