package dev.jsamuelap.oikonomiaapi.user.infrastructure.out.persistence.jpa;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import dev.jsamuelap.oikonomiaapi.user.domain.model.User;
import dev.jsamuelap.oikonomiaapi.user.domain.port.out.UserRepositoryPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepositoryPort {
  private final UserJpaRepository jpaRepository;
  private final UserPersistenceMapper mapper;

  @Override
  public Optional<User> findByEmail(String email) {
    return jpaRepository.findByEmail(email).map(mapper::toDomain);
  }

  @Override
  public Optional<User> findById(UUID id) {
    return jpaRepository.findById(id).map(mapper::toDomain);
  }

  @Override
  public User save(User user) {
    UserJpaEntity entity = mapper.toEntity(user);
    UserJpaEntity saved = jpaRepository.save(entity);
    return mapper.toDomain(saved);
  }

  @Override
  public boolean existsByEmail(String email) {
    return jpaRepository.existsByEmail(email);
  }
}
