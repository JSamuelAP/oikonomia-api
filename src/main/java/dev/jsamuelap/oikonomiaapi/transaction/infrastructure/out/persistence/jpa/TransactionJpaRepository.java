package dev.jsamuelap.oikonomiaapi.transaction.infrastructure.out.persistence.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransactionJpaRepository extends JpaRepository<TransactionJpaEntity, UUID> {
  @Query("""
      SELECT t FROM TransactionJpaEntity t
        WHERE t.userId = :userId AND t.deletedAt IS NULL AND MONTH(t.transactionDate) = :month AND YEAR(t.transactionDate) = :year
    """)
  List<TransactionJpaEntity> findByUserIdAndMonthAndYear(@Param("userId") UUID userId, @Param("month") Short month,
    @Param("year") Short year);

  Optional<TransactionJpaEntity> findByIdAndUserIdAndDeletedAtIsNull(UUID id, UUID userId);
}
