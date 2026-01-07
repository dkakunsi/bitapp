package io.dkakunsi.bitapp.account.dto;

import io.dkakunsi.bitapp.account.model.Account;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public final record CreateAccountInput(
    @NotBlank String name,
    @NotBlank String themeColor,
    Account.Type type) {
}
