package dev.jsamuelap.oikonomiaapi.budget.infrastructure.in.rest;

import java.math.BigDecimal;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Monthly budget update request")
public record UpdateMonthlyBudgetRequest(
  // spotless:off
  @NotNull(message = "El id de la categoría es requerido")
  @Schema(description = "Category ID", example = "123e4567-e89b-12d3-a456-426614174000")
  UUID categoryId,

  @NotNull(message = "El mes es requerido")
  @Schema(description = "Budget month (1-12)", example = "1", minimum = "1", maximum = "12")
  Short month,

  @NotNull(message = "El año es requerido")
  @Schema(description = "Budget year", example = "2026", minimum = "2025", maximum = "2100")
  Short year,

  @NotNull(message = "La cantidad presupuestada es requerida")
  @Schema(description = "Expected budget amount", example = "500.00", minimum = "0.01")
  BigDecimal expectedAmount
  // spotless:on
) {
}
