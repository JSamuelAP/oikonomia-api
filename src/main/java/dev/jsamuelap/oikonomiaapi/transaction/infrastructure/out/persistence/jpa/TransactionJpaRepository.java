package dev.jsamuelap.oikonomiaapi.transaction.infrastructure.out.persistence.jpa;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TransactionJpaRepository extends JpaRepository<TransactionJpaEntity, UUID> {
  @Query("""
    SELECT t FROM TransactionJpaEntity t
    WHERE t.userId = :userId AND t.deletedAt IS NULL
        AND t.transactionDate >= :startOfMonth
        AND t.transactionDate < :startOfNextMonth
    ORDER BY t.transactionDate DESC
    """)
  List<TransactionJpaEntity> findActiveByUserIdAndDateRange(UUID userId, LocalDate startOfMonth,
    LocalDate startOfNextMonth);

  Optional<TransactionJpaEntity> findByIdAndUserIdAndDeletedAtIsNull(UUID id, UUID userId);
}
