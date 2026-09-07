package dev.jsamuelap.oikonomiaapi.category.infrastructure.out.persistence.jpa;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.jsamuelap.oikonomiaapi.category.domain.model.FlowType;

public interface CategoryJpaRepository extends JpaRepository<CategoryJpaEntity, UUID> {
  Optional<CategoryJpaEntity> findByIdAndUserIdAndDeletedAtIsNull(UUID id, UUID userId);

  Optional<CategoryJpaEntity> findByIdAndUserId(UUID id, UUID userId);

  List<CategoryJpaEntity> findByUserIdAndDeletedAtIsNull(UUID userId);

  List<CategoryJpaEntity> findByIdInAndUserId(Set<UUID> categoryIds, UUID userId);

  boolean existsByUserIdAndNameAndFlowTypeAndDeletedAtIsNull(UUID userId, String name, FlowType flowType);
}
