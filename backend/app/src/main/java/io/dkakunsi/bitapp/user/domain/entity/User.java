package io.dkakunsi.bitapp.user.domain.entity;

import java.time.LocalDateTime;
import java.util.Objects;

import io.dkakunsi.bitapp.EntityStatus;
import io.dkakunsi.bitapp.Id;
import io.dkakunsi.bitapp.user.application.dto.RegisterUserInput;
import io.dkakunsi.bitapp.user.application.dto.UserResult;
import lombok.Builder;

@Builder
public final record User(
    Id id,
    String name,
    String phone,
    String photoUrl,
    Language language,

    EntityStatus status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    String createdBy,
    String updatedBy) {

  private static final Language DEFAULT_LANGUAGE = Language.EN;

  public static enum Language {
    EN,
    ID;

    public static boolean isValid(String language) {
      if (language == null) {
        return false;
      }

      try {
        valueOf(language);
        return true;
      } catch (IllegalArgumentException _) {
        return false;
      }
    }

  }

  public static User from(RegisterUserInput input) {
    final var userId = Id.of(input.email());
    final var now = LocalDateTime.now();
    final var executor = input.email();
    return User.builder()
        .id(userId)
        .name(input.name())
        .phone(input.phone())
        .photoUrl(input.photoUrl())
        .language(DEFAULT_LANGUAGE)
        .status(EntityStatus.ACTIVE)
        .createdAt(now)
        .updatedAt(now)
        .createdBy(executor)
        .updatedBy(executor)
        .build();
  }

  public boolean needUpdate(RegisterUserInput userModel) {
    return !Objects.equals(this.name, userModel.name())
        || !Objects.equals(this.phone, userModel.phone())
        || !Objects.equals(this.photoUrl, userModel.photoUrl());
  }

  public User update(RegisterUserInput userInput) {
    var executor = userInput.email();
    return User.builder()
        .id(this.id)
        .language(this.language)
        .name(userInput.name())
        .phone(userInput.phone())
        .photoUrl(userInput.photoUrl())
        .status(this.status)
        .createdAt(this.createdAt)
        .updatedAt(LocalDateTime.now())
        .createdBy(this.createdBy)
        .updatedBy(executor)
        .build();
  }

  public User updateLanguage(Language newLanguage, String requester) {
    return User.builder()
        .id(this.id)
        .name(this.name)
        .phone(this.phone)
        .photoUrl(this.photoUrl)
        .language(newLanguage)
        .status(this.status)
        .createdAt(this.createdAt)
        .updatedAt(LocalDateTime.now())
        .createdBy(this.createdBy)
        .updatedBy(requester)
        .build();
  }

  public UserResult toResult() {
    return UserResult.builder()
        .email(this.id().value())
        .name(this.name())
        .phone(this.phone())
        .photoUrl(this.photoUrl())
        .language(this.language().name())
        .build();
  }
}
