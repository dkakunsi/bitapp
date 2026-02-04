package io.dkakunsi.bitapp.mongo;

import dev.morphia.transactions.MorphiaSession;
import io.dkakunsi.bitapp.database.Session;

public class MongoSession implements Session {
  private final MorphiaSession morphiaSession;

  public MongoSession(MorphiaSession morphiaSession) {
    this.morphiaSession = morphiaSession;
  }

  @Override
  public void commit() {
    morphiaSession.commitTransaction();
  }

  @Override
  public void rollback() {
    morphiaSession.abortTransaction();
  }

  @Override
  public void close() {
    morphiaSession.close();
  }

  public MorphiaSession getSession() {
    return morphiaSession;
  }
}
