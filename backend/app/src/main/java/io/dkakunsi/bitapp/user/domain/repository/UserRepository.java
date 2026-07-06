package io.dkakunsi.bitapp.user.domain.repository;

import java.util.Optional;

import io.dkakunsi.bitapp.user.domain.entity.User;

public interface UserRepository {
  User save(User user);

  Optional<User> findByEmail(String email);
}
