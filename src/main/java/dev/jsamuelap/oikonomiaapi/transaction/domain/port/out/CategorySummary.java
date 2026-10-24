package dev.jsamuelap.oikonomiaapi.transaction.domain.port.out;

import java.util.UUID;

public record CategorySummary(UUID id, String name, String flowType, boolean deleted) {
  public boolean isIncome() {
    return "INCOME".equals(flowType);
  }

  public boolean isExpense() {
    return "EXPENSE".equals(flowType);
  }
}
