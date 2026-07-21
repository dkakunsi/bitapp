package io.dkakunsi.bitapp.user.domain.entity;

import java.time.LocalDateTime;
import java.util.Objects;

import io.dkakunsi.bitapp.Id;
import lombok.Builder;

@Builder
public final record User(
    Id id,
    String name,
    String phone,
    String photoUrl,
    Language language,

    Boolean active,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    String createdBy,
    String updatedBy) {

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

  public boolean needUpdate(User input) {
    return !Objects.equals(this.name, input.name())
        || !Objects.equals(this.phone, input.phone())
        || !Objects.equals(this.photoUrl, input.photoUrl());
  }

  public User update(User input, String requester) {
    var updatedName = input.name() != null ? input.name() : this.name;
    var updatedPhone = input.phone() != null ? input.phone() : this.phone;
    var updatedPhotoUrl = input.photoUrl() != null ? input.photoUrl() : this.photoUrl;
    return User.builder()
        .id(this.id)
        .language(this.language)
        .name(updatedName)
        .phone(updatedPhone)
        .photoUrl(updatedPhotoUrl)
        .active(this.active)
        .createdAt(this.createdAt)
        .updatedAt(LocalDateTime.now())
        .createdBy(this.createdBy)
        .updatedBy(requester)
        .build();
  }

  public User updateLanguage(Language newLanguage, String requester) {
    return User.builder()
        .id(this.id)
        .name(this.name)
        .phone(this.phone)
        .photoUrl(this.photoUrl)
        .language(newLanguage)
        .active(this.active)
        .createdAt(this.createdAt)
        .updatedAt(LocalDateTime.now())
        .createdBy(this.createdBy)
        .updatedBy(requester)
        .build();
  }
}
