package io.dkakunsi.bitapp.draft.domain.repository;

import java.util.Optional;

import io.dkakunsi.bitapp.Id;
import io.dkakunsi.bitapp.draft.domain.entity.Draft;

public interface DraftRepository {

  Optional<Draft> findByIdAndNotConfirmed(Id draftId);

  Draft save(Draft draft);

}
