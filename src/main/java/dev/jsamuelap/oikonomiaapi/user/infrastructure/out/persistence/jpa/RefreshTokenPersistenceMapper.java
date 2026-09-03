package dev.jsamuelap.oikonomiaapi.user.infrastructure.out.persistence.jpa;

import org.mapstruct.Mapper;

import dev.jsamuelap.oikonomiaapi.user.domain.model.RefreshToken;

@Mapper(componentModel = "spring")
public interface RefreshTokenPersistenceMapper {
  default RefreshToken toDomain(RefreshTokenJpaEntity entity) {
    if (entity == null) {
      return null;
    }

    return RefreshToken.reconstitute(entity.getId(), entity.getUserId(), entity.getTokenHash(), entity.getExpiresAt(),
      entity.getRevoked(), entity.getCreatedAt(), entity.getReplacedBy());
  }

  default RefreshTokenJpaEntity toEntity(RefreshToken refreshToken) {
    if (refreshToken == null) {
      return null;
    }

    RefreshTokenJpaEntity entity = new RefreshTokenJpaEntity();
    entity.setId(refreshToken.getId());
    entity.setUserId(refreshToken.getUserId());
    entity.setTokenHash(refreshToken.getTokenHash());
    entity.setExpiresAt(refreshToken.getExpiresAt());
    entity.setRevoked(refreshToken.isRevoked());
    entity.setCreatedAt(refreshToken.getCreatedAt());
    entity.setReplacedBy(refreshToken.getReplacedBy());
    return entity;
  }
}
