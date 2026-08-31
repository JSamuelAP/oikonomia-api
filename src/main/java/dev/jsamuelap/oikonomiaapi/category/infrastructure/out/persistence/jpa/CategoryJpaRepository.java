package dev.jsamuelap.oikonomiaapi.category.infrastructure.out.persistence.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryJpaRepository extends JpaRepository<CategoryJpaEntity, UUID> {
  List<CategoryJpaEntity> findByUserIdAndDeletedAtIsNull(UUID userId);

  Optional<CategoryJpaEntity> findByIdAndUserIdAndDeletedAtIsNull(UUID id, UUID userId);
}
