package io.dkakunsi.bitapp.common;

import jakarta.validation.constraints.NotBlank;

public final record AuthorizedPrincipal(@NotBlank String email) {
}
