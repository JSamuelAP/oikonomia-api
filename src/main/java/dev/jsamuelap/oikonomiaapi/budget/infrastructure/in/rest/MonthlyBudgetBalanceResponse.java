package dev.jsamuelap.oikonomiaapi.budget.infrastructure.in.rest;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Monthly balance response")
public record MonthlyBudgetBalanceResponse(
  @Schema(description = "Month in yyyy-MM format", example = "2025-10") String month,
  @Schema(description = "Income totals") FlowTotalsResponse income,
  @Schema(description = "Expense totals") FlowTotalsResponse expense,
  @Schema(description = "Net budget balance (income - expense)", example = "699.50") BigDecimal expectedBalance,
  @Schema(description = "Net actual balance (income - expense)", example = "599.50") BigDecimal actualBalance) {
  @Schema(description = "Flow totals breakdown")
  public record FlowTotalsResponse(@Schema(description = "Expected amount", example = "500.00") BigDecimal expected,
    @Schema(description = "Actual amount", example = "450.75") BigDecimal actual,
    @Schema(description = "Remaining amount", example = "49.25") BigDecimal remaining) {
  }
}
