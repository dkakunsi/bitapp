package io.dkakunsi.bitapp.user.repository;

import java.util.Optional;

import dev.morphia.Datastore;
import io.dkakunsi.bitapp.domain.entity.Id;
import io.dkakunsi.bitapp.mongo.MongoRepository;
import io.dkakunsi.bitapp.user.entity.User;
import io.dkakunsi.bitapp.user.model.UserModel;

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