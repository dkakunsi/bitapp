package io.dkakunsi.bitapp.mongo.repository;

import java.util.Optional;

import dev.morphia.Datastore;
import dev.morphia.query.filters.Filters;
import io.dkakunsi.bitapp.mongo.entity.UserEntity;
import io.dkakunsi.bitapp.user.model.User;
import io.dkakunsi.bitapp.user.repository.UserRepository;

public class MongoUserRepository implements UserRepository {

  private final Datastore datastore;

  public MongoUserRepository(Datastore datastore) {
    this.datastore = datastore;
  }

  @Override
  public User save(User user) {
    var entity = UserEntity.fromUser(user);
    datastore.save(entity);
    return user;
  }

  @Override
  public Optional<User> findByEmail(String email) {
    var entity = datastore.find(UserEntity.class)
        .filter(Filters.eq("_id", email))
        .first();
    return Optional.ofNullable(entity).map(UserEntity::toUser);
  }
}