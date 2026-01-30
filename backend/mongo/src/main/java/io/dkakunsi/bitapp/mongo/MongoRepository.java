package io.dkakunsi.bitapp.mongo;

import java.util.NoSuchElementException;

import dev.morphia.Datastore;
import io.dkakunsi.bitapp.database.SessionManager;

public abstract class MongoRepository {

  protected final Datastore datastore;

  protected MongoRepository(Datastore datastore) {
    this.datastore = datastore;
  }

  protected Datastore pickDatastore() {
    var session = getCurrentSession();
    if (session != null) {
      return session.getSession();
    } else {
      return datastore;
    }
  }

  private MongoSession getCurrentSession() {
    try {
      return (MongoSession) SessionManager.SESSION.get();
    } catch (NoSuchElementException e) {
      return null;
    }
  }
}
