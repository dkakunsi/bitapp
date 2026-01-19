package io.dkakunsi.bitapp.loan.dto;

import lombok.Builder;

@Builder
public record GetUserLoansInput(String userId) {
}
