package io.dkakunsi.bitapp.mongo;

import java.lang.reflect.ParameterizedType;
import java.util.Optional;

import dev.morphia.Datastore;
import dev.morphia.query.filters.Filters;
import io.dkakunsi.bitapp.Id;
import io.dkakunsi.bitapp.SessionManager;

public abstract class MongoRepository<MODEL, ENTITY> {

  protected static final String MONGO_ID = "_id";

  protected final Datastore datastore;

  private final Class<MODEL> type;

  @SuppressWarnings("unchecked")
  protected MongoRepository(Datastore datastore) {
    this.datastore = datastore;
    this.type = (Class<MODEL>) ((ParameterizedType) getClass().getGenericSuperclass())
        .getActualTypeArguments()[0];
  }

  protected Datastore pickDatastore() {
    return SessionManager.getCurrentSession()
        .filter(MongoSession.class::isInstance)
        .map(s -> (Datastore) ((MongoSession) s).getSession())
        .orElse(datastore);
  }

  protected abstract MODEL fromEntity(ENTITY entity);

  protected abstract ENTITY toEntity(MODEL model);

  public ENTITY create(ENTITY entity) {
    return save(entity);
  }

  public ENTITY update(ENTITY entity) {
    return save(entity);
  }

  public ENTITY save(ENTITY entity) {
    var model = fromEntity(entity);
    var savedModel = pickDatastore().save(model);
    return toEntity(savedModel);
  }

  public void deleteById(Id id) {
    pickDatastore().find(type)
        .filter(Filters.eq(MONGO_ID, id.value()))
        .delete();
  }

  public Optional<ENTITY> findById(Id id) {
    var entity = pickDatastore().find(type)
        .filter(Filters.eq(MONGO_ID, id.value()))
        .first();
    return Optional.ofNullable(entity).map(this::toEntity);
  }
}
