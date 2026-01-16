package io.dkakunsi.bitapp.user.dto;

import io.dkakunsi.bitapp.user.model.User.Language;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public final record UpdateUserLanguageInput(
    @NotBlank String email,
    @NotNull Language language) {
}
