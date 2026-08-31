package dev.jsamuelap.oikonomiaapi.user.domain.port.out;

import java.util.Optional;
import java.util.UUID;

import dev.jsamuelap.oikonomiaapi.user.domain.model.User;

public interface UserRepositoryPort {
  Optional<User> findByEmail(String email);

  Optional<User> findById(UUID id);

  User save(User user);

  boolean existsByEmail(String email);
}
