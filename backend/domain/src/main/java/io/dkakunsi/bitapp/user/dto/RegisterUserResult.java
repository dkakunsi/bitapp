package io.dkakunsi.bitapp.user.dto;

import io.dkakunsi.bitapp.user.entity.User;
import io.dkakunsi.bitapp.user.entity.User.Language;
import lombok.Builder;

@Builder
public final record RegisterUserResult(
    String email,
    String name,
    String phone,
    String photoUrl,
    Language language) {

  public static RegisterUserResult from(User user) {
    return RegisterUserResult.builder()
        .email(user.id().value())
        .name(user.name())
        .phone(user.phone())
        .photoUrl(user.photoUrl())
        .language(user.language())
        .build();
  }
}
