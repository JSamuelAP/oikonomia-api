package dev.jsamuelap.oikonomiaapi.user.infrastructure.out.persistence.jpa;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import dev.jsamuelap.oikonomiaapi.user.domain.model.RefreshToken;
import dev.jsamuelap.oikonomiaapi.user.domain.port.out.RefreshTokenRepositoryPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepositoryPort {
  private final RefreshTokenJpaRepository jpaRepository;
  private final RefreshTokenPersistenceMapper mapper;

  @Override
  public RefreshToken save(RefreshToken refreshToken) {
    // Por si Hibernate ya está gestionando la instancia por id, reutilizarla para
    // evitar crear en algun punto una
    // nueva entidad con el mismo id que ya está asociado a otro objeto existente
    RefreshTokenJpaEntity entity = jpaRepository.findById(refreshToken.getId())
      .map(existing -> updateEntity(existing, refreshToken)).orElseGet(() -> mapper.toEntity(refreshToken));

    RefreshTokenJpaEntity saved = jpaRepository.save(entity);
    return mapper.toDomain(saved);
  }

  @Override
  public Optional<RefreshToken> findByTokenHash(String tokenHash) {
    return jpaRepository.findByTokenHash(tokenHash).map(mapper::toDomain);
  }

  @Override
  public void revokeAllByUserId(UUID userId) {
    jpaRepository.revokeAllByUserId(userId);
  }

  private RefreshTokenJpaEntity updateEntity(RefreshTokenJpaEntity entity, RefreshToken refreshToken) {
    entity.setRevoked(refreshToken.isRevoked());
    entity.setReplacedBy(refreshToken.getReplacedBy());
    return entity;
  }
}
