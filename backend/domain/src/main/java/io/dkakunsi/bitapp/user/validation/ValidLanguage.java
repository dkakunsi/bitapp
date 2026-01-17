package io.dkakunsi.bitapp.user.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * Validation annotation for language fields.
 * Validates that the string is a valid language (EN or ID).
 */
@Documented
@Constraint(validatedBy = LanguageValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidLanguage {
  String message() default "invalid value";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
