package io.dkakunsi.bitapp.chat.domain.repository;

import java.util.Optional;

import io.dkakunsi.bitapp.chat.domain.entity.Draft;

public interface DraftRepository {

  Optional<Draft> findOne(String draftId);

  Draft save(Draft draft);

}
