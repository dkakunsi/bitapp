package io.dkakunsi.bitapp.user.dto;

import lombok.Builder;

@Builder
public final record UserResult(
    String email,
    String name,
    String phone,
    String photoUrl,
    String language) {
}
