package dev.jsamuelap.oikonomiaapi.budget.domain.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import dev.jsamuelap.oikonomiaapi.budget.domain.model.MonthlyBudget;

public interface MonthlyBudgetRepository {
  List<MonthlyBudget> findAllByUser(UUID userId, Short year);

  Optional<MonthlyBudget> findByIdAndUser(UUID id, UUID userId);

  MonthlyBudget save(MonthlyBudget budget);
}
