package io.dkakunsi.bitapp.account.dto;

import org.apache.commons.lang3.StringUtils;

import io.dkakunsi.bitapp.account.model.Account;
import io.dkakunsi.bitapp.common.Validatable;
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
      errors.add("invalid request");
    }
    if (StringUtils.isBlank(id)) {
      errors.add("id: invalid value");
    }
    if (name != null && StringUtils.isBlank(name)) {
      errors.add("name: invalid value");
    }
    if (type != null && !Account.Type.isValid(type)) {
      errors.add("type: invalid value");
    }
    if (themeColor != null && StringUtils.isBlank(themeColor)) {
      errors.add("themeColor: invalid value");
    }

    if (!errors.isEmpty()) {
      throw new IllegalArgumentException(String.join(", ", errors));
    }
  }
}
