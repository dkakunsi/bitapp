package io.dkakunsi.bitapp.user.model;

import java.util.Objects;

import io.dkakunsi.bitapp.common.Id;
import io.dkakunsi.bitapp.user.dto.RegisterUserInput;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@EqualsAndHashCode
@ToString
public final class User {

  // email as Id
  @NotBlank
  private final Id id;
  @NotBlank
  private final String name;
  private final String phone;
  private final String photoUrl;
  private final Language language;

  public static enum Language {
    EN,
    ID,
  }

  public boolean needUpdate(RegisterUserInput userModel) {
    return !Objects.equals(this.name, userModel.name())
        || !Objects.equals(this.phone, userModel.phone())
        || !Objects.equals(this.photoUrl, userModel.photoUrl());
  }

  public User update(RegisterUserInput userInput) {
    return User.builder()
        .id(this.id)
        .language(this.language)
        .name(userInput.name())
        .phone(userInput.phone())
        .photoUrl(userInput.photoUrl())
        .build();
  }

  public User updateLanguage(Language newLanguage) {
    return User.builder()
        .id(this.id)
        .name(this.name)
        .phone(this.phone)
        .photoUrl(this.photoUrl)
        .language(newLanguage)
        .build();
  }
}
