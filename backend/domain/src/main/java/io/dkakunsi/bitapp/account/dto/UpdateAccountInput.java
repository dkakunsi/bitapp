package io.dkakunsi.bitapp.account.dto;

import io.dkakunsi.bitapp.account.validation.ValidAccountType;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public final record UpdateAccountInput(
        @NotBlank String id,
        @NotBlank String name,
        @ValidAccountType String type,
        String themeColor) {
}
