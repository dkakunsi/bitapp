package io.dkakunsi.bitapp.user.dto;

import io.dkakunsi.bitapp.user.entity.User;
import lombok.Builder;

@Builder
public final record GetUserResult(String email,
    String name,
    String phone,
    String photoUrl,
    String language) {

  public static GetUserResult from(User user) {
    return GetUserResult.builder()
        .email(user.id().value())
        .name(user.name())
        .phone(user.phone())
        .photoUrl(user.photoUrl())
        .language(user.language().name())
        .build();
  }
}
