package io.dkakunsi.bitapp.user.dto;

import io.dkakunsi.bitapp.user.model.User;
import io.dkakunsi.bitapp.user.model.User.Language;
import lombok.Builder;

@Builder
public final record GetUserResult(String email,
    String name,
    String phone,
    String photoUrl,
    Language language) {

  public static GetUserResult from(User user) {
    return GetUserResult.builder()
        .email(user.getId().value())
        .name(user.getName())
        .phone(user.getPhone())
        .photoUrl(user.getPhotoUrl())
        .language(user.getLanguage())
        .build();
  }
}
