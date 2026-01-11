package io.dkakunsi.bitapp.account.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record GetUserAccountsInput(
        @NotBlank String userId) {
}
