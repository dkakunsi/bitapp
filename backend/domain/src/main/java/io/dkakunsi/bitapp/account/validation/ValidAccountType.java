package io.dkakunsi.bitapp.account.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * Validation annotation for account type fields.
 * Validates that the string is a valid account type (BANK, CASH, EWALLET, or
 * OTHER).
 */
@Documented
@Constraint(validatedBy = AccountTypeValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidAccountType {
  String message() default "invalid value";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
