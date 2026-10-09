package dev.jsamuelap.oikonomiaapi.transaction.infrastructure.in.rest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Transaction summary response")
public record TransactionResponse(
  @Schema(description = "Transaction unique identifier", example = "123e4567-e89b-12d3-a456-426614174000") UUID id,
  @Schema(description = "Transaction amount", example = "25.50") BigDecimal amount,
  @Schema(description = "Transaction date", example = "2026-01-15") LocalDate date,
  @Schema(description = "Transaction notes", example = "Groceries shopping") String notes,
  @Schema(description = "Associated category summary") CategorySummaryResponse category) {
}
