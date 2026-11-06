package dev.jsamuelap.oikonomiaapi.budget.domain.port.in;

import java.time.YearMonth;
import java.util.UUID;

public interface GetMonthlyBalanceUseCase {
  MonthlyBudgetBalance getMonthlyBalance(UUID userId, YearMonth month);
}
