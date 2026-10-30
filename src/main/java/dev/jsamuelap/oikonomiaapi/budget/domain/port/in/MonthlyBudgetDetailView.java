package dev.jsamuelap.oikonomiaapi.budget.domain.port.in;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import dev.jsamuelap.oikonomiaapi.budget.domain.port.out.CategorySummary;

public record MonthlyBudgetDetailView(UUID id, Short month, Short year, BigDecimal expectedAmount,
  BigDecimal actualAmount, CategorySummary category, Instant createdAt, Instant updatedAt) {
  public BigDecimal remainingAmount() {
    return expectedAmount.subtract(actualAmount);
  }
}
