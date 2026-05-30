package io.dkakunsi.bitapp.user.dto;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.dkakunsi.bitapp.common.Validatable;
import lombok.Builder;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public final record RegisterUserInput(
    String name,
    String email,
    String phone,
    String photoUrl) implements Validatable {

  private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

  @Override
  public void validate() throws IllegalArgumentException {
    var errors = new ArrayList<String>();
    if (name == null || name.isBlank()) {
      errors.add("name: invalid value");
    }
    if (email == null || email.isBlank() || !email.matches(EMAIL_REGEX)) {
      errors.add("email: invalid value");
    }

    if (!errors.isEmpty()) {
      throw new IllegalArgumentException(String.join(", ", errors));
    }
  }
}
