package io.dkakunsi.bitapp.account.dto;

import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;

import io.dkakunsi.bitapp.account.entity.Account;
import io.dkakunsi.bitapp.common.Validatable;
import lombok.Builder;

@Builder
public final record CreateAccountInput(
    String name,
    String type,
    String themeColor) implements Validatable {

  @Override
  public void validate() {
    var errors = new ArrayList<String>();
    if (StringUtils.isBlank(name)) {
      errors.add("name: invalid value: " + name);
    }
    if (!Account.Type.isValid(type)) {
      errors.add("type: invalid value: " + type);
    }

    if (!errors.isEmpty()) {
      throw new IllegalArgumentException(String.join(", ", errors));
    }
  }
}
