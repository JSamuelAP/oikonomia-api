package dev.jsamuelap.oikonomiaapi.budget.infrastructure.in.rest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Monthly budget detailed response")
public record MonthlyBudgetDetailResponse(
  @Schema(description = "Monthly budget unique identifier", example = "123e4567-e89b-12d3-a456-426614174000") UUID id,
  @Schema(description = "Budget month (1-12)", example = "1") Short month,
  @Schema(description = "Budget year", example = "2026") Short year,
  @Schema(description = "Expected budget amount", example = "500.00") BigDecimal expectedAmount,
  @Schema(description = "Total actual accumulated for the month for the category", example = "300.00") BigDecimal actualAmount,
  @Schema(description = "Expected minus actual. Negative when the actual exceeds the expected.", example = "200.00") BigDecimal remainingAmount,
  @Schema(description = "Associated category summary") CategorySummaryResponse category,
  @Schema(description = "Creation timestamp", example = "2026-01-15T10:30:00Z") Instant createdAt,
  @Schema(description = "Last update timestamp", example = "2026-01-15T10:30:00Z") Instant updatedAt) {
}
