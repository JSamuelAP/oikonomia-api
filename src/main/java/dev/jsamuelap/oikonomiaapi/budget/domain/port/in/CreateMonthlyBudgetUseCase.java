package dev.jsamuelap.oikonomiaapi.budget.domain.port.in;

import java.util.UUID;

public interface CreateMonthlyBudgetUseCase {
  UUID create(CreateMonthlyBudgetCommand command);
}
