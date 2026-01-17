package io.dkakunsi.bitapp.user.dto;

import io.dkakunsi.bitapp.user.validation.ValidLanguage;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public final record UpdateUserLanguageInput(
        @NotBlank String email,
        @NotBlank @ValidLanguage String language) {
}
