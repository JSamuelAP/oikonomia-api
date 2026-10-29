package dev.jsamuelap.oikonomiaapi.budget.domain.port.in;

import java.util.UUID;

public interface GetMonthlyBudgetUseCase {
  MonthlyBudgetDetailView getById(UUID id, UUID userId);
}
