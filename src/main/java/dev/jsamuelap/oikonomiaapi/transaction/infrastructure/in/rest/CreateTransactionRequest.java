package dev.jsamuelap.oikonomiaapi.transaction.infrastructure.in.rest;

import static dev.jsamuelap.oikonomiaapi.shared.util.StringSanitizer.trimOrNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateTransactionRequest(
  // spotless:off
  @NotNull(message = "El id de la categoría es requerido")
  UUID categoryId,

  @NotNull(message = "La cantidad es requerida")
  BigDecimal amount,

  @NotNull(message = "La fecha es requerida")
  LocalDate date,

  @Size(max = 255, message = "Las notas deben tener máximo {max} catecteres de longitud")
  String notes
  // spotless:on
) {
  public CreateTransactionRequest {
    notes = trimOrNull(notes);
  }
}
