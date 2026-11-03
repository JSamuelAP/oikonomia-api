package dev.jsamuelap.oikonomiaapi.transaction.domain.port.in;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Map;
import java.util.UUID;

public interface GetMonthlyTotalsByCategoryUseCase {
  Map<UUID, BigDecimal> getTotalsByCategory(UUID userId, YearMonth month);
}
