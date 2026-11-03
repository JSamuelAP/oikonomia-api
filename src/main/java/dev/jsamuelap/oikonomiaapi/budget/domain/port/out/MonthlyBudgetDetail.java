package dev.jsamuelap.oikonomiaapi.budget.domain.port.out;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record MonthlyBudgetDetail(UUID id, UUID categoryId, Short month, Short year, BigDecimal expectedAmount,
  Instant createdAt, Instant updatedAt) {
}
