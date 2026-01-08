package io.dkakunsi.bitapp.user.model;

import java.util.Objects;

import io.dkakunsi.bitapp.common.Id;
import io.dkakunsi.bitapp.user.dto.UserRegistrationInput;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@EqualsAndHashCode
@ToString
public final class User {

  private static final Language DEFAULT_LANGUAGE = Language.EN;

  // email as Id
  private final Id id;
  private final String name;
  private final String phone;
  private final String photoUrl;
  private final Language language;

  public static enum Language {
    EN,
    ID,
  }

  public boolean needUpdate(UserRegistrationInput userModel) {
    return !Objects.equals(this.name, userModel.name())
        || !Objects.equals(this.phone, userModel.phone())
        || !Objects.equals(this.photoUrl, userModel.photoUrl());
  }

  public User update(UserRegistrationInput userInput) {
    return User.builder()
        .id(this.id)
        .language(this.language)
        .name(userInput.name())
        .phone(userInput.phone())
        .photoUrl(userInput.photoUrl())
        .build();
  }

  public static User from(UserRegistrationInput userInput) {
    Objects.requireNonNull(userInput.email(), "Email cannot be null");
    Objects.requireNonNull(userInput.name(), "Name cannot be null");
    return User.builder()
        .id(Id.of(userInput.email()))
        .name(userInput.name())
        .phone(userInput.phone())
        .photoUrl(userInput.photoUrl())
        .language(DEFAULT_LANGUAGE)
        .build();
  }
}
