package dev.jsamuelap.oikonomiaapi.transaction.infrastructure.in.rest;

import static dev.jsamuelap.oikonomiaapi.shared.util.StringSanitizer.trimOrNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Transaction update request")
public record UpdateTransactionRequest(
  // spotless:off
  @NotNull(message = "El id de la categoría es requerido")
  @Schema(description = "Category ID", example = "123e4567-e89b-12d3-a456-426614174000")
  UUID categoryId,

  @NotNull(message = "La cantidad es requerida")
  @Schema(description = "Transaction amount", example = "25.50", minimum = "0.01")
  BigDecimal amount,

  @NotNull(message = "La fecha es requerida")
  @Schema(description = "Transaction date", example = "2026-01-15")
  LocalDate date,

  @Size(max = 255, message = "Las notas deben tener máximo {max} catecteres de longitud")
  @Schema(description = "Transaction notes", example = "Groceries shopping")
  String notes
  // spotless:on
) {
  public UpdateTransactionRequest {
    notes = trimOrNull(notes);
  }
}
