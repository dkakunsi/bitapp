package io.dkakunsi.money.account.process;

import io.dkakunsi.money.account.model.Account;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public final record CreateAccountInput(
    @NotBlank String name,
    @NotBlank String themeColor,
    Account.Type type) {
}
