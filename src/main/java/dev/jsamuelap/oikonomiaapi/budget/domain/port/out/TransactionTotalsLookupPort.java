package dev.jsamuelap.oikonomiaapi.budget.domain.port.out;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Map;
import java.util.UUID;

public interface TransactionTotalsLookupPort {
  Map<UUID, BigDecimal> findTotalsByCategory(UUID userId, YearMonth month);
}
