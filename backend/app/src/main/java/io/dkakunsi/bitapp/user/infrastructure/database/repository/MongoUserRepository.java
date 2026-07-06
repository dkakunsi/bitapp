package io.dkakunsi.bitapp.user.infrastructure.database.repository;

import java.util.Optional;

import dev.morphia.Datastore;
import io.dkakunsi.bitapp.Id;
import io.dkakunsi.bitapp.mongo.MongoRepository;
import io.dkakunsi.bitapp.user.domain.entity.User;
import io.dkakunsi.bitapp.user.domain.repository.UserRepository;
import io.dkakunsi.bitapp.user.infrastructure.database.model.UserModel;

public class MongoUserRepository extends MongoRepository<UserModel, User> implements UserRepository {

  public MongoUserRepository(Datastore datastore) {
    super(datastore);
  }

  @Override
  protected UserModel fromEntity(User entity) {
    return UserModel.fromUser(entity);
  }

  @Override
  protected User toEntity(UserModel model) {
    return model.toUser();
  }

  @Override
  public Optional<User> findByEmail(String email) {
    return findById(Id.of(email));
  }
}