package dev.jsamuelap.oikonomiaapi.budget.domain.port.in;

import java.util.UUID;

public interface DeleteMonthlyBudgetUseCase {
  void deleteById(UUID id, UUID userId);
}
