package dev.jsamuelap.oikonomiaapi.category.infrastructure.out.persistence.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.jsamuelap.oikonomiaapi.category.domain.model.FlowType;

public interface CategoryJpaRepository extends JpaRepository<CategoryJpaEntity, UUID> {
  List<CategoryJpaEntity> findByUserIdAndDeletedAtIsNull(UUID userId);

  Optional<CategoryJpaEntity> findByIdAndUserIdAndDeletedAtIsNull(UUID id, UUID userId);

  Optional<CategoryJpaEntity> findByIdAndUserId(UUID id, UUID userId);

  boolean existsByUserIdAndNameAndFlowTypeAndDeletedAtIsNull(UUID userId, String name, FlowType flowType);
}
