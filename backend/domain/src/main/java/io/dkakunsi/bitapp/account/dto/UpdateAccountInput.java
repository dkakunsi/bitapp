package io.dkakunsi.bitapp.account.dto;

import io.dkakunsi.bitapp.account.model.Account;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public final record UpdateAccountInput(
    @NotBlank String id,
    @NotBlank String name,
    Account.Type type,
    String themeColor) {
}
