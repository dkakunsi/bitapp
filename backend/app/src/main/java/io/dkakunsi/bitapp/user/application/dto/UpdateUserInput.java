package io.dkakunsi.bitapp.user.application.dto;

import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;

import io.dkakunsi.bitapp.Validatable;
import io.dkakunsi.bitapp.user.domain.entity.User;
import lombok.Builder;

@Builder
public final record UpdateUserInput(
    String email,
    String language) implements Validatable {

  @Override
  public void validate() throws IllegalArgumentException {
    var errors = new ArrayList<String>();
    if (StringUtils.isBlank(email)) {
      errors.add("email: invalid value: " + email);
    }
    if (!User.Language.isValid(language)) {
      errors.add("language: invalid value: " + language);
    }

    if (!errors.isEmpty()) {
      throw new IllegalArgumentException(String.join(", ", errors));
    }
  }
}
