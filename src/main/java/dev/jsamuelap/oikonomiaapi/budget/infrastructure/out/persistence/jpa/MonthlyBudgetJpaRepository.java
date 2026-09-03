package dev.jsamuelap.oikonomiaapi.budget.infrastructure.out.persistence.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MonthlyBudgetJpaRepository extends JpaRepository<MonthlyBudgetJpaEntity, UUID> {
  List<MonthlyBudgetJpaEntity> findByUserIdAndYearAndDeletedAtIsNull(UUID userId, Short year);

  Optional<MonthlyBudgetJpaEntity> findByIdAndUserIdAndDeletedAtIsNull(UUID id, UUID userId);
}
