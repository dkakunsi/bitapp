package io.dkakunsi.bitapp.account.application.dto;

import org.apache.commons.lang3.StringUtils;

import io.dkakunsi.bitapp.Validatable;
import io.dkakunsi.bitapp.account.domain.entity.Account;
import lombok.Builder;

@Builder
public final record UpdateAccountInput(
    String id,
    String name,
    String type,
    String themeColor) implements Validatable {

  @Override
  public void validate() {
    var errors = new java.util.ArrayList<String>();
    if (name == null && type == null && themeColor == null) {
      errors.add("invalid request. fields could not be all null");
    }
    if (StringUtils.isBlank(id)) {
      errors.add("id: invalid value: " + id);
    }
    if (name != null && StringUtils.isBlank(name)) {
      errors.add("name: invalid value: " + name);
    }
    if (type != null && !Account.Type.isValid(type)) {
      errors.add("type: invalid value: " + type);
    }
    if (themeColor != null && StringUtils.isBlank(themeColor)) {
      errors.add("themeColor: invalid value: " + themeColor);
    }

    if (!errors.isEmpty()) {
      throw new IllegalArgumentException(String.join(", ", errors));
    }
  }
}
