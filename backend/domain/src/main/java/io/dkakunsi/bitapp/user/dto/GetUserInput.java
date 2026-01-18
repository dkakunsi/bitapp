package io.dkakunsi.bitapp.user.dto;

import lombok.Builder;

@Builder
public final record GetUserInput(String email) {
}
