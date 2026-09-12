package dev.jsamuelap.oikonomiaapi.budget.domain.port.in;

import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

public interface ListMonthlyBudgetUseCase {
  List<MonthlyBudgetView> getAll(UUID userId, YearMonth yearMonth);
}
