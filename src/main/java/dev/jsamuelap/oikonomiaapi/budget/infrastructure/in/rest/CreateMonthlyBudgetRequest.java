package dev.jsamuelap.oikonomiaapi.budget.infrastructure.in.rest;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record CreateMonthlyBudgetRequest(
  // spotless:off
  @NotNull(message = "El id de la categoría es requerido")
  UUID categoryId,

  @NotNull(message = "El mes es requerido")
  Short month,

  @NotNull(message = "El año es requerido")
  Short year,

  @NotNull(message = "La cantidad presupuestada es requerida")
  BigDecimal expectedAmount
  // spotless:on
) {
}
