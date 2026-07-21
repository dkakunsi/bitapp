package io.dkakunsi.bitapp.user.application.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.dkakunsi.bitapp.Id;
import io.dkakunsi.bitapp.Validatable;
import io.dkakunsi.bitapp.user.domain.entity.User;
import io.dkakunsi.bitapp.user.domain.entity.User.Language;
import lombok.Builder;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public final record RegisterUserInput(
    String name,
    String email,
    String phone,
    String photoUrl) implements Validatable {

  private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

  private static final Language DEFAULT_LANGUAGE = Language.EN;

  @Override
  public void validate() throws IllegalArgumentException {
    var errors = new ArrayList<String>();
    if (name == null || name.isBlank()) {
      errors.add("name: invalid value: " + name);
    }
    if (email == null || email.isBlank() || !email.matches(EMAIL_REGEX)) {
      errors.add("email: invalid value: " + email);
    }

    if (!errors.isEmpty()) {
      throw new IllegalArgumentException(String.join(", ", errors));
    }
  }

  public User toUser() {
    final var now = LocalDateTime.now();
    final var executor = this.email();
    return User.builder()
        .id(Id.of(this.email()))
        .name(this.name())
        .phone(this.phone())
        .photoUrl(this.photoUrl())
        .language(DEFAULT_LANGUAGE)
        .active(true)
        .createdAt(now)
        .updatedAt(now)
        .createdBy(executor)
        .updatedBy(executor)
        .build();
  }
}
