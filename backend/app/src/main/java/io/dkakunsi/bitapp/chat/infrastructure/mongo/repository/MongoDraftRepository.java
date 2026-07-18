package io.dkakunsi.bitapp.chat.infrastructure.mongo.repository;

import dev.morphia.Datastore;
import io.dkakunsi.bitapp.chat.domain.entity.Draft;
import io.dkakunsi.bitapp.chat.domain.repository.DraftRepository;
import io.dkakunsi.bitapp.chat.infrastructure.mongo.model.DraftModel;
import io.dkakunsi.bitapp.mongo.MongoRepository;

public class MongoDraftRepository extends MongoRepository<DraftModel, Draft> implements DraftRepository {

  protected MongoDraftRepository(Datastore datastore) {
    super(datastore);
  }

  @Override
  protected DraftModel fromEntity(Draft entity) {
    return DraftModel.from(entity);
  }

  @Override
  protected Draft toEntity(DraftModel model) {
    return model.toDraft();
  }
}
