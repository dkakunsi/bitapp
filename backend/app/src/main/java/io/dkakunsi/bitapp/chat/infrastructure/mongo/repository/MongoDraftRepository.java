package io.dkakunsi.bitapp.chat.infrastructure.mongo.repository;

import java.util.Optional;

import dev.morphia.Datastore;
import dev.morphia.query.filters.Filters;
import io.dkakunsi.bitapp.Id;
import io.dkakunsi.bitapp.chat.domain.entity.Draft;
import io.dkakunsi.bitapp.chat.domain.repository.DraftRepository;
import io.dkakunsi.bitapp.chat.infrastructure.mongo.model.DraftModel;
import io.dkakunsi.bitapp.mongo.MongoRepository;

public class MongoDraftRepository extends MongoRepository<DraftModel, Draft> implements DraftRepository {

  public MongoDraftRepository(Datastore datastore) {
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

  @Override
  public Optional<Draft> findByIdAndNotConfirmed(Id draftId) {
    var query = datastore.find(DraftModel.class)
        .filter(Filters.eq(MONGO_ID, draftId.value()))
        .filter(Filters.eq("confirmed", false));

    var draftModel = query.first();
    return Optional.ofNullable(draftModel).map(this::toEntity);
  }
}
