package io.dkakunsi.bitapp.user.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Builder;

@Builder
@JsonInclude(Include.ALWAYS)
public final record UserResult(
    String email,
    String name,
    String phone,
    String photoUrl,
    String language) {
}
