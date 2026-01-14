package io.dkakunsi.bitapp.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public final record GetUserInput(@NotBlank @Email String email) {
}
