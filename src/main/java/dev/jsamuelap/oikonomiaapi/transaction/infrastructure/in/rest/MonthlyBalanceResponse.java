package dev.jsamuelap.oikonomiaapi.transaction.infrastructure.in.rest;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Monthly balance response")
public record MonthlyBalanceResponse(@Schema(description = "Month in yyyy-MM format", example = "2025-10") String month,
  @Schema(description = "Total income for the month", example = "2500.00") BigDecimal totalIncome,
  @Schema(description = "Total expenses for the month", example = "1800.50") BigDecimal totalExpense,
  @Schema(description = "Net balance (income - expense)", example = "699.50") BigDecimal balance) {
}
