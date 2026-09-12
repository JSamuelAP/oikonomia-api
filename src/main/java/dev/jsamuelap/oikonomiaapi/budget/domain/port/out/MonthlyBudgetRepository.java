package dev.jsamuelap.oikonomiaapi.budget.domain.port.out;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import dev.jsamuelap.oikonomiaapi.budget.domain.model.MonthlyBudget;

public interface MonthlyBudgetRepository {
  List<MonthlyBudget> findAllByUser(UUID userId, YearMonth yearMonth);

  Optional<MonthlyBudget> findByIdAndUser(UUID id, UUID userId);

  boolean existsByCategoryAndUserAndDate(UUID categoryId, UUID userId, Short month, Short year);

  MonthlyBudget save(MonthlyBudget budget);
}
