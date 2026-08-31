package dev.jsamuelap.oikonomiaapi.user.infrastructure.out.persistence.jpa;

import org.mapstruct.Mapper;

import dev.jsamuelap.oikonomiaapi.user.domain.model.User;

@Mapper(componentModel = "spring")
public interface UserPersistenceMapper {
  default User toDomain(UserJpaEntity entity) {
    if (entity == null) {
      return null;
    }

    return User.reconstitute(entity.getId(), entity.getFirstName(), entity.getLastName(), entity.getEmail(),
      entity.getPasswordHash(), entity.getDeletedAt());
  }

  default UserJpaEntity toEntity(User domain) {
    if (domain == null) {
      return null;
    }

    UserJpaEntity entity = new UserJpaEntity();
    entity.setId(domain.getId());
    entity.setFirstName(domain.getFirstName());
    entity.setLastName(domain.getLastName());
    entity.setEmail(domain.getEmail());
    entity.setPasswordHash(domain.getPasswordHash());
    return entity;
  }
}
