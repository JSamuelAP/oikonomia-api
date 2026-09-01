package dev.jsamuelap.oikonomiaapi.transaction.infrastructure.out.persistence.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionJpaRepository extends JpaRepository<TransactionJpaEntity, UUID> {
  List<TransactionJpaEntity> findByUserIdAndDeletedAtIsNull(UUID userId);

  Optional<TransactionJpaEntity> findByIdAndUserIdAndDeletedAtIsNull(UUID id, UUID userId);
}
