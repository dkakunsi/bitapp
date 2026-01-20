package io.dkakunsi.bitapp.mongo.repository;

import java.util.Optional;

import dev.morphia.Datastore;
import dev.morphia.query.filters.Filters;
import io.dkakunsi.bitapp.mongo.model.UserModel;
import io.dkakunsi.bitapp.user.entity.User;
import io.dkakunsi.bitapp.user.repository.UserRepository;

public class MongoUserRepository implements UserRepository {

  private final Datastore datastore;

  public MongoUserRepository(Datastore datastore) {
    this.datastore = datastore;
  }

  @Override
  public User save(User user) {
    var entity = UserModel.fromUser(user);
    datastore.save(entity);
    return user;
  }

  @Override
  public Optional<User> findByEmail(String email) {
    var entity = datastore.find(UserModel.class)
        .filter(Filters.eq("_id", email))
        .first();
    return Optional.ofNullable(entity).map(UserModel::toUser);
  }
}