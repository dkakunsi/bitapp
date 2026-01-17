package io.dkakunsi.bitapp.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.dkakunsi.bitapp.common.AppError.Code;
import io.dkakunsi.bitapp.common.Context;
import io.dkakunsi.bitapp.common.Validator;
import io.dkakunsi.bitapp.common.Validator.Violation;
import io.dkakunsi.bitapp.common.usecase.Result;

class ValidationAspectTest {

  private Validator validator;
  private ValidationAspect validationAspect;
  private ProceedingJoinPoint joinPoint;
  private Context context;

  @BeforeEach
  void setUp() {
    validator = mock(Validator.class);
    validationAspect = new ValidationAspect(validator);
    joinPoint = mock(ProceedingJoinPoint.class);
    context = mock(Context.class);
  }

  @Test
  void shouldProceedWhenValidationPasses() throws Throwable {
    // Given
    Object input = new Object();
    when(validator.validate(input)).thenReturn(Collections.emptyList());
    Result<?> expectedResult = Result.success("Success");
    when(joinPoint.proceed()).thenReturn(expectedResult);

    // When
    Object result = validationAspect.validateBeforeProcess(joinPoint, context, input);

    // Then
    verify(validator).validate(input);
    verify(joinPoint).proceed();
    assertEquals(expectedResult, result);
  }

  @Test
  void shouldReturnValidationErrorWhenValidationFails() throws Throwable {
    // Given
    Object input = new Object();
    var violations = List.of(
        new Violation("email", "must not be blank"),
        new Violation("name", "must not be blank"));
    when(validator.validate(input)).thenReturn(violations);

    // When
    Object result = validationAspect.validateBeforeProcess(joinPoint, context, input);

    // Then
    verify(validator).validate(input);
    verify(joinPoint, never()).proceed();

    assertTrue(result instanceof Result);
    @SuppressWarnings("unchecked")
    Result<Object> castedResult = (Result<Object>) result;
    assertTrue(castedResult.isFailed());
    assertTrue(castedResult.error().isPresent());
    assertEquals(Code.BAD_REQUEST, castedResult.error().get().code());
    assertTrue(castedResult.error().get().message().contains("email: must not be blank"));
    assertTrue(castedResult.error().get().message().contains("name: must not be blank"));
  }

  @Test
  void shouldUseDefaultValidatorWhenNoValidatorProvided() {
    // When
    ValidationAspect aspect = new ValidationAspect();

    // Then
    // Should not throw exception
    assertTrue(aspect != null);
  }
}
