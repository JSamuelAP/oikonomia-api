package dev.jsamuelap.oikonomiaapi.budget.infrastructure.in.rest;

import java.math.BigDecimal;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Monthly budget response")
public record MonthlyBudgetResponse(
  @Schema(description = "Monthly budget unique identifier", example = "123e4567-e89b-12d3-a456-426614174000") UUID id,
  @Schema(description = "Budget month (1-12)", example = "1") Short month,
  @Schema(description = "Budget year", example = "2026") Short year,
  @Schema(description = "Expected budget amount", example = "500.00") BigDecimal expectedAmount,
  @Schema(description = "Associated category summary") CategorySummaryResponse category) {
}
