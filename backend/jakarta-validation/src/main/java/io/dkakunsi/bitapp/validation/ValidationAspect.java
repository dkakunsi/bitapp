package io.dkakunsi.bitapp.validation;

import java.util.stream.Collectors;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import io.dkakunsi.bitapp.common.AppError.Code;
import io.dkakunsi.bitapp.common.Context;
import io.dkakunsi.bitapp.common.Validator;
import io.dkakunsi.bitapp.common.usecase.Result;

@Aspect
public final class ValidationAspect {

  private final Validator validator;

  public ValidationAspect() {
    this.validator = new JakartaValidation();
  }

  public ValidationAspect(Validator validator) {
    this.validator = validator;
  }

  @Around("execution(* io.dkakunsi.bitapp..usecase.*.process(..)) && args(context, input)")
  public Object validateBeforeProcess(ProceedingJoinPoint joinPoint, Context context, Object input) throws Throwable {
    var violations = validator.validate(input);

    if (!violations.isEmpty()) {
      var errorMessage = violations.stream()
          .map(Validator.Violation::toString)
          .collect(Collectors.joining(", "));
      return Result.failure(Code.BAD_REQUEST, errorMessage);
    }

    return joinPoint.proceed();
  }
}
