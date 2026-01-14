package io.dkakunsi.bitapp.user.dto;

import io.dkakunsi.bitapp.common.Id;
import io.dkakunsi.bitapp.user.model.User;
import io.dkakunsi.bitapp.user.model.User.Language;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public final record RegisterUserInput(
    @NotBlank String name,
    @NotBlank @Email String email,
    String phone,
    String photoUrl) {

  private static final Language DEFAULT_LANGUAGE = Language.EN;

  public User toUser() {
    return User.builder()
        .id(Id.of(this.email()))
        .name(this.name())
        .phone(this.phone())
        .photoUrl(this.photoUrl())
        .language(DEFAULT_LANGUAGE)
        .build();
  }
}
