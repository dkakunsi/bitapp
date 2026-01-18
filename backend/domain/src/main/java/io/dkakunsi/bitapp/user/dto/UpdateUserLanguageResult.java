package io.dkakunsi.bitapp.user.dto;

import io.dkakunsi.bitapp.user.entity.User;
import io.dkakunsi.bitapp.user.entity.User.Language;
import lombok.Builder;

@Builder
public final record UpdateUserLanguageResult(
    String email,
    Language language) {

  public static UpdateUserLanguageResult from(User user) {
    return UpdateUserLanguageResult.builder()
        .email(user.id().value())
        .language(user.language())
        .build();
  }
}
