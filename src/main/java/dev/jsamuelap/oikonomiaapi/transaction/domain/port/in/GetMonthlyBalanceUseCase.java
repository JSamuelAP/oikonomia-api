package dev.jsamuelap.oikonomiaapi.transaction.domain.port.in;

import java.time.YearMonth;
import java.util.UUID;

public interface GetMonthlyBalanceUseCase {
  MonthlyBalance getMonthlyBalance(UUID userId, YearMonth month);
}
