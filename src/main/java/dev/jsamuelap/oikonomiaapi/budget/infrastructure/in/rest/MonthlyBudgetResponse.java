package dev.jsamuelap.oikonomiaapi.budget.infrastructure.in.rest;

import java.math.BigDecimal;
import java.util.UUID;

public record MonthlyBudgetResponse(UUID id, UUID userId, Short month, Short year, BigDecimal expectedAmount,
  CategorySummaryResponse category) {
}
