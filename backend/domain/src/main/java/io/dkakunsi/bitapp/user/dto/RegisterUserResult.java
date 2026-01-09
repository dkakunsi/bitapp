package io.dkakunsi.bitapp.user.dto;

import io.dkakunsi.bitapp.user.model.User;
import io.dkakunsi.bitapp.user.model.User.Language;
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
        .email(user.getId().value())
        .name(user.getName())
        .phone(user.getPhone())
        .photoUrl(user.getPhotoUrl())
        .language(user.getLanguage())
        .build();
  }
}
