package dev.jsamuelap.oikonomiaapi.budget.domain.port.in;

import java.math.BigDecimal;
import java.util.UUID;

import dev.jsamuelap.oikonomiaapi.budget.domain.port.out.CategorySummary;

public record MonthlyBudgetView(UUID id, UUID userId, Short month, Short year, BigDecimal expectedAmount,
  CategorySummary category) {
}
