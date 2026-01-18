package io.dkakunsi.bitapp.user.dto;

import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;

import io.dkakunsi.bitapp.common.Validatable;
import io.dkakunsi.bitapp.user.entity.User;
import lombok.Builder;

@Builder
public final record UpdateUserLanguageInput(
    String email,
    String language) implements Validatable {

  @Override
  public void validate() throws IllegalArgumentException {
    var errors = new ArrayList<String>();
    if (StringUtils.isBlank(email)) {
      errors.add("email: invalid value");
    }
    if (!User.Language.isValid(language)) {
      errors.add("language: invalid value");
    }

    if (!errors.isEmpty()) {
      throw new IllegalArgumentException(String.join(", ", errors));
    }
  }
}
