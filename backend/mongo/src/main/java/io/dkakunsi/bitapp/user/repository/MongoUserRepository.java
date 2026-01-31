package io.dkakunsi.bitapp.user.repository;

import java.util.Optional;

import dev.morphia.Datastore;
import dev.morphia.query.filters.Filters;
import io.dkakunsi.bitapp.mongo.MongoRepository;
import io.dkakunsi.bitapp.user.entity.User;
import io.dkakunsi.bitapp.user.model.UserModel;

public class MongoUserRepository extends MongoRepository implements UserRepository {

  public MongoUserRepository(Datastore datastore) {
    super(datastore);
  }

  @Override
  public User save(User user) {
    var entity = UserModel.fromUser(user);
    pickDatastore().save(entity);
    return user;
  }

  @Override
  public Optional<User> findByEmail(String email) {
    var entity = datastore.find(UserModel.class)
        .filter(Filters.eq(MONGO_ID, email))
        .first();
    return Optional.ofNullable(entity).map(UserModel::toUser);
  }
}