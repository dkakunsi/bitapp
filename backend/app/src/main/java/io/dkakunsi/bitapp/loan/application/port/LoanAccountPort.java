package io.dkakunsi.bitapp.loan.application.port;

import java.util.Optional;

import io.dkakunsi.bitapp.Id;
import lombok.Builder;

public interface LoanAccountPort {

  Optional<LoanAccount> findById(Id of);

  @Builder
  static record LoanAccount(
      Id userId) {

    public boolean isOwner(String requester) {
      return userId.value().equals(requester);
    }
  }
}
