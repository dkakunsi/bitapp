package io.dkakunsi.bitapp.mongo;

import dev.morphia.Datastore;
import io.dkakunsi.bitapp.database.SessionManager;

public abstract class MongoRepository {

  protected static final String MONGO_ID = "_id";

  protected final Datastore datastore;

  protected MongoRepository(Datastore datastore) {
    this.datastore = datastore;
  }

  protected Datastore pickDatastore() {
    return SessionManager.getCurrentSession()
        .filter(s -> s instanceof MongoSession)
        .map(s -> (Datastore) ((MongoSession) s).getSession())
        .orElse(datastore);
  }
}
