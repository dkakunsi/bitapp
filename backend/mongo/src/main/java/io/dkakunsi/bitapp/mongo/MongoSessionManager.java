package io.dkakunsi.bitapp.mongo;

import java.util.function.Supplier;

import dev.morphia.Datastore;
import dev.morphia.transactions.MorphiaSession;
import io.dkakunsi.bitapp.common.AppError;
import io.dkakunsi.bitapp.database.Session;
import io.dkakunsi.bitapp.database.SessionManager;
import io.dkakunsi.bitapp.domain.usecase.Result;

public class MongoSessionManager implements SessionManager {

  private Datastore datastore;

  public MongoSessionManager(Datastore datastore) {
    this.datastore = datastore;
  }

  @Override
  public <T> Result<T> executeInSession(Supplier<Result<T>> function) {
    return SessionManager.getCurrentSession()
        .filter(MongoSession.class::isInstance)
        .map(_ -> function.get())
        .orElseGet(() -> executeNewSession(function));
  }

  private <T> Result<T> executeNewSession(Supplier<Result<T>> function) {
    try (Session session = createSession()) {
      return ScopedValue.where(SESSION, session).call(() -> {
        try {
          Result<T> functionResult = function.get();
          if (functionResult.isSuccess()) {
            session.commit();
          } else {
            session.rollback();
          }
          return functionResult;
        } catch (Exception e) {
          session.rollback();
          return Result.failure(AppError.Code.INTERNAL_ERROR, e.getMessage());
        }
      });
    } catch (Exception e) {
      return Result.failure(AppError.Code.INTERNAL_ERROR, e.getMessage());
    }
  }

  private MongoSession createSession() {
    MorphiaSession morphiaSession = datastore.startSession();
    morphiaSession.startTransaction();
    return new MongoSession(morphiaSession);
  }
}
