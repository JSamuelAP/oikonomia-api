package dev.jsamuelap.oikonomiaapi.budget.domain.port.in;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateMonthlyBudgetCommand(UUID userId, UUID categoryId, Short month, Short year,
  BigDecimal expectedAmount) {
}
