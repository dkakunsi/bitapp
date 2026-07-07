package io.dkakunsi.bitapp.user.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.dkakunsi.bitapp.user.domain.entity.User;
import lombok.Builder;

@Builder
@JsonInclude(Include.ALWAYS)
public final record UserResult(
    String email,
    String name,
    String phone,
    String photoUrl,
    String language) {

  public static UserResult from(User user) {
    return UserResult.builder()
        .email(user.id().value())
        .name(user.name())
        .phone(user.phone())
        .photoUrl(user.photoUrl())
        .language(user.language().name())
        .build();
  }
}
