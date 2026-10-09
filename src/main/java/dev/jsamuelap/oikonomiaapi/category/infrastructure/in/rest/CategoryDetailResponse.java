package dev.jsamuelap.oikonomiaapi.category.infrastructure.in.rest;

import java.time.Instant;
import java.util.UUID;

import dev.jsamuelap.oikonomiaapi.category.domain.model.FlowType;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Category detailed response")
public record CategoryDetailResponse(
  @Schema(description = "Category unique identifier", example = "123e4567-e89b-12d3-a456-426614174000") UUID id,
  @Schema(description = "Category name", example = "Groceries") String name,
  @Schema(description = "Category flow type", example = "EXPENSE") FlowType flowType,
  @Schema(description = "Creation timestamp", example = "2026-01-15T10:30:00Z") Instant createdAt,
  @Schema(description = "Last update timestamp", example = "2026-01-15T10:30:00Z") Instant updatedAt) {
}
