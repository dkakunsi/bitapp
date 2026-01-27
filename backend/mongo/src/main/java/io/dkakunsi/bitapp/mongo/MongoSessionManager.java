package io.dkakunsi.bitapp.mongo;

import dev.morphia.Datastore;
import dev.morphia.transactions.MorphiaSession;
import io.dkakunsi.bitapp.database.SessionManager;

public class MongoSessionManager implements SessionManager {

  private Datastore datastore;

  public MongoSessionManager(Datastore datastore) {
    this.datastore = datastore;
  }

  @Override
  public MongoSession createSession() {
    MorphiaSession morphiaSession = datastore.startSession();
    morphiaSession.startTransaction();
    return new MongoSession(morphiaSession);
  }
}
