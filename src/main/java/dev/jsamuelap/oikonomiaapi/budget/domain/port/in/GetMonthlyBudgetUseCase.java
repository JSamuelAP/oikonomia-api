package dev.jsamuelap.oikonomiaapi.budget.domain.port.in;

import java.util.UUID;

public interface GetMonthlyBudgetUseCase {
  MonthlyBudgetView getById(UUID id, UUID userId);
}
