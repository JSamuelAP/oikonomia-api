package dev.jsamuelap.oikonomiaapi.budget.infrastructure.in.rest;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Category summary in monthly budget response")
public record CategorySummaryResponse(
  @Schema(description = "Category unique identifier", example = "123e4567-e89b-12d3-a456-426614174000") UUID id,
  @Schema(description = "Category name", example = "Groceries") String name,
  @Schema(description = "Category flow type", example = "EXPENSE") String flowType,
  @Schema(description = "Whether the category is deleted", example = "false") boolean deleted) {
}
