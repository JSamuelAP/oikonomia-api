package dev.jsamuelap.oikonomiaapi.budget.domain.port.in;

public interface UpdateMonthlyBudgetUseCase {
  void update(UpdateMonthlyBudgetCommand command);
}
