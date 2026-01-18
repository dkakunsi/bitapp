package io.dkakunsi.bitapp.account.dto;

import lombok.Builder;

@Builder
public record GetUserAccountsInput(String userId) {
}
