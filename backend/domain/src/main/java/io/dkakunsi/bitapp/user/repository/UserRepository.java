package io.dkakunsi.bitapp.user.repository;

import java.util.Optional;

import io.dkakunsi.bitapp.user.model.User;

public interface UserRepository {
  User save(User user);

  Optional<User> findByEmail(String email);
}
